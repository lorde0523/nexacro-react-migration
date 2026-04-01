// [MIGRATION] AS-IS: Nexcore DAO - BoardDAO
// [TO-BE]: MyBatis Mapper Interface

package com.migration.nexacro.mapper;

import com.migration.nexacro.dto.BoardDTO;
import com.migration.nexacro.dto.SearchCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 게시판 MyBatis Mapper (Nexcore DAO → MyBatis Mapper)
 */
@Mapper
public interface BoardMapper {
    List<BoardDTO> findAll(SearchCondition condition);
    BoardDTO findById(@Param("boardId") Long boardId);
    int insert(BoardDTO board);
    int update(BoardDTO board);
    int deleteById(@Param("boardId") Long boardId);
    int incrementViewCount(@Param("boardId") Long boardId);
}
