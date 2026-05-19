package com.lorde0523.migration.analysis.web;

import com.lorde0523.migration.analysis.NexacroAnalysisService;
import com.lorde0523.migration.analysis.model.AnalysisRequest;
import com.lorde0523.migration.analysis.model.MigrationAnalysisReport;
import com.lorde0523.migration.analysis.report.MigrationReportRenderer;
import com.lorde0523.migration.common.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/migration")
public class MigrationAnalysisController {

    private final NexacroAnalysisService analysisService;

    public MigrationAnalysisController(NexacroAnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/analyze")
    public ApiResponse<MigrationAnalysisReport> analyze(@Valid @RequestBody AnalysisRequest request) {
        return ApiResponse.success(analysisService.analyze(request));
    }

    @PostMapping(value = "/analyze/report", produces = MediaType.TEXT_PLAIN_VALUE)
    public String analyzeReport(
            @Valid @RequestBody AnalysisRequest request,
            @RequestParam(defaultValue = "markdown") String format
    ) {
        MigrationAnalysisReport report = analysisService.analyze(request);
        if ("csv".equalsIgnoreCase(format)) {
            return MigrationReportRenderer.toCsv(report);
        }
        return MigrationReportRenderer.toMarkdown(report);
    }
}
