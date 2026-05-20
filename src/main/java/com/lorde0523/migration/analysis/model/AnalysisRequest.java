package com.lorde0523.migration.analysis.model;

import jakarta.validation.constraints.NotBlank;

public record AnalysisRequest(
        @NotBlank String nexacroRoot,
        String legacyServerRoot,
        String legacyJavaRoot,
        String legacyDbRoot,
        String generationOutputRoot,
        String basePackage
) {
    public AnalysisRequest(String nexacroRoot, String legacyServerRoot) {
        this(nexacroRoot, legacyServerRoot, null, null, null, null);
    }
}
