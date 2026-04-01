# Project Migration Context: Nexacro 14 + Nexcore → React 17 + Spring

## 프로젝트 개요

- **AS-IS**: Nexacro 14 (Frontend) + Nexcore Framework (Backend)
- **TO-BE**: React 17 (Frontend) + Spring Boot (Backend)
- **목적**: 레거시 시스템을 현대적인 웹 스택으로 전환

-----

## 기술 스택 매핑

### Frontend

|AS-IS (Nexacro 14)     |TO-BE (React 17)                                    |
|-----------------------|----------------------------------------------------|
|Nexacro Form / xfdl    |React Function Component                            |
|Nexacro Grid           |AG Grid Enterprise                                  |
|Nexacro Chart          |Apache ECharts (`echarts-for-react`)                |
|Nexacro WebEditor      |Froala Editor                                       |
|Dataset                |React useState                                      |
|Transaction (커뮤니케이션)   |Axios + REST API                                    |
|Div / PopupDiv         |React Component / Modal                             |
|fn_init / fn_load 등 이벤트|useEffect / useCallback                             |
|setCellProperty / expr |AG Grid `cellClassRules`, `cellStyle`, `valueGetter`|

### Backend

|AS-IS (Nexcore)          |TO-BE (Spring Boot)                  |
|-------------------------|-------------------------------------|
|Nexcore Service          |Spring `@Service`                    |
|Nexcore DAO              |MyBatis Mapper                       |
|PlatformData (Dataset 통신)|REST API (JSON)                      |
|`.xpc` / `.xml` 서비스 정의   |`@RestController` + `@RequestMapping`|

-----

## React 환경 상세

- **React 버전**: 17 (StrictMode 유지)
- **빌드 도구**: Vite
- **함수형 컴포넌트** 사용 (클래스 컴포넌트 금지)
- **Hooks 기반**: `useState`, `useEffect`, `useRef`, `useCallback`, `useMemo`
- **상태관리**: useState (외부 상태관리 라이브러리 사용하지 않음)
- **HTTP 통신**: Axios
- **라우팅**: React Router v5 (React 17 호환)

-----

## AG Grid Enterprise 사용 규칙

- **버전**: AG Grid Enterprise (v27 ~ v30 범위, React 17 호환 확인 필요)
- **라이선스**: Enterprise 라이선스 적용 (`LicenseManager.setLicenseKey(...)`)
- **주요 기능 활용**:
  - Row Grouping / Aggregation
  - Server-Side Row Model (대용량 데이터)
  - Column Pinning / Range Selection
  - Excel Export (`exportDataAsExcel`)
  - Cell Merging: `rowSpan` / `colSpan` 활용
- **Nexacro Grid 변환 시 주의사항**:
  - `Dataset` 컬럼 구조 → `columnDefs`로 변환
  - `setCellProperty` / `expr` 기반 셀 스타일 → `cellClassRules` / `cellStyle` / `valueGetter`로 변환
  - Nexacro 병합 셀 → AG Grid `rowSpan` + CSS 처리
  - 헤더 병합 → `columnDefs children` 구조로 변환

-----

## Apache ECharts 사용 규칙

- **라이브러리**: `echarts` + `echarts-for-react`
- **Nexacro Chart 변환 시**:
  - Nexacro Chart 타입 → ECharts `series.type` 매핑
    - BarChart → `type: 'bar'`
    - LineChart → `type: 'line'`
    - PieChart → `type: 'pie'`
  - Nexacro Dataset 바인딩 → ECharts `option.dataset` 또는 `series.data`
  - 이벤트(onClick 등) → ECharts `onEvents` prop

-----

## Froala Editor 사용 규칙

- **라이브러리**: `react-froala-wysiwyg`
- **Nexacro WebEditor 변환 시**:
  - `getInnerHTML()` → `model` state로 관리
  - `setInnerHTML()` → `model` prop으로 주입
  - 파일 업로드 설정 → Froala `imageUploadURL` / `fileUploadURL` 옵션으로 연결

-----

## 변환 규칙 (공통)

### 이벤트 매핑

|Nexacro 이벤트         |React 대응                          |
|--------------------|----------------------------------|
|`fn_init` (폼 초기화)   |`useEffect(() => {}, [])`         |
|`fn_search` (조회)    |`useCallback` + Axios GET         |
|`btn_save_onclick`  |`onClick` handler + Axios POST/PUT|
|`grd_oncelldblclick`|AG Grid `onCellDoubleClicked`     |
|`grd_oncellclick`   |AG Grid `onCellClicked`           |

### Dataset → API 통신 변환

- Nexacro `Transaction`은 Axios 호출로 대체
- `PlatformData` 포맷(Dataset) → JSON 배열/객체로 변환
- 인풋/아웃풋 Dataset → `request body` / `response data`

### 컴포넌트 구조

- Nexacro Form 1개 → React 컴포넌트 1개 (원칙)
- 공통 팝업(PopupDiv) → `Modal` 공통 컴포넌트로 추출
- 공통 유틸 함수 (`fn_`, `util_`, `common_`) → `/src/utils/` 디렉터리로 이전

-----

## 디렉터리 구조 (TO-BE 기준)

```
frontend/src/
├── components/         # 공통 컴포넌트 (Grid, Modal, Editor 등)
├── pages/              # 화면 단위 컴포넌트 (Nexacro Form 1:1 대응)
├── hooks/              # Custom Hooks
├── utils/              # 공통 유틸 함수 (Nexacro common/util 이전)
├── api/                # Axios 인스턴스 및 API 호출 함수
└── assets/             # 정적 리소스

backend/src/main/java/com/migration/
├── controller/         # REST Controller
├── service/            # Service Layer
├── mapper/             # MyBatis Mapper Interface
├── dto/                # Data Transfer Objects
└── config/             # Configuration
```

-----

## 코드 생성 시 주의사항

1. React 17 기준으로 생성 (React 18 API 사용 금지: `createRoot` 등)
2. `class` 컴포넌트 생성 금지, 함수형 컴포넌트만 사용
3. AG Grid는 항상 Enterprise import 사용: `import 'ag-grid-enterprise'`
4. ECharts는 필요한 컴포넌트만 tree-shaking import
5. 모든 Axios 호출은 `/src/api/` 인스턴스를 통해 호출
6. Nexacro 주석 및 함수명은 최대한 보존하여 추적 가능하게 유지
7. 상태관리는 useState만 사용 (Zustand, Redux 등 외부 라이브러리 금지)
8. 빌드 도구는 Vite 사용

-----

## 파일 상단 컨텍스트 힌트 (변환 작업 시 추가)

변환 대상 파일 상단에 아래 주석을 붙여두면 Copilot이 더 정확하게 인식합니다.

```javascript
// [MIGRATION] AS-IS: Nexacro Form - 화면명.xfdl
// [TO-BE]: React 17 Function Component
// Grid: AG Grid Enterprise / Chart: ECharts / Editor: Froala
```