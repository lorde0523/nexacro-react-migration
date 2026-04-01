// [MIGRATION] AS-IS: Nexacro PopupDiv - CodeSearch.xfdl (공통코드 검색 팝업)
// [TO-BE]: React 17 Function Component
// Grid: AG Grid Enterprise

import React, { useState, useEffect, useCallback } from 'react';
import CommonGrid from '../components/CommonGrid';
import CommonModal from '../components/CommonModal';
import { getCodeList, getCodeGroups } from '../api/codeApi';
import { createColumnDef } from '../utils/gridUtils';
import { fn_alert } from '../utils/commonUtils';

/**
 * 공통코드 검색 팝업 화면
 * Nexacro 패턴: 공통코드 검색 PopupDiv (AG Grid + 검색)
 *
 * Nexacro 이벤트 매핑:
 * - fn_init → useEffect([], [])
 * - btn_search_onclick → handleSearch
 * - grd_oncelldblclick → handleCodeSelect (코드 선택)
 *
 * 사용 방법:
 * - 단독 페이지로도 사용 가능
 * - 팝업(Modal)으로도 사용 가능 (isPopup=true, onSelect 콜백 전달)
 */
function CodeSearchPopup({ isPopup = false, onSelect, initialCodeGroup = '' }) {
  const [codeGroups, setCodeGroups] = useState([]);
  const [rowData, setRowData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedCode, setSelectedCode] = useState(null);

  // 확인 Modal 상태
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);

  // 검색 조건
  const [searchCondition, setSearchCondition] = useState({
    codeGroup: initialCodeGroup,
    codeValue: '',
  });

  const columnDefs = [
    createColumnDef('codeGroup', '코드그룹', { width: 120 }),
    createColumnDef('codeGroupName', '그룹명', { width: 150 }),
    createColumnDef('codeValue', '코드값', { width: 120 }),
    createColumnDef('codeName', '코드명', { width: 200 }),
    createColumnDef('sortOrder', '정렬순서', { width: 80, type: 'numericColumn' }),
    createColumnDef('useYn', '사용여부', { width: 80 }),
  ];

  // fn_init 대응
  useEffect(() => {
    fetchCodeGroups();
    if (initialCodeGroup) {
      handleSearch();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const fetchCodeGroups = async () => {
    try {
      const response = await getCodeGroups();
      setCodeGroups(response.data);
    } catch (error) {
      console.error('코드 그룹 조회 실패:', error);
    }
  };

  const handleSearch = useCallback(async () => {
    setLoading(true);
    try {
      const response = await getCodeList(searchCondition.codeGroup, searchCondition.codeValue);
      setRowData(response.data);
    } catch (error) {
      console.error('공통코드 조회 실패:', error);
      fn_alert('공통코드 조회 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  }, [searchCondition]);

  // 행 클릭 (선택)
  const handleRowClicked = useCallback((params) => {
    setSelectedCode(params.data);
  }, []);

  // grd_oncelldblclick 대응: 더블클릭 시 코드 선택 (팝업 모드)
  const handleCellDoubleClicked = useCallback((params) => {
    if (isPopup && onSelect) {
      onSelect(params.data);
    }
  }, [isPopup, onSelect]);

  // 선택 버튼 클릭 (팝업 모드)
  const handleSelectClick = useCallback(() => {
    if (!selectedCode) {
      fn_alert('코드를 선택해주세요.');
      return;
    }
    if (isPopup && onSelect) {
      onSelect(selectedCode);
    }
  }, [selectedCode, isPopup, onSelect]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setSearchCondition((prev) => ({ ...prev, [name]: value }));
  };

  return (
    <div className="page-wrapper">
      {!isPopup && <h2 className="page-title">공통코드 검색</h2>}

      {/* 검색 조건 */}
      <div className="search-bar-wrapper">
        <div className="search-bar-body">
          <div className="search-row">
            <div className="search-item">
              <label>코드그룹</label>
              <select
                name="codeGroup"
                value={searchCondition.codeGroup}
                onChange={handleInputChange}
              >
                <option value="">전체</option>
                {codeGroups.map((group) => (
                  <option key={group.codeGroup} value={group.codeGroup}>
                    {group.codeGroupName}
                  </option>
                ))}
              </select>
            </div>
            <div className="search-item">
              <label>코드/코드명</label>
              <input
                type="text"
                name="codeValue"
                value={searchCondition.codeValue}
                onChange={handleInputChange}
                placeholder="코드 또는 코드명 입력"
                onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
              />
            </div>
          </div>
        </div>
        <div className="search-bar-footer">
          <button className="btn btn-primary" onClick={handleSearch}>조회</button>
          {isPopup && (
            <button className="btn btn-success" onClick={handleSelectClick}>선택</button>
          )}
        </div>
      </div>

      {/* Grid */}
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
            height="450px"
            onRowClicked={handleRowClicked}
            onCellDoubleClicked={handleCellDoubleClicked}
          />
        )}
      </div>

      {/* 사용 예시: 팝업으로 사용하는 방법 */}
      {!isPopup && (
        <div className="usage-example">
          <h3>팝업 사용 예시</h3>
          <pre>{`
// 다른 컴포넌트에서 CommonModal과 함께 사용:
<CommonModal
  isOpen={isCodePopupOpen}
  title="공통코드 검색"
  onClose={() => setIsCodePopupOpen(false)}
  showFooter={false}
  size="lg"
>
  <CodeSearchPopup
    isPopup={true}
    initialCodeGroup="DEPT"
    onSelect={(code) => {
      setDeptCode(code.codeValue);
      setIsCodePopupOpen(false);
    }}
  />
</CommonModal>
          `}</pre>
        </div>
      )}
    </div>
  );
}

export default CodeSearchPopup;
