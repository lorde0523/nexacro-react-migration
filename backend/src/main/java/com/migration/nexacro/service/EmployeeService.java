// [MIGRATION] AS-IS: Nexcore Service - EmployeeService
// [TO-BE]: Spring @Service

package com.migration.nexacro.service;

import com.migration.nexacro.dto.EmployeeDTO;
import com.migration.nexacro.dto.SearchCondition;
import com.migration.nexacro.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 사원 서비스 (Nexcore Service → Spring @Service)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeMapper employeeMapper;

    /**
     * 사원 목록 조회 (Nexacro fn_search 대응)
     */
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getEmployeeList(SearchCondition condition) {
        log.debug("사원 목록 조회: {}", condition);
        return employeeMapper.findAll(condition);
    }

    /**
     * 사원 상세 조회
     */
    @Transactional(readOnly = true)
    public EmployeeDTO getEmployeeById(String empId) {
        log.debug("사원 상세 조회: empId={}", empId);
        EmployeeDTO employee = employeeMapper.findById(empId);
        if (employee == null) {
            throw new RuntimeException("사원 정보를 찾을 수 없습니다: " + empId);
        }
        return employee;
    }

    /**
     * 사원 등록 (Nexacro INSERT 대응)
     */
    @Transactional
    public EmployeeDTO createEmployee(EmployeeDTO employee) {
        log.debug("사원 등록: {}", employee);
        employeeMapper.insert(employee);
        return employee;
    }

    /**
     * 사원 수정 (Nexacro UPDATE 대응)
     */
    @Transactional
    public EmployeeDTO updateEmployee(String empId, EmployeeDTO employee) {
        log.debug("사원 수정: empId={}, data={}", empId, employee);
        employee.setEmpId(empId);
        int updated = employeeMapper.update(employee);
        if (updated == 0) {
            throw new RuntimeException("사원 정보를 찾을 수 없습니다: " + empId);
        }
        return employee;
    }

    /**
     * 사원 삭제 (Nexacro DELETE 대응)
     */
    @Transactional
    public void deleteEmployee(String empId) {
        log.debug("사원 삭제: empId={}", empId);
        int deleted = employeeMapper.deleteById(empId);
        if (deleted == 0) {
            throw new RuntimeException("사원 정보를 찾을 수 없습니다: " + empId);
        }
    }
}
