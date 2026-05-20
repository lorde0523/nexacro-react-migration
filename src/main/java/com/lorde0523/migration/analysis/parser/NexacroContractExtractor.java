package com.lorde0523.migration.analysis.parser;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.lorde0523.migration.analysis.model.ApiEndpointCandidate;
import com.lorde0523.migration.analysis.model.AnalysisRequest;
import com.lorde0523.migration.analysis.model.DataAccessDecision;
import com.lorde0523.migration.analysis.model.DatasetColumn;
import com.lorde0523.migration.analysis.model.DatasetSpec;
import com.lorde0523.migration.analysis.model.DtoCandidate;
import com.lorde0523.migration.analysis.model.DtoFieldCandidate;
import com.lorde0523.migration.analysis.model.GeneratedFile;
import com.lorde0523.migration.analysis.model.IbatisSqlMap;
import com.lorde0523.migration.analysis.model.IbatisStatement;
import com.lorde0523.migration.analysis.model.LegacyJavaClass;
import com.lorde0523.migration.analysis.model.LegacyJavaMethod;
import com.lorde0523.migration.analysis.model.LegacyLayer;
import com.lorde0523.migration.analysis.model.LegacyMethodFlow;
import com.lorde0523.migration.analysis.model.MigrationAnalysisReport;
import com.lorde0523.migration.analysis.model.MigrationUnit;
import com.lorde0523.migration.analysis.model.NexcoreServiceMethod;
import com.lorde0523.migration.analysis.model.ScreenAnalysis;
import com.lorde0523.migration.analysis.model.SearchParameterCandidate;
import com.lorde0523.migration.analysis.model.ServerMappingCandidate;
import com.lorde0523.migration.analysis.model.ServiceMatch;
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
import java.util.HashSet;
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
    private static final Pattern DOCTYPE = Pattern.compile("<!DOCTYPE[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Set<String> IBATIS_OPERATIONS = Set.of("select", "insert", "update", "delete");

    public MigrationAnalysisReport analyze(Path nexacroRoot, Path legacyServerRoot) {
        return analyze(new AnalysisRequest(
                nexacroRoot == null ? null : nexacroRoot.toString(),
                legacyServerRoot == null ? null : legacyServerRoot.toString()
        ));
    }

    public MigrationAnalysisReport analyze(AnalysisRequest request) {
        Path nexacroRoot = request.nexacroRoot() == null || request.nexacroRoot().isBlank()
                ? null
                : Path.of(request.nexacroRoot());
        Path legacyServerRoot = request.legacyServerRoot() == null || request.legacyServerRoot().isBlank()
                ? null
                : Path.of(request.legacyServerRoot());
        Path legacyJavaRoot = request.legacyJavaRoot() == null || request.legacyJavaRoot().isBlank()
                ? null
                : Path.of(request.legacyJavaRoot());
        Path legacyDbRoot = request.legacyDbRoot() == null || request.legacyDbRoot().isBlank()
                ? null
                : Path.of(request.legacyDbRoot());
        Path generationOutputRoot = request.generationOutputRoot() == null || request.generationOutputRoot().isBlank()
                ? Path.of("build/generated-migration")
                : Path.of(request.generationOutputRoot());
        String basePackage = request.basePackage() == null || request.basePackage().isBlank()
                ? "com.lorde0523.migration.generated"
                : request.basePackage();

        if (nexacroRoot == null || !Files.isDirectory(nexacroRoot)) {
            throw new IllegalArgumentException("nexacroRoot must be an existing directory");
        }

        List<NexcoreServiceMethod> nexcoreServices = extractNexcoreServices(legacyServerRoot);
        List<ScreenAnalysis> screens = findFiles(nexacroRoot, ".xfdl").stream()
                .map(path -> analyzeScreen(nexacroRoot, path, legacyServerRoot, nexcoreServices))
                .sorted(Comparator.comparing(ScreenAnalysis::screenId))
                .toList();
        List<LegacyJavaClass> legacyJavaClasses = parseLegacyJavaClasses(legacyJavaRoot);
        List<IbatisSqlMap> ibatisSqlMaps = parseIbatisSqlMaps(legacyDbRoot);
        List<MigrationUnit> migrationUnits = buildMigrationUnits(legacyJavaClasses, ibatisSqlMaps);
        List<GeneratedFile> generatedFiles = generateMigrationOutput(
                generationOutputRoot,
                basePackage,
                migrationUnits,
                ibatisSqlMaps
        );

        return new MigrationAnalysisReport(
                Instant.now(),
                nexacroRoot.toAbsolutePath().normalize().toString(),
                legacyServerRoot == null ? null : legacyServerRoot.toAbsolutePath().normalize().toString(),
                legacyJavaRoot == null ? null : legacyJavaRoot.toAbsolutePath().normalize().toString(),
                legacyDbRoot == null ? null : legacyDbRoot.toAbsolutePath().normalize().toString(),
                nexcoreServices,
                screens,
                migrationUnits,
                ibatisSqlMaps,
                generatedFiles
        );
    }

    private ScreenAnalysis analyzeScreen(
            Path nexacroRoot,
            Path xfdlPath,
            Path legacyServerRoot,
            List<NexcoreServiceMethod> nexcoreServices
    ) {
        String xfdlContent = read(xfdlPath);
        String screenId = extractScreenId(xfdlContent).orElse(stripExtension(xfdlPath.getFileName().toString()));
        Path scriptPath = siblingScriptPath(xfdlPath);
        String scriptContent = xfdlContent + "\n" + (Files.exists(scriptPath) ? read(scriptPath) : "");

        List<DatasetSpec> datasets = parseDatasets(xfdlContent);
        List<TransactionSpec> transactions = parseTransactions(scriptContent);
        List<SearchParameterCandidate> searchParameters = parseSearchParameters(scriptContent);
        List<ServerMappingCandidate> mappings = mapLegacySources(transactions, legacyServerRoot);
        List<ServiceMatch> serviceMatches = matchServices(transactions, nexcoreServices);
        List<DtoCandidate> dtoCandidates = generateDtoCandidates(screenId, transactions, datasets);
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
                serviceMatches,
                dtoCandidates,
                endpoints
        );
    }

    private List<NexcoreServiceMethod> extractNexcoreServices(Path legacyServerRoot) {
        if (legacyServerRoot == null || !Files.isDirectory(legacyServerRoot)) {
            return List.of();
        }

        List<NexcoreServiceMethod> methods = new ArrayList<>();
        for (Path path : findFiles(legacyServerRoot, ".xml")) {
            String content = read(path);
            if (!content.contains("<bizUnit") || !content.contains("<transactionId>")) {
                continue;
            }
            methods.addAll(parseBizUnitMethods(legacyServerRoot, path, content));
        }
        return List.copyOf(methods);
    }

    private List<LegacyJavaClass> parseLegacyJavaClasses(Path legacyJavaRoot) {
        if (legacyJavaRoot == null || !Files.isDirectory(legacyJavaRoot)) {
            return List.of();
        }

        List<LegacyJavaClass> classes = new ArrayList<>();
        for (Path path : findFiles(legacyJavaRoot, ".java")) {
            try {
                CompilationUnit unit = StaticJavaParser.parse(path);
                String packageName = unit.getPackageDeclaration()
                        .map(declaration -> declaration.getName().asString())
                        .orElse("");
                for (ClassOrInterfaceDeclaration declaration : unit.findAll(ClassOrInterfaceDeclaration.class)) {
                    if (declaration.isInterface()) {
                        continue;
                    }
                    String className = declaration.getNameAsString();
                    List<LegacyJavaMethod> publicMethods = declaration.getMethods().stream()
                            .filter(MethodDeclaration::isPublic)
                            .map(this::toLegacyJavaMethod)
                            .toList();
                    classes.add(new LegacyJavaClass(
                            packageName,
                            className,
                            legacyJavaRoot.relativize(path).toString().replace('\\', '/'),
                            toLegacyLayer(className),
                            publicMethods
                    ));
                }
            } catch (Exception ignored) {
                // Legacy Nexcore sources can contain framework-specific syntax or encodings.
                // The report remains useful with the parsable subset.
            }
        }
        return List.copyOf(classes);
    }

    private LegacyJavaMethod toLegacyJavaMethod(MethodDeclaration method) {
        List<String> calls = method.findAll(MethodCallExpr.class).stream()
                .map(call -> call.getName().asString())
                .distinct()
                .toList();
        List<String> statementIds = method.findAll(StringLiteralExpr.class).stream()
                .map(StringLiteralExpr::asString)
                .filter(value -> value.contains("."))
                .distinct()
                .toList();
        return new LegacyJavaMethod(method.getNameAsString(), calls, statementIds);
    }

    private LegacyLayer toLegacyLayer(String className) {
        if (className == null || className.isBlank()) {
            return LegacyLayer.UNKNOWN;
        }
        return switch (className.charAt(0)) {
            case 'P' -> LegacyLayer.CONTROLLER;
            case 'F' -> LegacyLayer.SERVICE;
            case 'D' -> LegacyLayer.DATA;
            default -> LegacyLayer.UNKNOWN;
        };
    }

    private List<IbatisSqlMap> parseIbatisSqlMaps(Path legacyDbRoot) {
        if (legacyDbRoot == null || !Files.isDirectory(legacyDbRoot)) {
            return List.of();
        }

        List<IbatisSqlMap> sqlMaps = new ArrayList<>();
        for (Path path : findFiles(legacyDbRoot, ".xml")) {
            String content = DOCTYPE.matcher(read(path)).replaceAll("");
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
                factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                factory.setExpandEntityReferences(false);
                Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(content)));
                Element sqlMap = document.getDocumentElement();
                if (sqlMap == null || !"sqlMap".equals(sqlMap.getTagName())) {
                    continue;
                }
                String namespace = sqlMap.getAttribute("namespace");
                List<IbatisStatement> statements = new ArrayList<>();
                for (String operation : IBATIS_OPERATIONS) {
                    NodeList nodes = sqlMap.getElementsByTagName(operation);
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Element statement = (Element) nodes.item(i);
                        String id = statement.getAttribute("id");
                        String statementId = namespace.isBlank() ? id : namespace + "." + id;
                        statements.add(new IbatisStatement(
                                namespace,
                                id,
                                statementId,
                                operation,
                                statement.getAttribute("parameterClass"),
                                statement.getAttribute("resultClass"),
                                statement.getAttribute("resultMap"),
                                toMyBatisSql(statement.getTextContent())
                        ));
                    }
                }
                sqlMaps.add(new IbatisSqlMap(
                        namespace,
                        legacyDbRoot.relativize(path).toString().replace('\\', '/'),
                        List.copyOf(statements)
                ));
            } catch (Exception ignored) {
                // Keep scanning other SQL maps; unparsable files will be left for manual migration.
            }
        }
        return List.copyOf(sqlMaps);
    }

    private List<MigrationUnit> buildMigrationUnits(
            List<LegacyJavaClass> legacyJavaClasses,
            List<IbatisSqlMap> ibatisSqlMaps
    ) {
        if (legacyJavaClasses.isEmpty()) {
            return List.of();
        }

        Map<String, LegacyJavaClass> classesByName = new LinkedHashMap<>();
        for (LegacyJavaClass legacyClass : legacyJavaClasses) {
            classesByName.put(legacyClass.className(), legacyClass);
        }
        Map<String, IbatisStatement> statementsById = new LinkedHashMap<>();
        for (IbatisSqlMap sqlMap : ibatisSqlMaps) {
            for (IbatisStatement statement : sqlMap.statements()) {
                statementsById.put(statement.statementId(), statement);
            }
        }

        List<MigrationUnit> units = new ArrayList<>();
        for (LegacyJavaClass controller : legacyJavaClasses.stream()
                .filter(candidate -> candidate.layer() == LegacyLayer.CONTROLLER)
                .sorted(Comparator.comparing(LegacyJavaClass::className))
                .toList()) {
            String baseName = stripLayerPrefix(controller.className());
            List<LegacyJavaClass> serviceSources = matchingClasses(legacyJavaClasses, LegacyLayer.SERVICE, baseName);
            List<LegacyJavaClass> dataSources = matchingClasses(legacyJavaClasses, LegacyLayer.DATA, baseName);
            List<LegacyMethodFlow> flows = buildMethodFlows(controller, serviceSources, dataSources);
            List<DataAccessDecision> decisions = buildDataAccessDecisions(dataSources);
            List<String> warnings = buildWarnings(dataSources, statementsById);
            List<String> mapperNames = dataSources.stream()
                    .map(dataSource -> stripLayerPrefix(dataSource.className()) + "Mapper")
                    .distinct()
                    .toList();
            units.add(new MigrationUnit(
                    baseName + "Controller",
                    baseName + "Service",
                    mapperNames,
                    controller,
                    serviceSources,
                    dataSources,
                    flows,
                    decisions,
                    warnings
            ));
        }
        return List.copyOf(units);
    }

    private List<LegacyJavaClass> matchingClasses(
            List<LegacyJavaClass> legacyJavaClasses,
            LegacyLayer layer,
            String baseName
    ) {
        return legacyJavaClasses.stream()
                .filter(candidate -> candidate.layer() == layer)
                .filter(candidate -> stripLayerPrefix(candidate.className()).equals(baseName))
                .sorted(Comparator.comparing(LegacyJavaClass::className))
                .toList();
    }

    private List<LegacyMethodFlow> buildMethodFlows(
            LegacyJavaClass controller,
            List<LegacyJavaClass> services,
            List<LegacyJavaClass> dataSources
    ) {
        List<LegacyMethodFlow> flows = new ArrayList<>();
        for (LegacyJavaMethod controllerMethod : controller.publicMethods()) {
            List<LegacyJavaMethod> serviceMethods = methodsCalledBy(controllerMethod, services);
            List<LegacyJavaMethod> dataMethods = serviceMethods.stream()
                    .flatMap(serviceMethod -> methodsCalledBy(serviceMethod, dataSources).stream())
                    .distinct()
                    .toList();
            List<String> statementIds = dataMethods.stream()
                    .flatMap(dataMethod -> dataMethod.statementIds().stream())
                    .distinct()
                    .toList();
            flows.add(new LegacyMethodFlow(
                    controllerMethod.name(),
                    serviceMethods.stream().map(LegacyJavaMethod::name).distinct().toList(),
                    dataMethods.stream().map(LegacyJavaMethod::name).distinct().toList(),
                    statementIds
            ));
        }
        return List.copyOf(flows);
    }

    private List<LegacyJavaMethod> methodsCalledBy(
            LegacyJavaMethod source,
            List<LegacyJavaClass> targetClasses
    ) {
        Set<String> calledNames = new HashSet<>(source.calledMethodNames());
        return targetClasses.stream()
                .flatMap(targetClass -> targetClass.publicMethods().stream())
                .filter(method -> calledNames.contains(method.name()))
                .toList();
    }

    private List<DataAccessDecision> buildDataAccessDecisions(List<LegacyJavaClass> dataSources) {
        List<DataAccessDecision> decisions = new ArrayList<>();
        for (LegacyJavaClass dataSource : dataSources) {
            for (LegacyJavaMethod method : dataSource.publicMethods()) {
                String decision = method.statementIds().isEmpty() ? "JPA_CANDIDATE" : "MYBATIS_REQUIRED";
                String reason = method.statementIds().isEmpty()
                        ? "No iBatis statement id was detected; verify whether this is simple CRUD before using JPA."
                        : "D method references iBatis statement ids and should preserve SQL semantics in MyBatis.";
                decisions.add(new DataAccessDecision(dataSource.className(), method.name(), decision, reason));
            }
        }
        return List.copyOf(decisions);
    }

    private List<String> buildWarnings(
            List<LegacyJavaClass> dataSources,
            Map<String, IbatisStatement> statementsById
    ) {
        List<String> warnings = new ArrayList<>();
        for (LegacyJavaClass dataSource : dataSources) {
            for (LegacyJavaMethod method : dataSource.publicMethods()) {
                for (String statementId : method.statementIds()) {
                    if (!statementsById.containsKey(statementId)) {
                        warnings.add("Manual check required: iBatis statement "
                                + statementId
                                + " was referenced by "
                                + dataSource.className()
                                + "."
                                + method.name()
                                + " but not found under legacyDbRoot.");
                    }
                }
            }
        }
        return List.copyOf(warnings);
    }

    private List<GeneratedFile> generateMigrationOutput(
            Path generationOutputRoot,
            String basePackage,
            List<MigrationUnit> migrationUnits,
            List<IbatisSqlMap> ibatisSqlMaps
    ) {
        if (migrationUnits.isEmpty()) {
            return List.of();
        }

        Map<String, IbatisStatement> statementsById = new LinkedHashMap<>();
        for (IbatisSqlMap sqlMap : ibatisSqlMaps) {
            for (IbatisStatement statement : sqlMap.statements()) {
                statementsById.put(statement.statementId(), statement);
            }
        }

        List<GeneratedFile> generatedFiles = new ArrayList<>();
        try {
            for (MigrationUnit unit : migrationUnits) {
                generatedFiles.add(writeGeneratedJava(
                        generationOutputRoot,
                        basePackage + ".controller",
                        "controller",
                        unit.controllerClassName(),
                        renderController(basePackage, unit)
                ));
                generatedFiles.add(writeGeneratedJava(
                        generationOutputRoot,
                        basePackage + ".service",
                        "service",
                        unit.serviceClassName(),
                        renderService(basePackage, unit)
                ));
                for (String mapperName : unit.dataAccessClassNames()) {
                    generatedFiles.add(writeGeneratedJava(
                            generationOutputRoot,
                            basePackage + ".mapper",
                            "mapper",
                            mapperName,
                            renderMapperInterface(basePackage, unit, mapperName)
                    ));
                    generatedFiles.add(writeGeneratedResource(
                            generationOutputRoot,
                            "mapper",
                            mapperName + ".xml",
                            renderMapperXml(basePackage, unit, mapperName, statementsById)
                    ));
                }
            }
            generatedFiles.add(writeGeneratedRootFile(
                    generationOutputRoot,
                    "migration-report.md",
                    renderGeneratedMigrationReport(migrationUnits)
            ));
            return List.copyOf(generatedFiles);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate migration output under " + generationOutputRoot, e);
        }
    }

    private GeneratedFile writeGeneratedJava(
            Path generationOutputRoot,
            String packageName,
            String role,
            String className,
            String content
    ) throws Exception {
        Path packagePath = generationOutputRoot
                .resolve("src/main/java")
                .resolve(packageName.replace('.', '/'));
        Files.createDirectories(packagePath);
        Path file = packagePath.resolve(className + ".java");
        Files.writeString(file, content);
        return new GeneratedFile(role, generationOutputRoot.relativize(file).toString().replace('\\', '/'));
    }

    private GeneratedFile writeGeneratedResource(
            Path generationOutputRoot,
            String folder,
            String fileName,
            String content
    ) throws Exception {
        Path resourcePath = generationOutputRoot.resolve("src/main/resources");
        if (!folder.isBlank()) {
            resourcePath = resourcePath.resolve(folder);
        }
        Files.createDirectories(resourcePath);
        Path file = resourcePath.resolve(fileName);
        Files.writeString(file, content);
        return new GeneratedFile("resource", generationOutputRoot.relativize(file).toString().replace('\\', '/'));
    }

    private GeneratedFile writeGeneratedRootFile(
            Path generationOutputRoot,
            String fileName,
            String content
    ) throws Exception {
        Files.createDirectories(generationOutputRoot);
        Path file = generationOutputRoot.resolve(fileName);
        Files.writeString(file, content);
        return new GeneratedFile("report", generationOutputRoot.relativize(file).toString().replace('\\', '/'));
    }

    private String renderController(String basePackage, MigrationUnit unit) {
        StringBuilder methods = new StringBuilder();
        for (LegacyMethodFlow flow : unit.methodFlows()) {
            methods.append("""

                    @PostMapping("/%s")
                    public Object %s(@RequestBody(required = false) Object request) {
                        return service.%s(request);
                    }
                    """.formatted(toKebabCase(flow.controllerMethodName()), flow.controllerMethodName(), flow.controllerMethodName()));
        }
        return """
                package %s.controller;

                import %s.service.%s;
                import org.springframework.web.bind.annotation.PostMapping;
                import org.springframework.web.bind.annotation.RequestBody;
                import org.springframework.web.bind.annotation.RequestMapping;
                import org.springframework.web.bind.annotation.RestController;

                @RestController
                @RequestMapping("/api/%s")
                public class %s {
                    private final %s service;

                    public %s(%s service) {
                        this.service = service;
                    }
                %s
                }
                """.formatted(
                basePackage,
                basePackage,
                unit.serviceClassName(),
                toKebabCase(stripSuffix(unit.controllerClassName(), "Controller")),
                unit.controllerClassName(),
                unit.serviceClassName(),
                unit.controllerClassName(),
                unit.serviceClassName(),
                methods
        );
    }

    private String renderService(String basePackage, MigrationUnit unit) {
        String mapperName = unit.dataAccessClassNames().isEmpty() ? null : unit.dataAccessClassNames().get(0);
        StringBuilder methods = new StringBuilder();
        for (LegacyMethodFlow flow : unit.methodFlows()) {
            String targetMethod = flow.dataMethodNames().isEmpty()
                    ? "return request;"
                    : "return mapper." + flow.dataMethodNames().get(0) + "(request);";
            methods.append("""

                    public Object %s(Object request) {
                        %s
                    }
                    """.formatted(flow.controllerMethodName(), targetMethod));
        }
        if (mapperName == null) {
            return """
                    package %s.service;

                    import org.springframework.stereotype.Service;

                    @Service
                    public class %s {
                    %s
                    }
                    """.formatted(basePackage, unit.serviceClassName(), methods);
        }
        return """
                package %s.service;

                import %s.mapper.%s;
                import org.springframework.stereotype.Service;

                @Service
                public class %s {
                    private final %s mapper;

                    public %s(%s mapper) {
                        this.mapper = mapper;
                    }
                %s
                }
                """.formatted(
                basePackage,
                basePackage,
                mapperName,
                unit.serviceClassName(),
                mapperName,
                unit.serviceClassName(),
                mapperName,
                methods
        );
    }

    private String renderMapperInterface(String basePackage, MigrationUnit unit, String mapperName) {
        StringBuilder methods = new StringBuilder();
        for (LegacyMethodFlow flow : unit.methodFlows()) {
            for (String dataMethodName : flow.dataMethodNames()) {
                methods.append("    Object ").append(dataMethodName).append("(Object parameter);\n");
            }
        }
        return """
                package %s.mapper;

                import org.apache.ibatis.annotations.Mapper;

                @Mapper
                public interface %s {
                %s}
                """.formatted(basePackage, mapperName, methods);
    }

    private String renderMapperXml(
            String basePackage,
            MigrationUnit unit,
            String mapperName,
            Map<String, IbatisStatement> statementsById
    ) {
        StringBuilder statements = new StringBuilder();
        Map<String, String> methodByStatementId = new LinkedHashMap<>();
        for (LegacyMethodFlow flow : unit.methodFlows()) {
            for (String statementId : flow.statementIds()) {
                String methodName = flow.dataMethodNames().isEmpty()
                        ? statementId.substring(statementId.lastIndexOf('.') + 1)
                        : flow.dataMethodNames().get(0);
                methodByStatementId.put(statementId, methodName);
            }
        }
        for (Map.Entry<String, String> entry : methodByStatementId.entrySet()) {
            IbatisStatement statement = statementsById.get(entry.getKey());
            if (statement == null) {
                continue;
            }
            String tag = statement.operation();
            statements.append("    <").append(tag)
                    .append(" id=\"").append(entry.getValue()).append("\"")
                    .append(" parameterType=\"").append(toMyBatisType(statement.parameterClassName())).append("\"");
            if ("select".equals(tag)) {
                statements.append(" resultType=\"").append(toMyBatisType(statement.resultClassName())).append("\"");
            }
            statements.append(">\n")
                    .append(indentSql(statement.sql()))
                    .append("\n    </").append(tag).append(">\n");
        }
        return """
                <?xml version="1.0" encoding="UTF-8" ?>
                <!DOCTYPE mapper
                  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
                  "https://mybatis.org/dtd/mybatis-3-mapper.dtd">
                <mapper namespace="%s.mapper.%s">
                %s</mapper>
                """.formatted(basePackage, mapperName, statements);
    }

    private String renderGeneratedMigrationReport(List<MigrationUnit> migrationUnits) {
        StringBuilder markdown = new StringBuilder("# Nexcore Spring Boot Migration Report\n\n");
        for (MigrationUnit unit : migrationUnits) {
            markdown.append("## ").append(unit.controllerClassName()).append("\n\n");
            markdown.append("- Service: `").append(unit.serviceClassName()).append("`\n");
            markdown.append("- Data Access: `").append(String.join(", ", unit.dataAccessClassNames())).append("`\n");
            for (LegacyMethodFlow flow : unit.methodFlows()) {
                markdown.append("- Flow: `").append(flow.controllerMethodName()).append("` -> `")
                        .append(String.join(", ", flow.serviceMethodNames())).append("` -> `")
                        .append(String.join(", ", flow.dataMethodNames())).append("`");
                if (!flow.statementIds().isEmpty()) {
                    markdown.append(" statements `").append(String.join(", ", flow.statementIds())).append("`");
                }
                markdown.append('\n');
            }
            for (DataAccessDecision decision : unit.dataAccessDecisions()) {
                markdown.append("- DB Decision: `").append(decision.dataClassName()).append(".")
                        .append(decision.dataMethodName()).append("` -> `")
                        .append(decision.decision()).append("` (")
                        .append(decision.reason()).append(")\n");
            }
            for (String warning : unit.warnings()) {
                markdown.append("- Warning: ").append(warning).append('\n');
            }
            markdown.append('\n');
        }
        return markdown.toString();
    }

    private List<NexcoreServiceMethod> parseBizUnitMethods(Path legacyServerRoot, Path path, String content) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);
            Document document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(content)));
            NodeList bizUnits = document.getElementsByTagName("bizUnit");
            List<NexcoreServiceMethod> methods = new ArrayList<>();

            for (int i = 0; i < bizUnits.getLength(); i++) {
                Element bizUnit = (Element) bizUnits.item(i);
                String bizUnitId = bizUnit.getAttribute("id");
                String bizUnitName = firstText(bizUnit, "bizUnitName");
                String componentId = firstText(bizUnit, "componentId");
                NodeList methodNodes = bizUnit.getElementsByTagName("method");
                for (int j = 0; j < methodNodes.getLength(); j++) {
                    Element method = (Element) methodNodes.item(j);
                    String transactionId = firstText(method, "transactionId");
                    if (transactionId.isBlank()) {
                        continue;
                    }
                    methods.add(new NexcoreServiceMethod(
                            bizUnitId,
                            bizUnitName,
                            componentId,
                            firstText(method, "methodId"),
                            firstText(method, "methodName"),
                            transactionId,
                            legacyServerRoot.relativize(path).toString().replace('\\', '/')
                    ));
                }
            }
            return methods;
        } catch (Exception ignored) {
            return List.of();
        }
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

    private List<ServiceMatch> matchServices(
            List<TransactionSpec> transactions,
            List<NexcoreServiceMethod> nexcoreServices
    ) {
        Map<String, NexcoreServiceMethod> byTransactionId = new LinkedHashMap<>();
        for (NexcoreServiceMethod service : nexcoreServices) {
            byTransactionId.put(service.transactionId(), service);
        }

        List<ServiceMatch> matches = new ArrayList<>();
        for (TransactionSpec transaction : transactions) {
            String transactionId = normalizeServiceExpression(transaction.serviceUrl());
            NexcoreServiceMethod service = byTransactionId.get(transactionId);
            matches.add(new ServiceMatch(
                    transaction.name(),
                    transactionId,
                    service != null,
                    service == null ? "" : service.bizUnitId(),
                    service == null ? "" : service.methodId(),
                    service == null ? "" : service.componentId(),
                    service == null ? "" : service.filePath()
            ));
        }
        return List.copyOf(matches);
    }

    private List<DtoCandidate> generateDtoCandidates(
            String screenId,
            List<TransactionSpec> transactions,
            List<DatasetSpec> datasets
    ) {
        Map<String, DatasetSpec> datasetByName = new LinkedHashMap<>();
        for (DatasetSpec dataset : datasets) {
            datasetByName.put(dataset.name(), dataset);
        }

        List<DtoCandidate> candidates = new ArrayList<>();
        for (TransactionSpec transaction : transactions) {
            for (String datasetName : transaction.inputDatasets().values()) {
                addDtoCandidate(candidates, screenId, transaction, datasetByName.get(datasetName), "Request");
            }
            for (String datasetName : transaction.outputDatasets().values()) {
                addDtoCandidate(candidates, screenId, transaction, datasetByName.get(datasetName), "Response");
            }
        }
        return List.copyOf(candidates);
    }

    private void addDtoCandidate(
            List<DtoCandidate> candidates,
            String screenId,
            TransactionSpec transaction,
            DatasetSpec dataset,
            String direction
    ) {
        if (dataset == null) {
            return;
        }

        List<DtoFieldCandidate> fields = dataset.columns().stream()
                .map(column -> new DtoFieldCandidate(
                        toCamelCase(column.name()),
                        column.name(),
                        toJavaType(column.type()),
                        column.type(),
                        column.size()
                ))
                .toList();
        candidates.add(new DtoCandidate(
                toPascalCase(screenId) + toPascalCase(transaction.name()) + direction,
                dataset.name(),
                direction,
                fields
        ));
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
        String path = normalizeServiceExpression(transaction.serviceUrl());
        if (path == null || path.isBlank()) {
            path = "/" + transaction.name();
        }

        path = path.replaceAll("\\.do$", "")
                .replace('.', '/')
                .replace('_', '/')
                .replace('\\', '/')
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

    private String firstText(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0 || nodes.item(0).getTextContent() == null) {
            return "";
        }
        return nodes.item(0).getTextContent().trim();
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

    private String normalizeServiceExpression(String value) {
        String normalized = unquote(value);
        int separator = normalized.indexOf("::");
        if (separator >= 0) {
            normalized = normalized.substring(separator + 2);
        }
        return normalized.trim();
    }

    private String toJavaType(String nexacroType) {
        if (nexacroType == null) {
            return "String";
        }
        return switch (nexacroType.toUpperCase(Locale.ROOT)) {
            case "INT", "INTEGER" -> "Integer";
            case "LONG", "BIGDECIMAL", "FLOAT", "DOUBLE", "DECIMAL" -> "java.math.BigDecimal";
            case "DATE", "DATETIME", "TIME" -> "java.time.LocalDateTime";
            case "BOOLEAN", "BOOL" -> "Boolean";
            default -> "String";
        };
    }

    private String toMyBatisSql(String sql) {
        if (sql == null) {
            return "";
        }
        return Pattern.compile("#([A-Za-z0-9_]+)#").matcher(sql.trim()).replaceAll("#{$1}");
    }

    private String toMyBatisType(String className) {
        if (className == null || className.isBlank()) {
            return "java.util.Map";
        }
        return switch (className) {
            case "map", "java.util.HashMap" -> "java.util.Map";
            default -> className;
        };
    }

    private String indentSql(String sql) {
        if (sql == null || sql.isBlank()) {
            return "        ";
        }
        return "        " + sql.trim().replace("\r\n", "\n").replace("\n", "\n        ");
    }

    private String stripLayerPrefix(String className) {
        if (className == null || className.length() < 2) {
            return Objects.requireNonNullElse(className, "Generated");
        }
        char first = className.charAt(0);
        if (first == 'P' || first == 'F' || first == 'D') {
            return toPascalCase(className.substring(1));
        }
        return toPascalCase(className);
    }

    private String stripSuffix(String value, String suffix) {
        if (value != null && value.endsWith(suffix)) {
            return value.substring(0, value.length() - suffix.length());
        }
        return Objects.requireNonNullElse(value, "");
    }

    private String toKebabCase(String value) {
        if (value == null || value.isBlank()) {
            return "generated";
        }
        return value
                .replaceAll("([a-z0-9])([A-Z])", "$1-$2")
                .replaceAll("[^A-Za-z0-9]+", "-")
                .replaceAll("^-|-$", "")
                .toLowerCase(Locale.ROOT);
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

        String[] parts = value
                .replaceAll("([a-z0-9])([A-Z])", "$1 $2")
                .replaceAll("[^A-Za-z0-9]+", " ")
                .split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            String normalized = part.toLowerCase(Locale.ROOT);
            result.append(normalized.substring(0, 1).toUpperCase(Locale.ROOT));
            if (normalized.length() > 1) {
                result.append(normalized.substring(1));
            }
        }
        return Objects.requireNonNullElse(result.toString(), "Generated");
    }

    private String toCamelCase(String value) {
        String pascal = toPascalCase(value);
        if (pascal.isBlank()) {
            return pascal;
        }
        return pascal.substring(0, 1).toLowerCase(Locale.ROOT) + pascal.substring(1);
    }
}
