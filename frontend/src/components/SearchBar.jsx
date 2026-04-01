// [MIGRATION] AS-IS: Nexacro 검색 조건 영역 (Search Area)
// [TO-BE]: React 17 공통 검색 바 컴포넌트

import React from 'react';

/**
 * 공통 검색 바 컴포넌트 (Nexacro 검색 조건 영역 대응)
 * @param {React.ReactNode} children - 검색 조건 입력 필드들
 * @param {Function} onSearch - 조회 버튼 클릭 콜백 (Nexacro fn_search 대응)
 * @param {Function} onReset - 초기화 버튼 클릭 콜백 (Nexacro fn_init 대응)
 * @param {string} title - 검색 영역 제목
 */
function SearchBar({ children, onSearch, onReset, title = '검색 조건' }) {
  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      onSearch && onSearch();
    }
  };

  return (
    <div className="search-bar-wrapper" onKeyDown={handleKeyDown}>
      <div className="search-bar-header">
        <h4 className="search-bar-title">{title}</h4>
      </div>
      <div className="search-bar-body">{children}</div>
      <div className="search-bar-footer">
        <button className="btn btn-primary" onClick={onSearch}>
          조회
        </button>
        {onReset && (
          <button className="btn btn-secondary" onClick={onReset}>
            초기화
          </button>
        )}
      </div>
    </div>
  );
}

export default SearchBar;
