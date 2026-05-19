package com.lorde0523.migration.analysis.model;

import java.util.List;

public record DtoCandidate(
        String name,
        String datasetName,
        String direction,
        List<DtoFieldCandidate> fields
) {
}
