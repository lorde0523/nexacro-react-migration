package com.lorde0523.migration.analysis.model;

public record ServerMappingCandidate(
        String transactionName,
        String serviceUrl,
        String filePath,
        String reason
) {
}
