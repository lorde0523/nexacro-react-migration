package com.lorde0523.migration.analysis.model;

import java.util.List;

public record ScreenAnalysis(
        String screenId,
        String filePath,
        List<DatasetSpec> datasets,
        List<TransactionSpec> transactions,
        List<SearchParameterCandidate> searchParameters,
        List<ServerMappingCandidate> serverMappings,
        List<ApiEndpointCandidate> endpointCandidates
) {
}
