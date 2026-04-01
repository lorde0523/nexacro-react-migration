// [MIGRATION] AS-IS: Nexacro Transaction - 부서 서비스 호출
// [TO-BE]: Axios 기반 부서 API 호출 함수

import axiosInstance from './axiosInstance';

/**
 * 부서 목록 조회
 */
export const getDepartmentList = () => {
  return axiosInstance.get('/departments');
};

/**
 * 부서별 사원 조회 (Nexacro grd_oncellclick 연동 대응)
 * @param {string|number} deptId - 부서 ID
 */
export const getEmployeesByDept = (deptId) => {
  return axiosInstance.get(`/departments/${deptId}/employees`);
};
