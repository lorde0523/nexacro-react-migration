// [MIGRATION] AS-IS: Nexcore .xpc 서비스 정의 - 사원 서비스
// [TO-BE]: Spring @RestController

package com.migration.nexacro.controller;

import com.migration.nexacro.dto.EmployeeDTO;
import com.migration.nexacro.dto.SearchCondition;
import com.migration.nexacro.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사원 REST Controller (Nexcore .xpc 서비스 → @RestController)
 *
 * Nexacro Transaction 매핑:
 * - GET    /api/employees       → 사원 목록 조회 (fn_search)
 * - GET    /api/employees/{id}  → 사원 상세 조회
 * - POST   /api/employees       → 사원 등록 (btn_save_onclick - INSERT)
 * - PUT    /api/employees/{id}  → 사원 수정 (btn_save_onclick - UPDATE)
 * - DELETE /api/employees/{id}  → 사원 삭제 (btn_delete_onclick)
 */
@Slf4j
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 사원 목록 조회 (Nexacro fn_search 대응)
     */
    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getEmployeeList(SearchCondition condition) {
        log.info("사원 목록 조회 요청: {}", condition);
        List<EmployeeDTO> employees = employeeService.getEmployeeList(condition);
        return ResponseEntity.ok(employees);
    }

    /**
     * 사원 상세 조회
     */
    @GetMapping("/{empId}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable String empId) {
        log.info("사원 상세 조회 요청: empId={}", empId);
        EmployeeDTO employee = employeeService.getEmployeeById(empId);
        return ResponseEntity.ok(employee);
    }

    /**
     * 사원 등록 (Nexacro btn_save_onclick - INSERT 대응)
     */
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody EmployeeDTO employee) {
        log.info("사원 등록 요청: {}", employee.getEmpName());
        EmployeeDTO created = employeeService.createEmployee(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 사원 수정 (Nexacro btn_save_onclick - UPDATE 대응)
     */
    @PutMapping("/{empId}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @PathVariable String empId,
            @RequestBody EmployeeDTO employee) {
        log.info("사원 수정 요청: empId={}", empId);
        EmployeeDTO updated = employeeService.updateEmployee(empId, employee);
        return ResponseEntity.ok(updated);
    }

    /**
     * 사원 삭제 (Nexacro btn_delete_onclick 대응)
     */
    @DeleteMapping("/{empId}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable String empId) {
        log.info("사원 삭제 요청: empId={}", empId);
        employeeService.deleteEmployee(empId);
        return ResponseEntity.noContent().build();
    }
}
