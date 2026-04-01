// [MIGRATION] AS-IS: Nexcore DAO - CodeDAO
// [TO-BE]: MyBatis Mapper Interface

package com.migration.nexacro.mapper;

import com.migration.nexacro.dto.CodeDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 공통코드 MyBatis Mapper (Nexcore DAO → MyBatis Mapper)
 */
@Mapper
public interface CodeMapper {
    List<CodeDTO> findAll(@Param("codeGroup") String codeGroup, @Param("codeValue") String codeValue);
    List<Map<String, Object>> findGroups();
    CodeDTO findByGroupAndValue(@Param("codeGroup") String codeGroup, @Param("codeValue") String codeValue);
}
