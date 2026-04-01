// [MIGRATION] AS-IS: Nexacro Form - BoardList.xfdl
// [TO-BE]: React 17 Function Component
// Grid: AG Grid Enterprise

import React, { useState, useEffect, useCallback } from 'react';
import { useHistory } from 'react-router-dom';
import CommonGrid from '../components/CommonGrid';
import SearchBar from '../components/SearchBar';
import { getBoardList, deleteBoard } from '../api/boardApi';
import { createColumnDef, createDateColumnDef } from '../utils/gridUtils';
import { fn_alert, fn_confirm, fn_getToday } from '../utils/commonUtils';

/**
 * 게시판 목록 화면
 * Nexacro 패턴: 목록 조회 → 행 클릭으로 상세 이동 → 작성 버튼으로 글쓰기
 *
 * Nexacro 이벤트 매핑:
 * - fn_init → useEffect([], [])
 * - fn_search → handleSearch
 * - grd_oncellclick → handleRowClicked (상세 이동)
 * - btn_write_onclick → handleWriteClick
 */
function BoardList() {
  const history = useHistory();
  const [rowData, setRowData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [selectedRow, setSelectedRow] = useState(null);

  const [searchCondition, setSearchCondition] = useState({
    title: '',
    writer: '',
    startDate: '',
    endDate: fn_getToday(),
  });

  const columnDefs = [
    createColumnDef('boardId', '번호', { width: 80 }),
    createColumnDef('title', '제목', {
      flex: 1,
      cellRenderer: (params) => {
        return `<span style="color: #1890ff; cursor: pointer;">${params.value}</span>`;
      },
    }),
    createColumnDef('writer', '작성자', { width: 120 }),
    createDateColumnDef('createdAt', '작성일', { width: 120 }),
    createColumnDef('viewCount', '조회수', { width: 80, type: 'numericColumn' }),
    createColumnDef('attachCount', '첨부', { width: 70, type: 'numericColumn' }),
  ];

  // fn_init 대응 (검색 버튼 없이 최초 1회 조회)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  useEffect(() => { handleSearch(); }, []);

  const handleSearch = useCallback(async () => {
    setLoading(true);
    try {
      const response = await getBoardList(searchCondition);
      setRowData(response.data);
    } catch (error) {
      console.error('게시글 목록 조회 실패:', error);
      fn_alert('게시글 목록 조회 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  }, [searchCondition]);

  const handleReset = useCallback(() => {
    setSearchCondition({ title: '', writer: '', startDate: '', endDate: fn_getToday() });
  }, []);

  // grd_oncellclick 대응: 행 클릭 시 선택
  const handleRowClicked = useCallback((params) => {
    setSelectedRow(params.data);
  }, []);

  // grd_oncelldblclick 대응: 더블클릭 시 상세 이동 (React Router v5)
  const handleCellDoubleClicked = useCallback((params) => {
    history.push(`/board/edit/${params.data.boardId}`);
  }, [history]);

  // btn_write_onclick 대응: 글쓰기 이동
  const handleWriteClick = useCallback(() => {
    history.push('/board/write');
  }, [history]);

  // btn_delete_onclick 대응
  const handleDeleteClick = useCallback(async () => {
    if (!selectedRow) {
      fn_alert('삭제할 게시글을 선택해주세요.');
      return;
    }
    if (!fn_confirm(`"${selectedRow.title}" 게시글을 삭제하시겠습니까?`)) {
      return;
    }
    try {
      await deleteBoard(selectedRow.boardId);
      fn_alert('삭제되었습니다.');
      setSelectedRow(null);
      handleSearch();
    } catch (error) {
      console.error('게시글 삭제 실패:', error);
      fn_alert('삭제 중 오류가 발생했습니다.');
    }
  }, [selectedRow, handleSearch]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setSearchCondition((prev) => ({ ...prev, [name]: value }));
  };

  return (
    <div className="page-wrapper">
      <h2 className="page-title">게시판</h2>

      <SearchBar onSearch={handleSearch} onReset={handleReset}>
        <div className="search-row">
          <div className="search-item">
            <label>제목</label>
            <input
              type="text"
              name="title"
              value={searchCondition.title}
              onChange={handleInputChange}
              placeholder="제목 검색"
            />
          </div>
          <div className="search-item">
            <label>작성자</label>
            <input
              type="text"
              name="writer"
              value={searchCondition.writer}
              onChange={handleInputChange}
              placeholder="작성자 검색"
            />
          </div>
          <div className="search-item">
            <label>작성일</label>
            <input type="date" name="startDate" value={searchCondition.startDate} onChange={handleInputChange} />
            <span className="date-separator">~</span>
            <input type="date" name="endDate" value={searchCondition.endDate} onChange={handleInputChange} />
          </div>
        </div>
      </SearchBar>

      <div className="button-bar">
        <button className="btn btn-primary" onClick={handleWriteClick}>글쓰기</button>
        <button className="btn btn-danger" onClick={handleDeleteClick}>삭제</button>
      </div>

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
            onRowClicked={handleRowClicked}
            onCellDoubleClicked={handleCellDoubleClicked}
          />
        )}
      </div>
    </div>
  );
}

export default BoardList;
