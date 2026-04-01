// [MIGRATION] AS-IS: Nexcore .xpc 서비스 정의 - 공통코드 서비스
// [TO-BE]: Spring @RestController

package com.migration.nexacro.controller;

import com.migration.nexacro.dto.CodeDTO;
import com.migration.nexacro.service.CodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 공통코드 REST Controller (Nexcore .xpc 서비스 → @RestController)
 *
 * Nexacro Transaction 매핑:
 * - GET /api/codes        → 공통코드 목록 조회 (fn_getCodes 대응)
 * - GET /api/codes/groups → 코드 그룹 목록 조회
 */
@Slf4j
@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
public class CodeController {

    private final CodeService codeService;

    /**
     * 공통코드 목록 조회 (Nexacro fn_getCodes 대응)
     */
    @GetMapping
    public ResponseEntity<List<CodeDTO>> getCodeList(
            @RequestParam(required = false) String codeGroup,
            @RequestParam(required = false) String codeValue) {
        log.info("공통코드 조회 요청: codeGroup={}, codeValue={}", codeGroup, codeValue);
        List<CodeDTO> codes = codeService.getCodeList(codeGroup, codeValue);
        return ResponseEntity.ok(codes);
    }

    /**
     * 코드 그룹 목록 조회
     */
    @GetMapping("/groups")
    public ResponseEntity<List<Map<String, Object>>> getCodeGroups() {
        log.info("코드 그룹 목록 조회 요청");
        List<Map<String, Object>> groups = codeService.getCodeGroups();
        return ResponseEntity.ok(groups);
    }
}
