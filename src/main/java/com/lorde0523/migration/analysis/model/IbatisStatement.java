package com.lorde0523.migration.analysis.model;

public record IbatisStatement(
        String namespace,
        String id,
        String statementId,
        String operation,
        String parameterClassName,
        String resultClassName,
        String resultMap,
        String sql
) {
}
