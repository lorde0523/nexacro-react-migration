package com.lorde0523.migration.analysis.model;

import java.time.Instant;
import java.util.List;

public record MigrationAnalysisReport(
        Instant generatedAt,
        String nexacroRoot,
        String legacyServerRoot,
        List<NexcoreServiceMethod> nexcoreServices,
        List<ScreenAnalysis> screens
) {
}
