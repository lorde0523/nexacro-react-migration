// [MIGRATION] AS-IS: Nexacro Form - EmployeeCrud.xfdl
// [TO-BE]: React 17 Function Component
// Grid: AG Grid Enterprise

import React, { useState, useEffect, useCallback } from 'react';
import CommonGrid from '../components/CommonGrid';
import CommonModal from '../components/CommonModal';
import { getEmployeeList, createEmployee, updateEmployee, deleteEmployee } from '../api/employeeApi';
import { createColumnDef, createDateColumnDef } from '../utils/gridUtils';
import { fn_alert, fn_confirm, fn_getToday } from '../utils/commonUtils';

/**
 * 사원 CRUD 화면
 * Nexacro 패턴: 목록 조회 → 행 더블클릭으로 수정 팝업 → 저장/삭제
 *
 * Nexacro 이벤트 매핑:
 * - fn_init → useEffect([], [])
 * - btn_new_onclick → handleNewClick (등록 Modal 열기)
 * - btn_delete_onclick → handleDeleteClick (삭제 확인 후 삭제)
 * - btn_save_onclick → handleSave (Axios POST/PUT)
 * - grd_oncelldblclick → handleCellDoubleClicked (수정 Modal 열기)
 */
function EmployeeCrud() {
  const [rowData, setRowData] = useState([]);
  const [loading, setLoading] = useState(false);

  // Modal 상태
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState('new'); // 'new' | 'edit'

  // 선택된 행
  const [selectedRow, setSelectedRow] = useState(null);

  // 폼 데이터 (Nexacro 입력 Dataset 대응)
  const [formData, setFormData] = useState({
    empId: '',
    empName: '',
    deptCode: '',
    deptName: '',
    position: '',
    hireDate: fn_getToday(),
    email: '',
    phone: '',
    status: '재직',
  });

  // AG Grid 컬럼 정의
  const columnDefs = [
    createColumnDef('empId', '사원번호', { width: 100, pinned: 'left' }),
    createColumnDef('empName', '사원명', { width: 120 }),
    createColumnDef('deptName', '부서명', { width: 150 }),
    createColumnDef('position', '직급', { width: 100 }),
    createDateColumnDef('hireDate', '입사일', { width: 120 }),
    createColumnDef('email', '이메일', { width: 200 }),
    createColumnDef('phone', '연락처', { width: 140 }),
    createColumnDef('status', '재직상태', { width: 100 }),
  ];

  // fn_init 대응
  const fetchEmployees = useCallback(async () => {
    setLoading(true);
    try {
      const response = await getEmployeeList({});
      setRowData(response.data);
    } catch (error) {
      console.error('사원 목록 조회 실패:', error);
      fn_alert('사원 목록 조회 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchEmployees();
  }, [fetchEmployees]);

  // btn_new_onclick 대응: 신규 등록 Modal 열기
  const handleNewClick = useCallback(() => {
    setModalMode('new');
    setFormData({
      empId: '',
      empName: '',
      deptCode: '',
      deptName: '',
      position: '',
      hireDate: fn_getToday(),
      email: '',
      phone: '',
      status: '재직',
    });
    setIsModalOpen(true);
  }, []);

  // grd_oncelldblclick 대응: 행 더블클릭 시 수정 Modal 열기
  const handleCellDoubleClicked = useCallback((params) => {
    setModalMode('edit');
    setFormData({ ...params.data });
    setIsModalOpen(true);
  }, []);

  // 행 클릭 (선택)
  const handleRowClicked = useCallback((params) => {
    setSelectedRow(params.data);
  }, []);

  // btn_delete_onclick 대응: 삭제
  const handleDeleteClick = useCallback(async () => {
    if (!selectedRow) {
      fn_alert('삭제할 사원을 선택해주세요.');
      return;
    }
    if (!fn_confirm(`${selectedRow.empName} 사원을 삭제하시겠습니까?`)) {
      return;
    }
    try {
      await deleteEmployee(selectedRow.empId);
      fn_alert('삭제되었습니다.');
      setSelectedRow(null);
      fetchEmployees();
    } catch (error) {
      console.error('사원 삭제 실패:', error);
      fn_alert('사원 삭제 중 오류가 발생했습니다.');
    }
  }, [selectedRow]);

  // btn_save_onclick 대응: 저장 (등록/수정)
  const handleSave = useCallback(async () => {
    if (!formData.empName) {
      fn_alert('사원명을 입력해주세요.');
      return;
    }
    if (!formData.deptCode) {
      fn_alert('부서코드를 입력해주세요.');
      return;
    }

    try {
      if (modalMode === 'new') {
        await createEmployee(formData);
        fn_alert('등록되었습니다.');
      } else {
        await updateEmployee(formData.empId, formData);
        fn_alert('수정되었습니다.');
      }
      setIsModalOpen(false);
      fetchEmployees();
    } catch (error) {
      console.error('사원 저장 실패:', error);
      fn_alert('저장 중 오류가 발생했습니다.');
    }
  }, [formData, modalMode]);

  const handleFormChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  return (
    <div className="page-wrapper">
      <h2 className="page-title">사원 CRUD</h2>

      {/* 버튼 영역 */}
      <div className="button-bar">
        <button className="btn btn-primary" onClick={handleNewClick}>
          신규
        </button>
        <button className="btn btn-danger" onClick={handleDeleteClick}>
          삭제
        </button>
      </div>

      {/* Grid 영역 */}
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
            showExcelButton={true}
            excelFileName="사원목록"
          />
        )}
      </div>

      {/* 등록/수정 Modal (Nexacro PopupDiv 대응) */}
      <CommonModal
        isOpen={isModalOpen}
        title={modalMode === 'new' ? '사원 등록' : '사원 수정'}
        onClose={() => setIsModalOpen(false)}
        onConfirm={handleSave}
        confirmText="저장"
        size="md"
      >
        <div className="modal-form">
          {modalMode === 'edit' && (
            <div className="form-row">
              <label>사원번호</label>
              <input type="text" name="empId" value={formData.empId} disabled />
            </div>
          )}
          <div className="form-row">
            <label>사원명 *</label>
            <input
              type="text"
              name="empName"
              value={formData.empName}
              onChange={handleFormChange}
              placeholder="사원명을 입력하세요"
            />
          </div>
          <div className="form-row">
            <label>부서코드 *</label>
            <input
              type="text"
              name="deptCode"
              value={formData.deptCode}
              onChange={handleFormChange}
              placeholder="부서코드를 입력하세요"
            />
          </div>
          <div className="form-row">
            <label>직급</label>
            <select name="position" value={formData.position} onChange={handleFormChange}>
              <option value="">선택</option>
              <option value="사원">사원</option>
              <option value="대리">대리</option>
              <option value="과장">과장</option>
              <option value="차장">차장</option>
              <option value="부장">부장</option>
              <option value="이사">이사</option>
            </select>
          </div>
          <div className="form-row">
            <label>입사일</label>
            <input
              type="date"
              name="hireDate"
              value={formData.hireDate}
              onChange={handleFormChange}
            />
          </div>
          <div className="form-row">
            <label>이메일</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleFormChange}
              placeholder="이메일을 입력하세요"
            />
          </div>
          <div className="form-row">
            <label>연락처</label>
            <input
              type="text"
              name="phone"
              value={formData.phone}
              onChange={handleFormChange}
              placeholder="010-0000-0000"
            />
          </div>
          <div className="form-row">
            <label>재직상태</label>
            <select name="status" value={formData.status} onChange={handleFormChange}>
              <option value="재직">재직</option>
              <option value="퇴직">퇴직</option>
              <option value="휴직">휴직</option>
            </select>
          </div>
        </div>
      </CommonModal>
    </div>
  );
}

export default EmployeeCrud;
