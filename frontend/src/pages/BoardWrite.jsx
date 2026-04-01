// [MIGRATION] AS-IS: Nexacro Form - BoardWrite.xfdl (WebEditor 사용)
// [TO-BE]: React 17 Function Component
// Editor: Froala (react-froala-wysiwyg)

import React, { useState, useEffect, useCallback } from 'react';
import { useHistory, useParams } from 'react-router-dom';
import CommonEditor from '../components/CommonEditor';
import { createBoard, updateBoard, getBoardById } from '../api/boardApi';
import { fn_alert, fn_confirm, fn_isNull } from '../utils/commonUtils';

/**
 * 게시판 작성/수정 화면
 * Nexacro 패턴: WebEditor를 사용한 글쓰기
 *
 * Nexacro WebEditor 변환:
 * - getInnerHTML() → model state로 관리
 * - setInnerHTML(html) → model prop으로 주입 (useEffect에서 초기값 설정)
 *
 * Nexacro 이벤트 매핑:
 * - fn_init → useEffect([], [])
 * - btn_save_onclick → handleSave (Axios POST/PUT)
 * - btn_cancel_onclick → handleCancel
 */
function BoardWrite() {
  const history = useHistory();
  const { id } = useParams(); // React Router v5 useParams
  const isEdit = !fn_isNull(id);

  // 폼 데이터
  const [formData, setFormData] = useState({
    title: '',
    writer: '',
    content: '',
  });

  // Froala Editor 모델 (Nexacro getInnerHTML/setInnerHTML 대응)
  const [editorModel, setEditorModel] = useState('');
  const [loading, setLoading] = useState(false);

  // fn_init 대응: 수정 모드인 경우 기존 데이터 조회
  const fetchBoardData = useCallback(async () => {
    setLoading(true);
    try {
      const response = await getBoardById(id);
      const board = response.data;
      setFormData({
        title: board.title || '',
        writer: board.writer || '',
        content: board.content || '',
      });
      // Nexacro setInnerHTML() 대응: model prop으로 에디터 내용 주입
      setEditorModel(board.content || '');
    } catch (error) {
      console.error('게시글 조회 실패:', error);
      fn_alert('게시글을 불러오는 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    if (isEdit) {
      fetchBoardData();
    }
  }, [isEdit, fetchBoardData]);

  // btn_save_onclick 대응: 저장
  const handleSave = useCallback(async () => {
    if (!formData.title.trim()) {
      fn_alert('제목을 입력해주세요.');
      return;
    }
    if (!formData.writer.trim()) {
      fn_alert('작성자를 입력해주세요.');
      return;
    }
    if (!editorModel.trim()) {
      fn_alert('내용을 입력해주세요.');
      return;
    }

    const saveData = {
      ...formData,
      // Nexacro getInnerHTML() 대응: editorModel state에서 HTML 내용 가져오기
      content: editorModel,
    };

    try {
      if (isEdit) {
        await updateBoard(id, saveData);
        fn_alert('수정되었습니다.');
      } else {
        await createBoard(saveData);
        fn_alert('등록되었습니다.');
      }
      history.push('/board');
    } catch (error) {
      console.error('게시글 저장 실패:', error);
      fn_alert('저장 중 오류가 발생했습니다.');
    }
  }, [formData, editorModel, isEdit, id, history]);

  // btn_cancel_onclick 대응
  const handleCancel = useCallback(() => {
    if (fn_confirm('작성을 취소하시겠습니까? 입력하신 내용이 사라집니다.')) {
      history.push('/board');
    }
  }, [history]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  if (loading) {
    return <div className="loading-spinner">데이터 로딩 중...</div>;
  }

  return (
    <div className="page-wrapper">
      <h2 className="page-title">{isEdit ? '게시글 수정' : '게시글 작성'}</h2>

      <div className="board-write-form">
        <div className="form-row">
          <label className="form-label required">제목</label>
          <input
            type="text"
            name="title"
            value={formData.title}
            onChange={handleInputChange}
            placeholder="제목을 입력하세요"
            className="form-input"
          />
        </div>
        <div className="form-row">
          <label className="form-label required">작성자</label>
          <input
            type="text"
            name="writer"
            value={formData.writer}
            onChange={handleInputChange}
            placeholder="작성자를 입력하세요"
            className="form-input"
          />
        </div>

        {/* Froala Editor (Nexacro WebEditor 대응) */}
        <div className="form-row editor-row">
          <label className="form-label required">내용</label>
          <div className="editor-wrapper">
            <CommonEditor
              model={editorModel}
              onModelChange={setEditorModel}
            />
          </div>
        </div>

        <div className="button-bar">
          <button className="btn btn-primary" onClick={handleSave}>
            {isEdit ? '수정' : '등록'}
          </button>
          <button className="btn btn-secondary" onClick={handleCancel}>
            취소
          </button>
        </div>
      </div>
    </div>
  );
}

export default BoardWrite;
