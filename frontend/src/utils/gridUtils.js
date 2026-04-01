// [MIGRATION] AS-IS: Nexacro Grid 공통 설정 (setCellProperty, expr 기반)
// [TO-BE]: AG Grid Enterprise 공통 유틸 함수

import { LicenseManager } from 'ag-grid-enterprise';

// AG Grid Enterprise 라이선스 설정 (실제 라이선스 키로 교체 필요)
LicenseManager.setLicenseKey('YOUR_AG_GRID_LICENSE_KEY');

/**
 * 기본 컬럼 정의 생성 헬퍼 (Nexacro Dataset 컬럼 → AG Grid columnDef 변환)
 * @param {string} field - 필드명
 * @param {string} headerName - 헤더명
 * @param {Object} options - 추가 옵션
 * @returns {Object} columnDef
 */
export const createColumnDef = (field, headerName, options = {}) => {
  return {
    field,
    headerName,
    sortable: true,
    filter: true,
    resizable: true,
    ...options,
  };
};

/**
 * 숫자 포맷 컬럼 (Nexacro expr 기반 숫자 포맷 → AG Grid valueFormatter)
 * @param {string} field
 * @param {string} headerName
 * @param {Object} options
 */
export const createNumberColumnDef = (field, headerName, options = {}) => {
  return createColumnDef(field, headerName, {
    type: 'numericColumn',
    valueFormatter: (params) => {
      if (params.value === null || params.value === undefined) return '';
      return params.value.toLocaleString('ko-KR');
    },
    ...options,
  });
};

/**
 * 날짜 포맷 컬럼 (Nexacro expr 기반 날짜 포맷 → AG Grid valueFormatter)
 * @param {string} field
 * @param {string} headerName
 * @param {Object} options
 */
export const createDateColumnDef = (field, headerName, options = {}) => {
  return createColumnDef(field, headerName, {
    valueFormatter: (params) => {
      if (!params.value) return '';
      const date = new Date(params.value);
      if (isNaN(date.getTime())) return params.value;
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    },
    ...options,
  });
};

/**
 * 기본 그리드 옵션 (Nexacro Grid 공통 설정 대응)
 */
export const defaultGridOptions = {
  defaultColDef: {
    sortable: true,
    filter: true,
    resizable: true,
    minWidth: 80,
  },
  rowSelection: 'single',
  animateRows: true,
  pagination: false,
  suppressMovableColumns: false,
  enableRangeSelection: true,
  suppressCellFocus: false,
};

/**
 * 페이지네이션 그리드 옵션
 */
export const paginationGridOptions = {
  ...defaultGridOptions,
  pagination: true,
  paginationPageSize: 20,
};

/**
 * Excel Export 설정 (Nexacro Excel 다운로드 대응)
 * @param {string} fileName - 파일명
 * @returns {Object} exportParams
 */
export const getExcelExportParams = (fileName = 'export') => {
  return {
    fileName: `${fileName}_${new Date().toISOString().slice(0, 10)}.xlsx`,
    sheetName: 'Sheet1',
  };
};

/**
 * 행 스타일 규칙 - 삭제 행 표시 (Nexacro setCellProperty 대응)
 */
export const rowClassRules = {
  'row-deleted': (params) => params.data && params.data._rowStatus === 'D',
  'row-new': (params) => params.data && params.data._rowStatus === 'I',
  'row-modified': (params) => params.data && params.data._rowStatus === 'U',
};
