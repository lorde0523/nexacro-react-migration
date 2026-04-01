// [MIGRATION] AS-IS: Nexacro PopupDiv (공통 팝업)
// [TO-BE]: React 17 공통 Modal 컴포넌트

import React from 'react';
import '../assets/styles/modal.css';

/**
 * 공통 Modal 컴포넌트 (Nexacro PopupDiv 대응)
 * @param {boolean} isOpen - Modal 표시 여부
 * @param {string} title - Modal 제목
 * @param {React.ReactNode} children - Modal 내용
 * @param {Function} onClose - 닫기 콜백
 * @param {string} size - Modal 크기 ('sm' | 'md' | 'lg' | 'xl')
 * @param {boolean} showFooter - 하단 버튼 영역 표시 여부
 * @param {Function} onConfirm - 확인 버튼 콜백
 * @param {string} confirmText - 확인 버튼 텍스트
 * @param {string} cancelText - 취소 버튼 텍스트
 */
function CommonModal({
  isOpen,
  title,
  children,
  onClose,
  size = 'md',
  showFooter = true,
  onConfirm,
  confirmText = '확인',
  cancelText = '취소',
}) {
  if (!isOpen) return null;

  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose && onClose();
    }
  };

  return (
    <div className="modal-overlay" onClick={handleOverlayClick}>
      <div className={`modal-container modal-${size}`}>
        <div className="modal-header">
          <h3 className="modal-title">{title}</h3>
          <button className="modal-close-btn" onClick={onClose} aria-label="닫기">
            &times;
          </button>
        </div>
        <div className="modal-body">{children}</div>
        {showFooter && (
          <div className="modal-footer">
            {onConfirm && (
              <button className="btn btn-primary" onClick={onConfirm}>
                {confirmText}
              </button>
            )}
            <button className="btn btn-secondary" onClick={onClose}>
              {cancelText}
            </button>
          </div>
        )}
      </div>
    </div>
  );
}

export default CommonModal;
