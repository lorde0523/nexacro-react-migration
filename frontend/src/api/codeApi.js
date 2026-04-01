// [MIGRATION] AS-IS: Nexacro Transaction - 공통코드 서비스 호출
// [TO-BE]: Axios 기반 공통코드 API 호출 함수

import axiosInstance from './axiosInstance';

/**
 * 공통코드 목록 조회 (Nexacro fn_getCodes 대응)
 * @param {string} codeGroup - 코드 그룹
 * @param {string} codeValue - 코드 검색어 (선택)
 */
export const getCodeList = (codeGroup, codeValue) => {
  return axiosInstance.get('/codes', { params: { codeGroup, codeValue } });
};

/**
 * 코드 그룹 목록 조회
 */
export const getCodeGroups = () => {
  return axiosInstance.get('/codes/groups');
};
