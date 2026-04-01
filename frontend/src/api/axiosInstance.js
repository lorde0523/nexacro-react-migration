// [MIGRATION] AS-IS: Nexacro Transaction 공통 설정
// [TO-BE]: Axios 인스턴스 (baseURL, interceptor 설정)

import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터 (Nexacro Transaction 전처리 대응)
axiosInstance.interceptors.request.use(
  (config) => {
    // 인증 토큰이 있는 경우 헤더에 추가
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 응답 인터셉터 (Nexacro Transaction 후처리 대응)
axiosInstance.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response;
      if (status === 401) {
        // 인증 만료 처리
        localStorage.removeItem('authToken');
        window.location.href = '/login';
      } else if (status === 500) {
        console.error('서버 오류:', data.message || '내부 서버 오류가 발생했습니다.');
      }
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
