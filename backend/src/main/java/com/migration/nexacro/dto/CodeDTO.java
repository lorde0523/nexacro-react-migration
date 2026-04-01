// [MIGRATION] AS-IS: Nexcore PlatformData/Dataset - 공통코드 Dataset
// [TO-BE]: Spring Boot DTO

package com.migration.nexacro.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 공통코드 DTO (Nexacro PlatformData/Dataset → Java DTO)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeDTO {
    private String codeGroup;
    private String codeGroupName;
    private String codeValue;
    private String codeName;
    private Integer sortOrder;
    private String useYn;
    private String remark;
    private LocalDateTime createdAt;
}
