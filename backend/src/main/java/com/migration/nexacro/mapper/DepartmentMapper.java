// [MIGRATION] AS-IS: Nexcore DAO - DepartmentDAO
// [TO-BE]: MyBatis Mapper Interface

package com.migration.nexacro.mapper;

import com.migration.nexacro.dto.DepartmentDTO;
import com.migration.nexacro.dto.EmployeeDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 부서 MyBatis Mapper (Nexcore DAO → MyBatis Mapper)
 */
@Mapper
public interface DepartmentMapper {
    List<DepartmentDTO> findAll();
    DepartmentDTO findById(@Param("deptId") String deptId);
    List<EmployeeDTO> findEmployeesByDeptId(@Param("deptId") String deptId);
    int insert(DepartmentDTO department);
    int update(DepartmentDTO department);
}
