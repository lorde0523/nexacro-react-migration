package com.lorde0523.migration.analysis.model;

public record DtoFieldCandidate(
        String name,
        String sourceColumn,
        String javaType,
        String nexacroType,
        String size
) {
}
