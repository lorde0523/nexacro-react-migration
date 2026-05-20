package com.lorde0523.migration.analysis.model;

public record DataAccessDecision(
        String dataClassName,
        String dataMethodName,
        String decision,
        String reason
) {
}
