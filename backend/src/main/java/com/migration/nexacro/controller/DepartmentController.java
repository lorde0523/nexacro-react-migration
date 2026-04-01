// [MIGRATION] AS-IS: Nexcore .xpc 서비스 정의 - 부서 서비스
// [TO-BE]: Spring @RestController

package com.migration.nexacro.controller;

import com.migration.nexacro.dto.DepartmentDTO;
import com.migration.nexacro.dto.EmployeeDTO;
import com.migration.nexacro.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 부서 REST Controller (Nexcore .xpc 서비스 → @RestController)
 *
 * Nexacro Transaction 매핑:
 * - GET /api/departments              → 부서 목록 조회
 * - GET /api/departments/{id}/employees → 부서별 사원 조회 (grd_oncellclick)
 */
@Slf4j
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    /**
     * 부서 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> getDepartmentList() {
        log.info("부서 목록 조회 요청");
        List<DepartmentDTO> departments = departmentService.getDepartmentList();
        return ResponseEntity.ok(departments);
    }

    /**
     * 부서 상세 조회
     */
    @GetMapping("/{deptId}")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable String deptId) {
        log.info("부서 상세 조회 요청: deptId={}", deptId);
        DepartmentDTO dept = departmentService.getDepartmentById(deptId);
        return ResponseEntity.ok(dept);
    }

    /**
     * 부서별 사원 조회 (Nexacro grd_oncellclick 연동 대응)
     */
    @GetMapping("/{deptId}/employees")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByDeptId(@PathVariable String deptId) {
        log.info("부서별 사원 조회 요청: deptId={}", deptId);
        List<EmployeeDTO> employees = departmentService.getEmployeesByDeptId(deptId);
        return ResponseEntity.ok(employees);
    }
}
