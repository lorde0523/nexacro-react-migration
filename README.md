# Nexacro 14 → React 17 + Spring Boot Migration

Nexacro 14 + Nexcore 기반 레거시 시스템을 React 17 + Spring Boot로 마이그레이션하는 프로젝트입니다.

## 기술 스택

### Frontend
- React 17 + Vite
- AG Grid Enterprise
- Apache ECharts
- Froala Editor
- Axios
- React Router v5

### Backend
- Spring Boot
- MyBatis
- REST API (JSON)

## 디렉터리 구조

```
frontend/
├── src/
│   ├── components/     # 공통 컴포넌트
│   ├── pages/          # 화면 단위 컴포넌트
│   ├── hooks/          # Custom Hooks
│   ├── utils/          # 공통 유틸 함수
│   ├── api/            # Axios 인스턴스 및 API 호출
│   └── assets/         # 정적 리소스

backend/
├── src/main/java/
│   ├── controller/     # REST Controller
│   ├── service/        # Service Layer
│   ├── mapper/         # MyBatis Mapper
│   └── dto/            # Data Transfer Objects
```
