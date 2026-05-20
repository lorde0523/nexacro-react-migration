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

## 입력 경로 의미

API 요청에서 사용하는 주요 경로는 아래와 같습니다.

```json
{
  "nexacroRoot": "Nexacro 화면 파일(.xfdl/.xjs)이 있는 폴더",
  "legacyServerRoot": "Nexcore BizUnit XML 등 서버 메타 파일 폴더",
  "legacyJavaRoot": "P/F/D Java 파일이 있는 폴더",
  "legacyDbRoot": "iBatis SQL Map XML이 있는 DB 폴더",
  "generationOutputRoot": "생성 결과를 저장할 폴더",
  "basePackage": "생성될 Spring Boot 코드의 패키지명"
}
```

`legacyDbRoot`는 고정 경로가 아닙니다. Nexcore/iBatis SQL Map XML은 프로젝트마다 `DB` 폴더 위치가 다를 수 있으므로 요청값으로 변경할 수 있습니다.

백엔드 P/F/D 분석만 먼저 하고 싶어도 현재 `nexacroRoot`는 필수값입니다. Nexacro 파일이 아직 없으면 존재하는 빈 폴더를 만들어서 넣으면 됩니다.

## 실행 방법 1: PowerShell/curl

### 1. Spring Boot 서버 실행

PowerShell에서 프로젝트 폴더로 이동한 뒤 서버를 실행합니다.

```powershell
cd C:\Users\Mint\Documents\migration
gradle bootRun
```

정상 실행되면 콘솔에 대략 아래와 같은 로그가 나옵니다.

```text
Tomcat started on port 8080
Started MigrationApplication
```

이 터미널은 서버 프로세스이므로 닫지 말고 그대로 둡니다.

### 2. API 호출

새 PowerShell 창을 열고 `curl.exe`로 API를 호출합니다. Windows PowerShell에서는 `curl`이 alias일 수 있으므로 `curl.exe`를 쓰는 것이 안전합니다.

```powershell
curl.exe -X POST "http://localhost:8080/api/migration/analyze" `
  -H "Content-Type: application/json" `
  -d "{
    `"nexacroRoot`": `"C:/path/to/nexacro`",
    `"legacyServerRoot`": `"C:/path/to/legacy-server`",
    `"legacyJavaRoot`": `"C:/path/to/src/java/z/zz/zzz/zzzz`",
    `"legacyDbRoot`": `"C:/path/to/DB`",
    `"generationOutputRoot`": `"build/generated-migration`",
    `"basePackage`": `"com.example.migrated`"
  }"
```

실제 사용 시 `C:/path/to/...` 값은 각 프로젝트 경로에 맞게 바꿉니다.

## 실행 방법 2: IntelliJ IDEA

### 1. 프로젝트 열기

1. IntelliJ IDEA 실행
2. `Open` 선택
3. `C:\Users\Mint\Documents\migration` 폴더 선택
4. Gradle 프로젝트로 import

처음 열면 Gradle dependency를 내려받기 때문에 시간이 조금 걸릴 수 있습니다.

### 2. JDK 21 설정

`File > Project Structure > Project`에서 아래처럼 설정합니다.

- SDK: Java 21
- Language level: 21

Java 21 SDK가 보이지 않으면 `Add SDK`로 아래 경로를 지정합니다.

```text
C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot
```

### 3. Spring Boot 실행

아래 파일을 엽니다.

```text
src/main/java/com/lorde0523/migration/MigrationApplication.java
```

클래스 옆 초록색 실행 버튼을 누르고 `Run MigrationApplication`을 선택합니다.

또는 오른쪽 Gradle 탭에서 아래 task를 실행해도 됩니다.

```text
Tasks > application > bootRun
```

### 4. IntelliJ HTTP Client로 API 호출

프로젝트 루트에 `request.http` 파일을 만들고 아래 내용을 넣습니다.

```http
POST http://localhost:8080/api/migration/analyze
Content-Type: application/json

{
  "nexacroRoot": "C:/path/to/nexacro",
  "legacyServerRoot": "C:/path/to/legacy-server",
  "legacyJavaRoot": "C:/path/to/src/java/z/zz/zzz/zzzz",
  "legacyDbRoot": "C:/path/to/DB",
  "generationOutputRoot": "build/generated-migration",
  "basePackage": "com.example.migrated"
}
```

`POST http://...` 왼쪽에 뜨는 초록색 실행 버튼을 누르면 API가 호출됩니다.

## 생성 결과 확인

API 호출이 성공하면 `generationOutputRoot` 아래에 결과가 생성됩니다. 기본 예시는 아래와 같습니다.

```text
build/generated-migration
  migration-report.md
  src/main/java/<basePackage>/controller
  src/main/java/<basePackage>/service
  src/main/java/<basePackage>/mapper
  src/main/resources/mapper
```

먼저 `migration-report.md`를 열어서 어떤 `P -> F -> D` 흐름이 잡혔고, 어떤 iBatis 쿼리가 MyBatis 후보로 변환됐는지 확인합니다.

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
