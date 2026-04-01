// [MIGRATION] AS-IS: Nexcore PlatformData/Dataset - 부서 Dataset
// [TO-BE]: Spring Boot DTO

package com.migration.nexacro.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 부서 DTO (Nexacro PlatformData/Dataset → Java DTO)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDTO {
    private String deptId;
    private String deptName;
    private String deptHead;
    private String location;
    private Integer empCount;
    private String useYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
