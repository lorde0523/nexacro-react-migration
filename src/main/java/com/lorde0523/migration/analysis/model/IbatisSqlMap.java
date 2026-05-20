package com.lorde0523.migration.analysis.model;

import java.util.List;

public record IbatisSqlMap(
        String namespace,
        String filePath,
        List<IbatisStatement> statements
) {
}
