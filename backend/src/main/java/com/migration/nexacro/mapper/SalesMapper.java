// [MIGRATION] AS-IS: Nexcore DAO - SalesDAO
// [TO-BE]: MyBatis Mapper Interface

package com.migration.nexacro.mapper;

import com.migration.nexacro.dto.SalesDTO;
import com.migration.nexacro.dto.SearchCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 매출 MyBatis Mapper (Nexcore DAO → MyBatis Mapper)
 */
@Mapper
public interface SalesMapper {
    List<SalesDTO> findAll(SearchCondition condition);
    List<Map<String, Object>> findSummaryByYear(@Param("year") Integer year);
    List<Map<String, Object>> findSummaryByDept(SearchCondition condition);
}
