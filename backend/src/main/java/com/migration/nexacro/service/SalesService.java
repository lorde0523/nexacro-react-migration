// [MIGRATION] AS-IS: Nexcore Service - SalesService
// [TO-BE]: Spring @Service

package com.migration.nexacro.service;

import com.migration.nexacro.dto.SalesDTO;
import com.migration.nexacro.dto.SearchCondition;
import com.migration.nexacro.mapper.SalesMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 매출 서비스 (Nexcore Service → Spring @Service)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SalesService {

    private final SalesMapper salesMapper;

    /**
     * 매출 목록 조회 (Nexacro fn_search 대응)
     */
    @Transactional(readOnly = true)
    public List<SalesDTO> getSalesList(SearchCondition condition) {
        log.debug("매출 목록 조회: {}", condition);
        return salesMapper.findAll(condition);
    }

    /**
     * 매출 요약 조회 (차트 데이터용, Nexacro Dataset 바인딩 대응)
     * ECharts Bar/Line/Pie 차트용 데이터 구조로 변환
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getSalesSummary(Integer year) {
        log.debug("매출 요약 조회: year={}", year);

        List<Map<String, Object>> quarterSummary = salesMapper.findSummaryByYear(year);
        SearchCondition condition = new SearchCondition();
        condition.setYear(year);
        List<Map<String, Object>> deptSummary = salesMapper.findSummaryByDept(condition);

        Map<String, Object> result = new HashMap<>();

        // 분기 데이터 (Bar 차트용 - Nexacro BarChart → type: 'bar')
        List<String> quarters = new ArrayList<>();
        List<BigDecimal> barData = new ArrayList<>();
        for (Map<String, Object> row : quarterSummary) {
            quarters.add(row.get("quarter") + "분기");
            barData.add((BigDecimal) row.get("total_amount"));
        }
        result.put("quarters", quarters);
        result.put("barData", barData);

        // 월별 더미 라인 데이터 (Line 차트용 - Nexacro LineChart → type: 'line')
        result.put("months", List.of("1월","2월","3월","4월","5월","6월","7월","8월","9월","10월","11월","12월"));
        result.put("lineData", List.of(12000000,15000000,18000000,20000000,22000000,25000000,
                                        24000000,27000000,30000000,28000000,32000000,35000000));
        result.put("prevLineData", List.of(10000000,12000000,14000000,16000000,18000000,20000000,
                                            19000000,22000000,24000000,23000000,26000000,28000000));

        // 부서별 파이 차트 데이터 (Pie 차트용 - Nexacro PieChart → type: 'pie')
        List<Map<String, Object>> pieData = new ArrayList<>();
        for (Map<String, Object> row : deptSummary) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", row.get("dept_name"));
            item.put("value", row.get("total_amount"));
            pieData.add(item);
        }
        result.put("pieData", pieData);

        return result;
    }
}
