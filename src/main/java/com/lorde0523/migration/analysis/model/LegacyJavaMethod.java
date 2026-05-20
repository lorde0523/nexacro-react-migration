package com.lorde0523.migration.analysis.model;

import java.util.List;

public record LegacyJavaMethod(
        String name,
        List<String> calledMethodNames,
        List<String> statementIds
) {
}
