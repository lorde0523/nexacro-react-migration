// [MIGRATION] AS-IS: Nexacro Form - EmployeeList.xfdl
// [TO-BE]: React 17 Function Component
// Grid: AG Grid Enterprise / Chart: ECharts / Editor: Froala

import React, { useState, useEffect, useCallback } from 'react';
import CommonGrid from '../components/CommonGrid';
import SearchBar from '../components/SearchBar';
import { getEmployeeList } from '../api/employeeApi';
import { fn_dateFormat, fn_getToday, fn_alert } from '../utils/commonUtils';
import { createColumnDef, createDateColumnDef } from '../utils/gridUtils';

/**
 * 사원 목록 조회 화면
 * Nexacro 패턴: 검색 조건 입력 → 조회 버튼 → Grid에 목록 표시
 *
 * Nexacro 이벤트 매핑:
 * - fn_init → useEffect([], [])
 * - fn_search → handleSearch (useCallback + Axios GET)
 * - btn_excel_onclick → handleExcelExport
 */
function EmployeeList() {
  // Nexacro Dataset → useState
  const [rowData, setRowData] = useState([]);
  const [loading, setLoading] = useState(false);

  // 검색 조건 상태 (Nexacro 검색 Dataset 대응)
  const [searchCondition, setSearchCondition] = useState({
    empName: '',
    deptCode: '',
    hireStartDate: '',
    hireEndDate: fn_getToday(),
  });

  // AG Grid 컬럼 정의 (Nexacro Grid 컬럼 Dataset 대응)
  const columnDefs = [
    createColumnDef('empId', '사원번호', { width: 100, pinned: 'left' }),
    createColumnDef('empName', '사원명', { width: 120 }),
    createColumnDef('deptName', '부서명', { width: 150 }),
    createColumnDef('position', '직급', { width: 100 }),
    createDateColumnDef('hireDate', '입사일', { width: 120 }),
    createColumnDef('email', '이메일', { width: 200 }),
    createColumnDef('phone', '연락처', { width: 140 }),
    createColumnDef('status', '재직상태', {
      width: 100,
      cellClassRules: {
        'cell-active': (params) => params.value === '재직',
        'cell-inactive': (params) => params.value === '퇴직',
      },
    }),
  ];

  // fn_init 대응: 컴포넌트 마운트 시 초기 데이터 조회 (검색 버튼 없이 최초 1회 조회)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { handleSearch(); }, []);

  // fn_search 대응: 조회 버튼 클릭 핸들러
  const handleSearch = useCallback(async () => {
    setLoading(true);
    try {
      const response = await getEmployeeList(searchCondition);
      setRowData(response.data);
    } catch (error) {
      console.error('사원 목록 조회 실패:', error);
      fn_alert('사원 목록 조회 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  }, [searchCondition]);

  // 검색 조건 초기화
  const handleReset = useCallback(() => {
    setSearchCondition({
      empName: '',
      deptCode: '',
      hireStartDate: '',
      hireEndDate: fn_getToday(),
    });
  }, []);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setSearchCondition((prev) => ({ ...prev, [name]: value }));
  };

  return (
    <div className="page-wrapper">
      <h2 className="page-title">사원 목록 조회</h2>

      {/* 검색 조건 영역 (Nexacro 검색 Dataset 대응) */}
      <SearchBar onSearch={handleSearch} onReset={handleReset}>
        <div className="search-row">
          <div className="search-item">
            <label>사원명</label>
            <input
              type="text"
              name="empName"
              value={searchCondition.empName}
              onChange={handleInputChange}
              placeholder="사원명 입력"
            />
          </div>
          <div className="search-item">
            <label>부서</label>
            <input
              type="text"
              name="deptCode"
              value={searchCondition.deptCode}
              onChange={handleInputChange}
              placeholder="부서코드 입력"
            />
          </div>
          <div className="search-item">
            <label>입사일</label>
            <input
              type="date"
              name="hireStartDate"
              value={searchCondition.hireStartDate}
              onChange={handleInputChange}
            />
            <span className="date-separator">~</span>
            <input
              type="date"
              name="hireEndDate"
              value={searchCondition.hireEndDate}
              onChange={handleInputChange}
            />
          </div>
        </div>
      </SearchBar>

      {/* Grid 영역 (Nexacro Grid 대응) - AG Grid Enterprise */}
      <div className="grid-section">
        <div className="section-header">
          <span className="record-count">총 {rowData.length}건</span>
        </div>
        {loading ? (
          <div className="loading-spinner">데이터 로딩 중...</div>
        ) : (
          <CommonGrid
            columnDefs={columnDefs}
            rowData={rowData}
            height="500px"
            showExcelButton={true}
            excelFileName="사원목록"
          />
        )}
      </div>
    </div>
  );
}

export default EmployeeList;
