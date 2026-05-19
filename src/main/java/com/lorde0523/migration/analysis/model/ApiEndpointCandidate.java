package com.lorde0523.migration.analysis.model;

public record ApiEndpointCandidate(
        String method,
        String path,
        String requestDtoName,
        String responseDtoName
) {
}
