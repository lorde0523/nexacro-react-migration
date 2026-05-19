package com.lorde0523.migration.analysis;

import com.lorde0523.migration.analysis.model.AnalysisRequest;
import com.lorde0523.migration.analysis.model.MigrationAnalysisReport;
import com.lorde0523.migration.analysis.parser.NexacroContractExtractor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class NexacroAnalysisService {

    private final NexacroContractExtractor extractor;

    public NexacroAnalysisService() {
        this(new NexacroContractExtractor());
    }

    NexacroAnalysisService(NexacroContractExtractor extractor) {
        this.extractor = extractor;
    }

    public MigrationAnalysisReport analyze(AnalysisRequest request) {
        Path nexacroRoot = Path.of(request.nexacroRoot());
        Path legacyServerRoot = request.legacyServerRoot() == null || request.legacyServerRoot().isBlank()
                ? null
                : Path.of(request.legacyServerRoot());
        return extractor.analyze(nexacroRoot, legacyServerRoot);
    }
}
