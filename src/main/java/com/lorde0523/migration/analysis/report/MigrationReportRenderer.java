package com.lorde0523.migration.analysis.report;

import com.lorde0523.migration.analysis.model.ApiEndpointCandidate;
import com.lorde0523.migration.analysis.model.DtoCandidate;
import com.lorde0523.migration.analysis.model.MigrationAnalysisReport;
import com.lorde0523.migration.analysis.model.ScreenAnalysis;
import com.lorde0523.migration.analysis.model.SearchParameterCandidate;
import com.lorde0523.migration.analysis.model.ServerMappingCandidate;
import com.lorde0523.migration.analysis.model.ServiceMatch;
import com.lorde0523.migration.analysis.model.TransactionSpec;

import java.util.stream.Collectors;

public final class MigrationReportRenderer {

    private MigrationReportRenderer() {
    }

    public static String toMarkdown(MigrationAnalysisReport report) {
        StringBuilder markdown = new StringBuilder();
        markdown.append("# Nexacro Migration API Candidates\n\n");
        markdown.append("- Generated At: ").append(report.generatedAt()).append('\n');
        markdown.append("- Nexacro Root: ").append(report.nexacroRoot()).append('\n');
        markdown.append("- Legacy Server Root: ").append(nullToBlank(report.legacyServerRoot())).append("\n\n");
        markdown.append("| Screen ID | File | Transaction | Service ID | Match | Input Dataset | Output Dataset | DTO Candidates | Search Parameters | Legacy Mapping | Endpoint |\n");
        markdown.append("| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |\n");

        for (ScreenAnalysis screen : report.screens()) {
            if (screen.transactions().isEmpty()) {
                appendMarkdownRow(markdown, screen, null);
                continue;
            }
            for (TransactionSpec transaction : screen.transactions()) {
                appendMarkdownRow(markdown, screen, transaction);
            }
        }

        return markdown.toString();
    }

    public static String toCsv(MigrationAnalysisReport report) {
        StringBuilder csv = new StringBuilder();
        csv.append("screen_id,file_path,transaction,service_id,match,input_dataset,output_dataset,dto_candidates,search_parameters,legacy_mapping,endpoint\n");

        for (ScreenAnalysis screen : report.screens()) {
            if (screen.transactions().isEmpty()) {
                appendCsvRow(csv, screen, null);
                continue;
            }
            for (TransactionSpec transaction : screen.transactions()) {
                appendCsvRow(csv, screen, transaction);
            }
        }

        return csv.toString();
    }

    private static void appendMarkdownRow(StringBuilder markdown, ScreenAnalysis screen, TransactionSpec transaction) {
        markdown.append("| ")
                .append(escapeMarkdown(screen.screenId())).append(" | ")
                .append(escapeMarkdown(screen.filePath())).append(" | ")
                .append(escapeMarkdown(transaction == null ? "" : transaction.name())).append(" | ")
                .append(escapeMarkdown(transaction == null ? "" : transaction.serviceUrl())).append(" | ")
                .append(escapeMarkdown(joinMatches(screen, transaction))).append(" | ")
                .append(escapeMarkdown(transaction == null ? "" : transaction.inputDatasets().toString())).append(" | ")
                .append(escapeMarkdown(transaction == null ? "" : transaction.outputDatasets().toString())).append(" | ")
                .append(escapeMarkdown(joinDtos(screen))).append(" | ")
                .append(escapeMarkdown(joinSearchParameters(screen))).append(" | ")
                .append(escapeMarkdown(joinMappings(screen))).append(" | ")
                .append(escapeMarkdown(joinEndpoints(screen))).append(" |\n");
    }

    private static void appendCsvRow(StringBuilder csv, ScreenAnalysis screen, TransactionSpec transaction) {
        csv.append(csv(screen.screenId())).append(',')
                .append(csv(screen.filePath())).append(',')
                .append(csv(transaction == null ? "" : transaction.name())).append(',')
                .append(csv(transaction == null ? "" : transaction.serviceUrl())).append(',')
                .append(csv(joinMatches(screen, transaction))).append(',')
                .append(csv(transaction == null ? "" : transaction.inputDatasets().toString())).append(',')
                .append(csv(transaction == null ? "" : transaction.outputDatasets().toString())).append(',')
                .append(csv(joinDtos(screen))).append(',')
                .append(csv(joinSearchParameters(screen))).append(',')
                .append(csv(joinMappings(screen))).append(',')
                .append(csv(joinEndpoints(screen))).append('\n');
    }

    private static String joinMatches(ScreenAnalysis screen, TransactionSpec transaction) {
        return screen.serviceMatches().stream()
                .filter(match -> transaction == null || match.transactionName().equals(transaction.name()))
                .map(MigrationReportRenderer::formatMatch)
                .collect(Collectors.joining("; "));
    }

    private static String formatMatch(ServiceMatch match) {
        if (!match.matched()) {
            return "NOT_FOUND:" + match.transactionId();
        }
        return "MATCHED:" + match.transactionId() + "@" + match.bizUnitId() + "." + match.methodId();
    }

    private static String joinDtos(ScreenAnalysis screen) {
        return screen.dtoCandidates().stream()
                .map(DtoCandidate::name)
                .distinct()
                .collect(Collectors.joining("; "));
    }

    private static String joinSearchParameters(ScreenAnalysis screen) {
        return screen.searchParameters().stream()
                .map(SearchParameterCandidate::name)
                .distinct()
                .collect(Collectors.joining("; "));
    }

    private static String joinMappings(ScreenAnalysis screen) {
        return screen.serverMappings().stream()
                .map(ServerMappingCandidate::filePath)
                .distinct()
                .collect(Collectors.joining("; "));
    }

    private static String joinEndpoints(ScreenAnalysis screen) {
        return screen.endpointCandidates().stream()
                .map(ApiEndpointCandidate::path)
                .distinct()
                .collect(Collectors.joining("; "));
    }

    private static String csv(String value) {
        String safe = nullToBlank(value).replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    private static String escapeMarkdown(String value) {
        return nullToBlank(value).replace("|", "\\|").replace("\n", " ");
    }

    private static String nullToBlank(String value) {
        return value == null ? "" : value;
    }
}
