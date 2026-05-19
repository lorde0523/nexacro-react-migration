package com.lorde0523.migration.analysis.parser;

import com.lorde0523.migration.analysis.model.ApiEndpointCandidate;
import com.lorde0523.migration.analysis.model.DatasetColumn;
import com.lorde0523.migration.analysis.model.DatasetSpec;
import com.lorde0523.migration.analysis.model.MigrationAnalysisReport;
import com.lorde0523.migration.analysis.model.ScreenAnalysis;
import com.lorde0523.migration.analysis.model.SearchParameterCandidate;
import com.lorde0523.migration.analysis.model.ServerMappingCandidate;
import com.lorde0523.migration.analysis.model.TransactionSpec;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class NexacroContractExtractor {

    private static final Pattern FORM_ID = Pattern.compile("<Form\\s+[^>]*id=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
    private static final Pattern TRANSACTION = Pattern.compile(
            "transaction\\s*\\((.*?)\\)\\s*;",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );
    private static final Pattern SET_COLUMN = Pattern.compile(
            "(?:this\\.)?(\\w+)\\.setColumn\\s*\\([^,]+,\\s*[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern GET_COLUMN = Pattern.compile(
            "(?:this\\.)?(\\w+)\\.getColumn\\s*\\([^,]+,\\s*[\"']([^\"']+)[\"']",
            Pattern.CASE_INSENSITIVE
    );

    public MigrationAnalysisReport analyze(Path nexacroRoot, Path legacyServerRoot) {
        if (nexacroRoot == null || !Files.isDirectory(nexacroRoot)) {
            throw new IllegalArgumentException("nexacroRoot must be an existing directory");
        }

        List<ScreenAnalysis> screens = findFiles(nexacroRoot, ".xfdl").stream()
                .map(path -> analyzeScreen(nexacroRoot, path, legacyServerRoot))
                .sorted(Comparator.comparing(ScreenAnalysis::screenId))
                .toList();

        return new MigrationAnalysisReport(
                Instant.now(),
                nexacroRoot.toAbsolutePath().normalize().toString(),
                legacyServerRoot == null ? null : legacyServerRoot.toAbsolutePath().normalize().toString(),
                screens
        );
    }

    private ScreenAnalysis analyzeScreen(Path nexacroRoot, Path xfdlPath, Path legacyServerRoot) {
        String xfdlContent = read(xfdlPath);
        String screenId = extractScreenId(xfdlContent).orElse(stripExtension(xfdlPath.getFileName().toString()));
        Path scriptPath = siblingScriptPath(xfdlPath);
        String scriptContent = xfdlContent + "\n" + (Files.exists(scriptPath) ? read(scriptPath) : "");

        List<DatasetSpec> datasets = parseDatasets(xfdlContent);
        List<TransactionSpec> transactions = parseTransactions(scriptContent);
        List<SearchParameterCandidate> searchParameters = parseSearchParameters(scriptContent);
        List<ServerMappingCandidate> mappings = mapLegacySources(transactions, legacyServerRoot);
        List<ApiEndpointCandidate> endpoints = transactions.stream()
                .map(this::toEndpointCandidate)
                .toList();

        return new ScreenAnalysis(
                screenId,
                nexacroRoot.relativize(xfdlPath).toString().replace('\\', '/'),
                datasets,
                transactions,
                searchParameters,
                mappings,
                endpoints
        );
    }

    private List<DatasetSpec> parseDatasets(String content) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(content)));
            NodeList datasetNodes = document.getElementsByTagName("Dataset");
            List<DatasetSpec> datasets = new ArrayList<>();

            for (int i = 0; i < datasetNodes.getLength(); i++) {
                Element dataset = (Element) datasetNodes.item(i);
                String datasetName = dataset.getAttribute("id");
                NodeList columnNodes = dataset.getElementsByTagName("Column");
                List<DatasetColumn> columns = new ArrayList<>();
                for (int j = 0; j < columnNodes.getLength(); j++) {
                    Element column = (Element) columnNodes.item(j);
                    columns.add(new DatasetColumn(
                            column.getAttribute("id"),
                            column.getAttribute("type"),
                            column.getAttribute("size")
                    ));
                }
                datasets.add(new DatasetSpec(datasetName, List.copyOf(columns)));
            }
            return List.copyOf(datasets);
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private List<TransactionSpec> parseTransactions(String scriptContent) {
        Matcher matcher = TRANSACTION.matcher(scriptContent);
        List<TransactionSpec> transactions = new ArrayList<>();

        while (matcher.find()) {
            List<String> args = splitArguments(matcher.group(1));
            if (args.size() < 2) {
                continue;
            }
            transactions.add(new TransactionSpec(
                    unquote(args.get(0)),
                    unquote(args.get(1)),
                    parseDatasetMapping(args.size() > 2 ? unquote(args.get(2)) : ""),
                    parseDatasetMapping(args.size() > 3 ? unquote(args.get(3)) : ""),
                    args.size() > 4 ? args.get(4).trim() : "",
                    args.size() > 5 ? unquote(args.get(5)) : ""
            ));
        }

        return List.copyOf(transactions);
    }

    private List<SearchParameterCandidate> parseSearchParameters(String scriptContent) {
        Map<String, SearchParameterCandidate> candidates = new LinkedHashMap<>();
        collectColumnReferences(candidates, SET_COLUMN.matcher(scriptContent), "setColumn");
        collectColumnReferences(candidates, GET_COLUMN.matcher(scriptContent), "getColumn");
        return List.copyOf(candidates.values());
    }

    private void collectColumnReferences(
            Map<String, SearchParameterCandidate> candidates,
            Matcher matcher,
            String source
    ) {
        while (matcher.find()) {
            String datasetName = matcher.group(1);
            String columnName = matcher.group(2);
            String key = datasetName + "." + columnName;
            candidates.putIfAbsent(key, new SearchParameterCandidate(columnName, datasetName, source));
        }
    }

    private List<ServerMappingCandidate> mapLegacySources(List<TransactionSpec> transactions, Path legacyServerRoot) {
        if (legacyServerRoot == null || !Files.isDirectory(legacyServerRoot) || transactions.isEmpty()) {
            return List.of();
        }

        List<Path> sourceFiles = findFiles(legacyServerRoot, ".java", ".xml", ".sql", ".properties");
        List<ServerMappingCandidate> mappings = new ArrayList<>();
        for (TransactionSpec transaction : transactions) {
            for (Path sourceFile : sourceFiles) {
                String content = read(sourceFile);
                if (containsAny(content, transaction.name(), transaction.serviceUrl())) {
                    mappings.add(new ServerMappingCandidate(
                            transaction.name(),
                            transaction.serviceUrl(),
                            legacyServerRoot.relativize(sourceFile).toString().replace('\\', '/'),
                            "matched transaction name or service URL"
                    ));
                }
            }
        }
        return List.copyOf(mappings);
    }

    private ApiEndpointCandidate toEndpointCandidate(TransactionSpec transaction) {
        String path = transaction.serviceUrl();
        if (path == null || path.isBlank()) {
            path = "/" + transaction.name();
        }

        path = path.replace('\\', '/')
                .replaceAll("\\.do$", "")
                .replaceAll("^/+", "");

        if (!path.startsWith("api/")) {
            path = "api/" + path;
        }

        return new ApiEndpointCandidate(
                "POST",
                "/" + path,
                toPascalCase(transaction.name()) + "Request",
                toPascalCase(transaction.name()) + "Response"
        );
    }

    private Optional<String> extractScreenId(String content) {
        Matcher matcher = FORM_ID.matcher(content);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    private List<Path> findFiles(Path root, String... extensions) {
        Set<String> suffixes = Set.of(extensions);
        try (Stream<Path> stream = Files.walk(root)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(path -> suffixes.stream().anyMatch(path.toString().toLowerCase(Locale.ROOT)::endsWith))
                    .sorted()
                    .toList();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to scan " + root, e);
        }
    }

    private Path siblingScriptPath(Path xfdlPath) {
        String fileName = xfdlPath.getFileName().toString();
        return xfdlPath.resolveSibling(stripExtension(fileName) + ".xjs");
    }

    private String read(Path path) {
        try {
            return Files.readString(path);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read " + path, e);
        }
    }

    private List<String> splitArguments(String expression) {
        List<String> args = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        char quote = 0;

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            if ((ch == '"' || ch == '\'') && (i == 0 || expression.charAt(i - 1) != '\\')) {
                if (!inQuote) {
                    inQuote = true;
                    quote = ch;
                } else if (quote == ch) {
                    inQuote = false;
                }
            }
            if (ch == ',' && !inQuote) {
                args.add(current.toString().trim());
                current.setLength(0);
                continue;
            }
            current.append(ch);
        }

        if (!current.isEmpty()) {
            args.add(current.toString().trim());
        }
        return args;
    }

    private Map<String, String> parseDatasetMapping(String value) {
        if (value == null || value.isBlank()) {
            return Map.of();
        }

        Map<String, String> mappings = new LinkedHashMap<>();
        for (String token : value.split("\\s+")) {
            String[] parts = token.split("=", 2);
            if (parts.length == 2 && !parts[0].isBlank() && !parts[1].isBlank()) {
                mappings.put(parts[0].trim(), parts[1].trim());
            }
        }
        return Map.copyOf(mappings);
    }

    private String unquote(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if ((trimmed.startsWith("\"") && trimmed.endsWith("\""))
                || (trimmed.startsWith("'") && trimmed.endsWith("'"))) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private boolean containsAny(String content, String... needles) {
        for (String needle : needles) {
            if (needle != null && !needle.isBlank() && content.contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private String stripExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? fileName : fileName.substring(0, dot);
    }

    private String toPascalCase(String value) {
        if (value == null || value.isBlank()) {
            return "Generated";
        }

        String[] parts = value.replaceAll("[^A-Za-z0-9]+", " ").split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            result.append(part.substring(0, 1).toUpperCase(Locale.ROOT));
            if (part.length() > 1) {
                result.append(part.substring(1));
            }
        }
        return Objects.requireNonNullElse(result.toString(), "Generated");
    }
}
