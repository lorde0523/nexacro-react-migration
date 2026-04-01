// [MIGRATION] AS-IS: Nexacro Grid 컴포넌트
// [TO-BE]: AG Grid Enterprise 래퍼 컴포넌트
// Grid: AG Grid Enterprise

import React, { useRef, useCallback } from 'react';
import { AgGridReact } from 'ag-grid-react';
import 'ag-grid-community/styles/ag-grid.css';
import 'ag-grid-community/styles/ag-theme-alpine.css';
import 'ag-grid-enterprise';
import { defaultGridOptions, getExcelExportParams } from '../utils/gridUtils';
import '../assets/styles/grid.css';

/**
 * AG Grid Enterprise 래퍼 컴포넌트 (Nexacro Grid 대응)
 * @param {Array} columnDefs - 컬럼 정의
 * @param {Array} rowData - 행 데이터
 * @param {Object} gridOptions - 그리드 옵션 (defaultGridOptions 오버라이드)
 * @param {Function} onRowClicked - 행 클릭 이벤트 (Nexacro grd_oncellclick 대응)
 * @param {Function} onCellDoubleClicked - 셀 더블클릭 이벤트 (Nexacro grd_oncelldblclick 대응)
 * @param {Function} onGridReady - 그리드 준비 완료 이벤트
 * @param {string} height - 그리드 높이
 * @param {boolean} showExcelButton - Excel 내보내기 버튼 표시 여부
 * @param {string} excelFileName - Excel 파일명
 */
function CommonGrid({
  columnDefs = [],
  rowData = [],
  gridOptions = {},
  onRowClicked,
  onCellDoubleClicked,
  onGridReady,
  height = '400px',
  showExcelButton = false,
  excelFileName = 'export',
  ...rest
}) {
  const gridRef = useRef(null);

  const mergedGridOptions = {
    ...defaultGridOptions,
    ...gridOptions,
  };

  // Excel 내보내기 (Nexacro Excel 다운로드 대응)
  const handleExcelExport = useCallback(() => {
    if (gridRef.current && gridRef.current.api) {
      gridRef.current.api.exportDataAsExcel(getExcelExportParams(excelFileName));
    }
  }, [excelFileName]);

  const handleGridReady = useCallback(
    (params) => {
      if (onGridReady) {
        onGridReady(params);
      }
    },
    [onGridReady]
  );

  return (
    <div className="common-grid-wrapper">
      {showExcelButton && (
        <div className="grid-toolbar">
          <button className="btn btn-excel" onClick={handleExcelExport}>
            Excel 다운로드
          </button>
        </div>
      )}
      <div className="ag-theme-alpine" style={{ height, width: '100%' }}>
        <AgGridReact
          ref={gridRef}
          columnDefs={columnDefs}
          rowData={rowData}
          onRowClicked={onRowClicked}
          onCellDoubleClicked={onCellDoubleClicked}
          onGridReady={handleGridReady}
          {...mergedGridOptions}
          {...rest}
        />
      </div>
    </div>
  );
}

export default CommonGrid;
