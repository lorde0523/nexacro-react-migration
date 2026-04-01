// [MIGRATION] AS-IS: Nexcore Service - DepartmentService
// [TO-BE]: Spring @Service

package com.migration.nexacro.service;

import com.migration.nexacro.dto.DepartmentDTO;
import com.migration.nexacro.dto.EmployeeDTO;
import com.migration.nexacro.mapper.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 부서 서비스 (Nexcore Service → Spring @Service)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentMapper departmentMapper;

    /**
     * 부서 목록 조회
     */
    @Transactional(readOnly = true)
    public List<DepartmentDTO> getDepartmentList() {
        log.debug("부서 목록 조회");
        return departmentMapper.findAll();
    }

    /**
     * 부서 상세 조회
     */
    @Transactional(readOnly = true)
    public DepartmentDTO getDepartmentById(String deptId) {
        log.debug("부서 상세 조회: deptId={}", deptId);
        DepartmentDTO dept = departmentMapper.findById(deptId);
        if (dept == null) {
            throw new RuntimeException("부서 정보를 찾을 수 없습니다: " + deptId);
        }
        return dept;
    }

    /**
     * 부서별 사원 목록 조회 (Nexacro grd_oncellclick 연동 대응)
     */
    @Transactional(readOnly = true)
    public List<EmployeeDTO> getEmployeesByDeptId(String deptId) {
        log.debug("부서별 사원 조회: deptId={}", deptId);
        return departmentMapper.findEmployeesByDeptId(deptId);
    }
}
