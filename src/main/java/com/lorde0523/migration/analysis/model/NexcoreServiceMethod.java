package com.lorde0523.migration.analysis.model;

public record NexcoreServiceMethod(
        String bizUnitId,
        String bizUnitName,
        String componentId,
        String methodId,
        String methodName,
        String transactionId,
        String filePath
) {
}
