package com.lorde0523.migration.analysis.model;

import java.util.List;

public record DatasetSpec(
        String name,
        List<DatasetColumn> columns
) {
}
