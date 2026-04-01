// [MIGRATION] AS-IS: Nexacro Transaction - 매출 서비스 호출
// [TO-BE]: Axios 기반 매출 API 호출 함수

import axiosInstance from './axiosInstance';

/**
 * 매출 목록 조회 (Nexacro fn_search 대응)
 * @param {Object} params - 검색 조건 { year, quarter, deptCode }
 */
export const getSalesList = (params) => {
  return axiosInstance.get('/sales', { params });
};

/**
 * 매출 요약 조회 (차트 데이터용, Nexacro Dataset 바인딩 대응)
 * @param {Object} params - 조회 조건 { year }
 */
export const getSalesSummary = (params) => {
  return axiosInstance.get('/sales/summary', { params });
};

/**
 * 부서별 매출 조회
 * @param {Object} params - 조회 조건
 */
export const getSalesByDept = (params) => {
  return axiosInstance.get('/sales/by-dept', { params });
};
