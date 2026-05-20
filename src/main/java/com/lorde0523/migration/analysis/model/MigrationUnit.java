package com.lorde0523.migration.analysis.model;

import java.util.List;

public record MigrationUnit(
        String controllerClassName,
        String serviceClassName,
        List<String> dataAccessClassNames,
        LegacyJavaClass controllerSource,
        List<LegacyJavaClass> serviceSources,
        List<LegacyJavaClass> dataSources,
        List<LegacyMethodFlow> methodFlows,
        List<DataAccessDecision> dataAccessDecisions,
        List<String> warnings
) {
}
