// [MIGRATION] AS-IS: Nexcore DAO - EmployeeDAO
// [TO-BE]: MyBatis Mapper Interface

package com.migration.nexacro.mapper;

import com.migration.nexacro.dto.EmployeeDTO;
import com.migration.nexacro.dto.SearchCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 사원 MyBatis Mapper (Nexcore DAO → MyBatis Mapper)
 */
@Mapper
public interface EmployeeMapper {
    List<EmployeeDTO> findAll(SearchCondition condition);
    EmployeeDTO findById(@Param("empId") String empId);
    int insert(EmployeeDTO employee);
    int update(EmployeeDTO employee);
    int deleteById(@Param("empId") String empId);
    int countByCondition(SearchCondition condition);
}
