// [MIGRATION] AS-IS: Nexacro Transaction - 사원 관련 서비스 호출
// [TO-BE]: Axios 기반 사원 API 호출 함수

import axiosInstance from './axiosInstance';

/**
 * 사원 목록 조회 (Nexacro fn_search 대응)
 * @param {Object} searchCondition - 검색 조건 { empName, deptCode, hireStartDate, hireEndDate }
 */
export const getEmployeeList = (searchCondition) => {
  return axiosInstance.get('/employees', { params: searchCondition });
};

/**
 * 사원 상세 조회
 * @param {string|number} empId - 사원 ID
 */
export const getEmployeeById = (empId) => {
  return axiosInstance.get(`/employees/${empId}`);
};

/**
 * 사원 등록 (Nexacro btn_save_onclick - INSERT 대응)
 * @param {Object} employeeData - 사원 데이터
 */
export const createEmployee = (employeeData) => {
  return axiosInstance.post('/employees', employeeData);
};

/**
 * 사원 수정 (Nexacro btn_save_onclick - UPDATE 대응)
 * @param {string|number} empId - 사원 ID
 * @param {Object} employeeData - 수정 데이터
 */
export const updateEmployee = (empId, employeeData) => {
  return axiosInstance.put(`/employees/${empId}`, employeeData);
};

/**
 * 사원 삭제 (Nexacro btn_delete_onclick 대응)
 * @param {string|number} empId - 사원 ID
 */
export const deleteEmployee = (empId) => {
  return axiosInstance.delete(`/employees/${empId}`);
};
