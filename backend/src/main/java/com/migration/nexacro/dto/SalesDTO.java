// [MIGRATION] AS-IS: Nexcore PlatformData/Dataset - 매출 Dataset
// [TO-BE]: Spring Boot DTO

package com.migration.nexacro.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 매출 DTO (Nexacro PlatformData/Dataset → Java DTO)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesDTO {
    private Long salesId;
    private String deptId;
    private String deptName;
    private Integer year;
    private Integer quarter;
    private Integer month;
    private String productName;
    private BigDecimal salesAmount;
    private Integer salesCount;
    private BigDecimal salesAvg;
    private BigDecimal growthRate;
    private LocalDateTime createdAt;
}
