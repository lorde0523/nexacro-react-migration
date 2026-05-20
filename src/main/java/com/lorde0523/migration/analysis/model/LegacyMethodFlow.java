package com.lorde0523.migration.analysis.model;

import java.util.List;

public record LegacyMethodFlow(
        String controllerMethodName,
        List<String> serviceMethodNames,
        List<String> dataMethodNames,
        List<String> statementIds
) {
}
