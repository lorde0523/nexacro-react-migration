// [MIGRATION] AS-IS: Nexacro WebEditor 컴포넌트
// [TO-BE]: Froala Editor 래퍼 컴포넌트
// Editor: Froala (react-froala-wysiwyg)

import React from 'react';
import FroalaEditorComponent from 'react-froala-wysiwyg';
import 'froala-editor/css/froala_style.min.css';
import 'froala-editor/css/froala_editor.pkgd.min.css';

/**
 * Froala Editor 래퍼 컴포넌트 (Nexacro WebEditor 대응)
 * @param {string} model - 에디터 내용 (Nexacro getInnerHTML() → model state)
 * @param {Function} onModelChange - 내용 변경 콜백 (state setter)
 * @param {Object} config - Froala Editor 설정 옵션
 * @param {string} imageUploadURL - 이미지 업로드 URL
 * @param {string} fileUploadURL - 파일 업로드 URL
 *
 * Nexacro WebEditor 변환:
 * - getInnerHTML() → model state로 관리
 * - setInnerHTML() → model prop으로 주입
 * - 파일 업로드 → imageUploadURL / fileUploadURL 옵션으로 연결
 */
function CommonEditor({
  model = '',
  onModelChange,
  config = {},
  imageUploadURL = '/api/upload/image',
  fileUploadURL = '/api/upload/file',
}) {
  const defaultConfig = {
    placeholderText: '내용을 입력하세요.',
    charCounterCount: true,
    height: 400,
    language: 'ko',
    imageUploadURL,
    fileUploadURL,
    imageUploadParams: { id: 'my_editor' },
    events: {
      'image.beforeUpload': function (images) {
        // 이미지 업로드 전처리
        return true;
      },
    },
    toolbarButtons: {
      moreText: {
        buttons: [
          'bold', 'italic', 'underline', 'strikeThrough',
          'subscript', 'superscript', 'fontFamily', 'fontSize',
          'textColor', 'backgroundColor', 'inlineClass',
          'inlineStyle', 'clearFormatting',
        ],
      },
      moreParagraph: {
        buttons: [
          'alignLeft', 'alignCenter', 'formatOLSimple',
          'alignRight', 'alignJustify', 'formatOL', 'formatUL',
          'paragraphFormat', 'paragraphStyle', 'lineHeight',
          'outdent', 'indent', 'quote',
        ],
      },
      moreRich: {
        buttons: [
          'insertLink', 'insertImage', 'insertVideo',
          'insertTable', 'emoticons', 'fontAwesome',
          'specialCharacters', 'embedly', 'insertFile', 'insertHR',
        ],
      },
      moreMisc: {
        buttons: ['undo', 'redo', 'fullscreen', 'print', 'getPDF', 'spellChecker', 'selectAll', 'html', 'help'],
        align: 'right',
        buttonsVisible: 2,
      },
    },
    ...config,
  };

  return (
    <div className="common-editor-wrapper">
      <FroalaEditorComponent
        tag="textarea"
        config={defaultConfig}
        model={model}
        onModelChange={onModelChange}
      />
    </div>
  );
}

export default CommonEditor;
