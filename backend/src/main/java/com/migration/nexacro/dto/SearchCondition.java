// [MIGRATION] AS-IS: Nexacro 검색 Dataset (공통 검색 조건)
// [TO-BE]: Spring Boot 공통 검색 조건 DTO

package com.migration.nexacro.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 공통 검색 조건 DTO (Nexacro 검색 Dataset 대응)
 */
@Data
@NoArgsConstructor
public class SearchCondition {
    // 공통 검색 조건
    private String keyword;
    private String startDate;
    private String endDate;
    private String useYn;
    private Integer page;
    private Integer size;

    // 사원 검색 조건
    private String empName;
    private String deptCode;
    private String hireStartDate;
    private String hireEndDate;
    private String status;

    // 게시판 검색 조건
    private String title;
    private String writer;

    // 매출 검색 조건
    private Integer year;
    private Integer quarter;

    // 코드 검색 조건
    private String codeGroup;
    private String codeValue;
}
