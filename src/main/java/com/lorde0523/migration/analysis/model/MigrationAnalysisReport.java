package com.lorde0523.migration.analysis.model;

import java.time.Instant;
import java.util.List;

public record MigrationAnalysisReport(
        Instant generatedAt,
        String nexacroRoot,
        String legacyServerRoot,
        String legacyJavaRoot,
        String legacyDbRoot,
        List<NexcoreServiceMethod> nexcoreServices,
        List<ScreenAnalysis> screens,
        List<MigrationUnit> migrationUnits,
        List<IbatisSqlMap> ibatisSqlMaps,
        List<GeneratedFile> generatedFiles
) {
}
