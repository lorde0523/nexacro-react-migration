# Nexcore Spring Boot 마이그레이션

이 프로젝트는 Nexcore 백엔드를 Gradle, Spring Boot 3.x, Java 21 기반으로 전환하기 위한 분석 및 코드 생성 도구입니다.

기존 Nexacro/Nexcore 자산을 분석하고, Nexcore Java 소스에서 흔히 사용하는 `P*`, `F*`, `D*` 계층 규칙을 기준으로 Spring Boot 코드 초안을 생성합니다.

- `P*`: Controller 후보
- `F*`: Service 후보
- `D*`: Data Access 후보

`D*` 클래스에서 iBatis SQL Map statement를 호출하는 경우, 설정 가능한 DB XML 폴더를 스캔해서 MyBatis Mapper 후보를 생성합니다.

## 기술 스택

- Gradle
- Java 21 toolchain
- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- MyBatis Spring Boot Starter
- JavaParser
- JUnit 5

## 분석 및 생성 실행

Spring Boot 애플리케이션을 실행한 뒤 아래 API를 호출합니다.

```bash
curl -X POST http://localhost:8080/api/migration/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "nexacroRoot": "C:/path/to/nexacro",
    "legacyServerRoot": "C:/path/to/legacy-server",
    "legacyJavaRoot": "C:/path/to/src/java/z/zz/zzz/zzzz",
    "legacyDbRoot": "C:/path/to/DB",
    "generationOutputRoot": "build/generated-migration",
    "basePackage": "com.example.migrated"
  }'
```

`legacyDbRoot`는 고정 경로가 아닙니다. Nexcore/iBatis SQL Map XML은 프로젝트마다 `DB` 폴더 위치가 다를 수 있으므로 요청값으로 변경할 수 있게 되어 있습니다.

생성 결과는 `generationOutputRoot` 아래에 만들어집니다.

```text
build/generated-migration
  migration-report.md
  src/main/java/<basePackage>/controller
  src/main/java/<basePackage>/service
  src/main/java/<basePackage>/mapper
  src/main/resources/mapper
```

## 마이그레이션 규칙

1. `P*` 파일을 마이그레이션 진입점으로 처리합니다.
2. `P*`의 public 메서드는 REST endpoint 후보가 됩니다.
3. `P -> F -> D` 호출 흐름은 메서드 이름 기준으로 추적합니다.
4. iBatis statement id를 참조하는 `D*` 메서드는 `MYBATIS_REQUIRED`로 표시합니다.
5. iBatis statement id가 감지되지 않은 `D*` 메서드는 수동 검토용 `JPA_CANDIDATE`로 표시합니다.
6. `D*`에서 참조한 statement id를 DB XML에서 찾지 못하면 수동 확인 경고를 리포트에 남깁니다.

## 생성 산출물

생성되는 코드는 운영 코드에 바로 반영하는 완성본이 아니라, 사람이 검토하고 보정하기 위한 Spring Boot 마이그레이션 초안입니다.

- `controller`: `P*` 기반 REST Controller 후보
- `service`: `F*` 기반 Service 후보
- `mapper`: `D*` 및 iBatis statement 기반 MyBatis Mapper interface 후보
- `resources/mapper`: iBatis XML을 변환한 MyBatis XML 후보
- `migration-report.md`: P/F/D 흐름, MyBatis 변환 여부, 수동 확인 지점

## 검증

```bash
gradle test --no-daemon
```
