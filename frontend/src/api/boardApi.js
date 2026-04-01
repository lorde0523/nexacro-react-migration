// [MIGRATION] AS-IS: Nexacro Transaction - 게시판 서비스 호출
// [TO-BE]: Axios 기반 게시판 API 호출 함수

import axiosInstance from './axiosInstance';

/**
 * 게시판 목록 조회 (Nexacro fn_search 대응)
 * @param {Object} params - 검색 조건 { title, writer, startDate, endDate, page, size }
 */
export const getBoardList = (params) => {
  return axiosInstance.get('/boards', { params });
};

/**
 * 게시글 상세 조회
 * @param {string|number} boardId - 게시글 ID
 */
export const getBoardById = (boardId) => {
  return axiosInstance.get(`/boards/${boardId}`);
};

/**
 * 게시글 등록 (Nexacro btn_save_onclick - INSERT 대응)
 * @param {Object} boardData - 게시글 데이터 { title, content, writer }
 */
export const createBoard = (boardData) => {
  return axiosInstance.post('/boards', boardData);
};

/**
 * 게시글 수정 (Nexacro btn_save_onclick - UPDATE 대응)
 * @param {string|number} boardId - 게시글 ID
 * @param {Object} boardData - 수정 데이터
 */
export const updateBoard = (boardId, boardData) => {
  return axiosInstance.put(`/boards/${boardId}`, boardData);
};

/**
 * 게시글 삭제 (Nexacro btn_delete_onclick 대응)
 * @param {string|number} boardId - 게시글 ID
 */
export const deleteBoard = (boardId) => {
  return axiosInstance.delete(`/boards/${boardId}`);
};
