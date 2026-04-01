// [MIGRATION] AS-IS: Nexcore .xpc 서비스 정의 - 매출 서비스
// [TO-BE]: Spring @RestController

package com.migration.nexacro.controller;

import com.migration.nexacro.dto.SalesDTO;
import com.migration.nexacro.dto.SearchCondition;
import com.migration.nexacro.service.SalesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 매출 REST Controller (Nexcore .xpc 서비스 → @RestController)
 *
 * Nexacro Transaction 매핑:
 * - GET /api/sales         → 매출 목록 조회 (fn_search)
 * - GET /api/sales/summary → 매출 요약 (차트 데이터, Nexacro Dataset 바인딩 대응)
 */
@Slf4j
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    /**
     * 매출 목록 조회 (Nexacro fn_search 대응)
     */
    @GetMapping
    public ResponseEntity<List<SalesDTO>> getSalesList(SearchCondition condition) {
        log.info("매출 목록 조회 요청: {}", condition);
        List<SalesDTO> sales = salesService.getSalesList(condition);
        return ResponseEntity.ok(sales);
    }

    /**
     * 매출 요약 조회 (ECharts 차트 데이터용, Nexacro Dataset 바인딩 대응)
     * - Bar 차트: 분기별 매출
     * - Line 차트: 월별 매출 추이
     * - Pie 차트: 부서별 매출 비중
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSalesSummary(
            @RequestParam(required = false) Integer year) {
        log.info("매출 요약 조회 요청: year={}", year);
        if (year == null) {
            year = java.time.LocalDate.now().getYear();
        }
        Map<String, Object> summary = salesService.getSalesSummary(year);
        return ResponseEntity.ok(summary);
    }
}
