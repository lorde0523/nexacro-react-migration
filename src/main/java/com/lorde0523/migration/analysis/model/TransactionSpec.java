package com.lorde0523.migration.analysis.model;

import java.util.Map;

public record TransactionSpec(
        String name,
        String serviceUrl,
        Map<String, String> inputDatasets,
        Map<String, String> outputDatasets,
        String argumentsExpression,
        String callback
) {
}
