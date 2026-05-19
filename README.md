# Nexacro React Migration

Nexacro 14 transaction과 Nexcore BizUnit의 `transactionId`를 대조해 Spring Boot API/DTO 계약 후보를 만드는 백엔드 우선 마이그레이션 스캐폴드입니다.

## 현재 범위

이 저장소는 백엔드 마이그레이션 레이어부터 시작합니다. React 화면 이관은 퍼블리싱 산출물이 준비된 뒤 진행하도록 의도적으로 후순위로 둡니다.

첫 번째 산출물은 자동 매핑 리포트입니다.

- 화면 ID와 `.xfdl` 파일 경로
- Nexacro transaction 명과 service ID
- Nexcore BizUnit의 `transactionId` 매칭 상태
- input/output dataset 매핑
- dataset 사용처 기반 search parameter 후보
- dataset 컬럼 후보
- 기존 서버 소스 매핑 힌트
- Spring Boot endpoint, request DTO, response DTO 후보

## 프로젝트 구조

```text
src/main/java/com/lorde0523/migration
  analysis/          Nexacro 분석 서비스
  analysis/model/    리포트 DTO와 추출된 계약 모델
  analysis/parser/   .xfdl/.xjs/Nexcore BizUnit XML 파서
  analysis/report/   Markdown/CSV 리포트 렌더러
  analysis/web/      마이그레이션 분석 API
  common/            ApiResponse와 paging response
  config/            MyBatis mapper scan 설정
  session/           권한 API용 SessionVo 기본 구조
```

## Nexacro 소스 분석

Spring Boot 앱을 실행한 뒤 아래 API를 호출합니다.

```bash
curl -X POST http://localhost:8080/api/migration/analyze \
  -H "Content-Type: application/json" \
  -d '{"nexacroRoot":"C:/path/to/nexacro","legacyServerRoot":"C:/path/to/legacy-server"}'
```

Markdown 리포트:

```bash
curl -X POST "http://localhost:8080/api/migration/analyze/report?format=markdown" \
  -H "Content-Type: application/json" \
  -d '{"nexacroRoot":"C:/path/to/nexacro","legacyServerRoot":"C:/path/to/legacy-server"}'
```

CSV 리포트:

```bash
curl -X POST "http://localhost:8080/api/migration/analyze/report?format=csv" \
  -H "Content-Type: application/json" \
  -d '{"nexacroRoot":"C:/path/to/nexacro","legacyServerRoot":"C:/path/to/legacy-server"}'
```

## 분석 방식

1. `nexacroRoot` 아래의 `.xfdl` 파일을 화면 단위로 스캔합니다.
2. 같은 이름의 `.xjs` 파일이 있으면 화면 script와 합쳐서 `transaction(...)` 호출을 찾습니다.
3. `.xfdl`의 `Dataset`/`ColumnInfo`를 읽어 input/output dataset 컬럼 목록을 추출합니다.
4. `legacyServerRoot` 아래의 Nexcore BizUnit XML에서 `<transactionId>` 목록을 추출합니다.
5. Nexacro transaction의 service ID와 Nexcore `transactionId`를 비교해 `MATCHED` 또는 `NOT_FOUND` 상태를 만듭니다.
6. 매칭된 transaction의 input dataset은 `Request` DTO 후보로, output dataset은 `Response` DTO 후보로 변환합니다.

## Python 사이드 분석 도구

Spring Boot 실행 없이 파일 기준으로 빠르게 분석하려면 아래 Python 도구를 사용할 수 있습니다.

```bash
python tools/nexcore_nexacro_dataset_mapper.py \
  --nexacro-root C:/path/to/nexacro \
  --nexcore-root C:/path/to/nexcore \
  --out build/migration-analysis \
  --write-dtos
```

출력 파일:

- `build/migration-analysis/mapping-report.md`
- `build/migration-analysis/mapping-report.csv`
- `build/migration-analysis/dto/*.java` (`--write-dtos` 옵션 사용 시)

Nexcore에서 비교해야 하는 ID 태그가 `transactionId`가 아니라면 [tools/nexcore_nexacro_dataset_mapper.py](tools/nexcore_nexacro_dataset_mapper.py) 상단의 아래 값을 수정하면 됩니다.

```python
BIZUNIT_ID_TAGS = ("transactionId",)
TRANSACTION_ID_ARG_INDEX = 1
```

Nexcore BizUnit XML 예시는 아래 형식을 기준으로 합니다.

```xml
<bizUnit id="SampleBizUnit">
  <bizUnitName>샘플업무</bizUnitName>
  <componentId>sampleComponent</componentId>
  <method-list>
    <method>
      <methodId>select</methodId>
      <methodName>조회</methodName>
      <transactionId>SAMPLE_SELECT</transactionId>
    </method>
  </method-list>
</bizUnit>
```

## 백엔드 구현 우선순위

1. 공통 코드, 공통 콤보, 로그인 후 기본 조회 API
2. 조회 화면의 search/list API
3. 저장, 수정, 삭제 API
4. 배치, 엑셀, 파일, 메일, 특수 기능
5. 퍼블리싱 완료 후 React 연동

## 검증

```bash
mvn test
```

첫 번째 분석기 테스트는 샘플 `.xfdl/.xjs` 내용을 사용해 dataset 추출, transaction 파싱, search parameter 후보, 생성된 endpoint 후보를 검증합니다.
