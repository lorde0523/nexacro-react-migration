// [MIGRATION] AS-IS: Nexcore PlatformData/Dataset - 게시판 Dataset
// [TO-BE]: Spring Boot DTO

package com.migration.nexacro.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 게시판 DTO (Nexacro PlatformData/Dataset → Java DTO)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardDTO {
    private Long boardId;
    private String title;
    private String content;
    private String writer;
    private Integer viewCount;
    private Integer attachCount;
    private String useYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
