// [MIGRATION] AS-IS: Nexacro Form - MasterDetail.xfdl
// [TO-BE]: React 17 Function Component
// Grid: AG Grid Enterprise

import React, { useState, useEffect, useCallback } from 'react';
import CommonGrid from '../components/CommonGrid';
import { getDepartmentList, getEmployeesByDept } from '../api/departmentApi';
import { createColumnDef, createDateColumnDef } from '../utils/gridUtils';
import { fn_alert, fn_dateFormat } from '../utils/commonUtils';

/**
 * 마스터-디테일 화면 (부서-사원)
 * Nexacro 패턴: 상단 Grid 행 클릭 → 하단에 상세 정보 표시
 *
 * Nexacro 이벤트 매핑:
 * - fn_init → useEffect([], [])
 * - grd_dept_oncellclick → handleDeptRowClicked (AG Grid onRowClicked)
 * - grd_emp_oncellclick → handleEmpRowClicked
 */
function MasterDetail() {
  // 부서 목록 (Master Dataset)
  const [deptRowData, setDeptRowData] = useState([]);
  // 사원 목록 (Detail Dataset)
  const [empRowData, setEmpRowData] = useState([]);
  // 선택된 사원 상세 정보
  const [selectedEmployee, setSelectedEmployee] = useState(null);
  // 선택된 부서
  const [selectedDept, setSelectedDept] = useState(null);

  const [deptLoading, setDeptLoading] = useState(false);
  const [empLoading, setEmpLoading] = useState(false);

  // 부서 Grid 컬럼 정의
  const deptColumnDefs = [
    createColumnDef('deptId', '부서코드', { width: 100 }),
    createColumnDef('deptName', '부서명', { width: 150 }),
    createColumnDef('deptHead', '부서장', { width: 120 }),
    createColumnDef('location', '위치', { width: 150 }),
    createColumnDef('empCount', '인원수', { width: 80, type: 'numericColumn' }),
  ];

  // 사원 Grid 컬럼 정의
  const empColumnDefs = [
    createColumnDef('empId', '사원번호', { width: 100 }),
    createColumnDef('empName', '사원명', { width: 120 }),
    createColumnDef('position', '직급', { width: 100 }),
    createDateColumnDef('hireDate', '입사일', { width: 120 }),
    createColumnDef('email', '이메일', { width: 200 }),
    createColumnDef('phone', '연락처', { width: 140 }),
  ];

  // fn_init 대응: 부서 목록 초기 조회 (마운트 시 1회)
  const fetchDeptList = useCallback(async () => {
    setDeptLoading(true);
    try {
      const response = await getDepartmentList();
      setDeptRowData(response.data);
    } catch (error) {
      console.error('부서 목록 조회 실패:', error);
      fn_alert('부서 목록 조회 중 오류가 발생했습니다.');
    } finally {
      setDeptLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchDeptList();
  }, [fetchDeptList]);

  // grd_dept_oncellclick 대응: 부서 행 클릭 시 사원 목록 조회
  const handleDeptRowClicked = useCallback(async (params) => {
    const dept = params.data;
    setSelectedDept(dept);
    setSelectedEmployee(null);
    setEmpRowData([]);

    setEmpLoading(true);
    try {
      const response = await getEmployeesByDept(dept.deptId);
      setEmpRowData(response.data);
    } catch (error) {
      console.error('사원 목록 조회 실패:', error);
      fn_alert('사원 목록 조회 중 오류가 발생했습니다.');
    } finally {
      setEmpLoading(false);
    }
  }, []);

  // grd_emp_oncellclick 대응: 사원 행 클릭 시 상세 정보 표시
  const handleEmpRowClicked = useCallback((params) => {
    setSelectedEmployee(params.data);
  }, []);

  return (
    <div className="page-wrapper">
      <h2 className="page-title">마스터-디테일 (부서-사원)</h2>

      <div className="master-detail-layout">
        {/* 마스터 Grid: 부서 목록 */}
        <div className="master-section">
          <div className="section-header">
            <h3>부서 목록</h3>
            <span className="record-count">총 {deptRowData.length}건</span>
          </div>
          <CommonGrid
            columnDefs={deptColumnDefs}
            rowData={deptRowData}
            height="300px"
            onRowClicked={handleDeptRowClicked}
            gridOptions={{ rowSelection: 'single' }}
          />
        </div>

        {/* 디테일 영역 */}
        {selectedDept && (
          <div className="detail-section">
            <div className="detail-layout">
              {/* 디테일 Grid: 사원 목록 */}
              <div className="detail-grid-section">
                <div className="section-header">
                  <h3>{selectedDept.deptName} 사원 목록</h3>
                  <span className="record-count">총 {empRowData.length}건</span>
                </div>
                {empLoading ? (
                  <div className="loading-spinner">데이터 로딩 중...</div>
                ) : (
                  <CommonGrid
                    columnDefs={empColumnDefs}
                    rowData={empRowData}
                    height="250px"
                    onRowClicked={handleEmpRowClicked}
                  />
                )}
              </div>

              {/* 선택된 사원 상세 Form */}
              {selectedEmployee && (
                <div className="detail-form-section">
                  <h3>사원 상세 정보</h3>
                  <div className="detail-form">
                    <div className="form-row">
                      <label>사원번호</label>
                      <span>{selectedEmployee.empId}</span>
                    </div>
                    <div className="form-row">
                      <label>사원명</label>
                      <span>{selectedEmployee.empName}</span>
                    </div>
                    <div className="form-row">
                      <label>부서명</label>
                      <span>{selectedEmployee.deptName}</span>
                    </div>
                    <div className="form-row">
                      <label>직급</label>
                      <span>{selectedEmployee.position}</span>
                    </div>
                    <div className="form-row">
                      <label>입사일</label>
                      <span>{fn_dateFormat(selectedEmployee.hireDate)}</span>
                    </div>
                    <div className="form-row">
                      <label>이메일</label>
                      <span>{selectedEmployee.email}</span>
                    </div>
                    <div className="form-row">
                      <label>연락처</label>
                      <span>{selectedEmployee.phone}</span>
                    </div>
                    <div className="form-row">
                      <label>재직상태</label>
                      <span>{selectedEmployee.status}</span>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}

export default MasterDetail;
