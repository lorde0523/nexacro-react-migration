package com.lorde0523.migration.analysis.model;

import jakarta.validation.constraints.NotBlank;

public record AnalysisRequest(
        @NotBlank String nexacroRoot,
        String legacyServerRoot
) {
}
