// [MIGRATION] AS-IS: Nexcore Service - CodeService
// [TO-BE]: Spring @Service

package com.migration.nexacro.service;

import com.migration.nexacro.dto.CodeDTO;
import com.migration.nexacro.mapper.CodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 공통코드 서비스 (Nexcore Service → Spring @Service)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeService {

    private final CodeMapper codeMapper;

    /**
     * 공통코드 목록 조회 (Nexacro fn_getCodes 대응)
     */
    @Transactional(readOnly = true)
    public List<CodeDTO> getCodeList(String codeGroup, String codeValue) {
        log.debug("공통코드 조회: codeGroup={}, codeValue={}", codeGroup, codeValue);
        return codeMapper.findAll(codeGroup, codeValue);
    }

    /**
     * 코드 그룹 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCodeGroups() {
        return codeMapper.findGroups();
    }

    /**
     * 코드 단건 조회
     */
    @Transactional(readOnly = true)
    public CodeDTO getCodeByGroupAndValue(String codeGroup, String codeValue) {
        return codeMapper.findByGroupAndValue(codeGroup, codeValue);
    }
}
