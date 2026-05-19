package com.lorde0523.migration.analysis.model;

public record ServiceMatch(
        String transactionName,
        String transactionId,
        boolean matched,
        String bizUnitId,
        String methodId,
        String componentId,
        String filePath
) {
}
