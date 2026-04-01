// [MIGRATION] AS-IS: Nexacro 공통 유틸 함수 (fn_, util_, common_ 접두사)
// [TO-BE]: React 17 공통 유틸 함수 (/src/utils/commonUtils.js)

/**
 * 날짜 포맷 변환 (Nexacro fn_dateFormat 대응)
 * @param {string|Date} date - 날짜 값
 * @param {string} format - 포맷 문자열 (예: 'YYYY-MM-DD')
 * @returns {string}
 */
export const fn_dateFormat = (date, format = 'YYYY-MM-DD') => {
  if (!date) return '';
  const d = new Date(date);
  if (isNaN(d.getTime())) return '';

  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const seconds = String(d.getSeconds()).padStart(2, '0');

  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds);
};

/**
 * 숫자 포맷 변환 - 천 단위 콤마 (Nexacro fn_numberFormat 대응)
 * @param {number|string} value - 숫자 값
 * @param {number} decimals - 소수점 자리수
 * @returns {string}
 */
export const fn_numberFormat = (value, decimals = 0) => {
  if (value === null || value === undefined || value === '') return '';
  const num = parseFloat(value);
  if (isNaN(num)) return '';
  return num.toLocaleString('ko-KR', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
};

/**
 * 문자열 null/empty 체크 (Nexacro fn_isNull 대응)
 * @param {*} value
 * @returns {boolean}
 */
export const fn_isNull = (value) => {
  return value === null || value === undefined || value === '';
};

/**
 * 문자열 trim (Nexacro fn_trim 대응)
 * @param {string} str
 * @returns {string}
 */
export const fn_trim = (str) => {
  if (fn_isNull(str)) return '';
  return String(str).trim();
};

/**
 * 날짜 범위 유효성 검사 (Nexacro fn_validateDateRange 대응)
 * @param {string} startDate - 시작일 (YYYY-MM-DD)
 * @param {string} endDate - 종료일 (YYYY-MM-DD)
 * @returns {boolean}
 */
export const fn_validateDateRange = (startDate, endDate) => {
  if (!startDate || !endDate) return true;
  return new Date(startDate) <= new Date(endDate);
};

/**
 * 오늘 날짜 반환 (Nexacro fn_getToday 대응)
 * @param {string} format - 포맷 문자열
 * @returns {string}
 */
export const fn_getToday = (format = 'YYYY-MM-DD') => {
  return fn_dateFormat(new Date(), format);
};

/**
 * 현재 연도 반환
 * @returns {string}
 */
export const fn_getCurrentYear = () => {
  return String(new Date().getFullYear());
};

/**
 * Alert 메시지 (Nexacro fn_alert 대응)
 * @param {string} message
 */
export const fn_alert = (message) => {
  alert(message);
};

/**
 * Confirm 메시지 (Nexacro fn_confirm 대응)
 * @param {string} message
 * @returns {boolean}
 */
export const fn_confirm = (message) => {
  return window.confirm(message);
};

/**
 * 전화번호 포맷 변환 (010-1234-5678)
 * @param {string} phone
 * @returns {string}
 */
export const util_phoneFormat = (phone) => {
  if (!phone) return '';
  const cleaned = phone.replace(/\D/g, '');
  if (cleaned.length === 11) {
    return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 7)}-${cleaned.slice(7)}`;
  }
  if (cleaned.length === 10) {
    return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 6)}-${cleaned.slice(6)}`;
  }
  return phone;
};

/**
 * 사업자번호 포맷 변환 (000-00-00000)
 * @param {string} bizNo
 * @returns {string}
 */
export const util_bizNoFormat = (bizNo) => {
  if (!bizNo) return '';
  const cleaned = bizNo.replace(/\D/g, '');
  if (cleaned.length === 10) {
    return `${cleaned.slice(0, 3)}-${cleaned.slice(3, 5)}-${cleaned.slice(5)}`;
  }
  return bizNo;
};

/**
 * 파일 크기 포맷 변환
 * @param {number} bytes
 * @returns {string}
 */
export const util_fileSizeFormat = (bytes) => {
  if (!bytes) return '0 B';
  const sizes = ['B', 'KB', 'MB', 'GB'];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  return `${(bytes / Math.pow(1024, i)).toFixed(1)} ${sizes[i]}`;
};

/**
 * 객체 깊은 복사 (Nexacro Dataset clone 대응)
 * @param {*} obj
 * @returns {*}
 */
export const common_deepCopy = (obj) => {
  return JSON.parse(JSON.stringify(obj));
};
