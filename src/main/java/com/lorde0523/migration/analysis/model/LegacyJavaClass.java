package com.lorde0523.migration.analysis.model;

import java.util.List;

public record LegacyJavaClass(
        String packageName,
        String className,
        String filePath,
        LegacyLayer layer,
        List<LegacyJavaMethod> publicMethods
) {
}
