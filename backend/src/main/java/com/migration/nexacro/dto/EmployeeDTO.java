// [MIGRATION] AS-IS: Nexcore PlatformData/Dataset - 사원 Dataset
// [TO-BE]: Spring Boot DTO

package com.migration.nexacro.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사원 DTO (Nexacro PlatformData/Dataset → Java DTO)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {
    private String empId;
    private String empName;
    private String deptId;
    private String deptName;
    private String position;
    private LocalDate hireDate;
    private String email;
    private String phone;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
