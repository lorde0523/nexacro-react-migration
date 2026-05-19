# Nexacro React Migration

Backend-first migration scaffold for converting Nexacro 14 transactions into Spring Boot API contracts.

## Current Scope

This repository starts with the backend migration layer only. React screens are intentionally deferred until publishing output is ready.

The first deliverable is an automated mapping report:

- Screen ID and `.xfdl` file path
- Nexacro transaction name and service URL
- Input and output dataset mappings
- Search parameter candidates from dataset usage
- Dataset column candidates
- Legacy server source mapping hints
- Spring Boot endpoint, request DTO, and response DTO candidates

## Project Structure

```text
src/main/java/com/lorde0523/migration
  analysis/          Nexacro analysis service
  analysis/model/    Report DTOs and extracted contract models
  analysis/parser/   .xfdl/.xjs parser
  analysis/report/   Markdown and CSV report renderers
  analysis/web/      Migration analysis API
  common/            ApiResponse and paging response
  config/            MyBatis mapper scan setup
  session/           SessionVo placeholder for secured APIs
```

## Analyze Nexacro Sources

Run the Spring Boot app, then call:

```bash
curl -X POST http://localhost:8080/api/migration/analyze \
  -H "Content-Type: application/json" \
  -d '{"nexacroRoot":"C:/path/to/nexacro","legacyServerRoot":"C:/path/to/legacy-server"}'
```

Markdown report:

```bash
curl -X POST "http://localhost:8080/api/migration/analyze/report?format=markdown" \
  -H "Content-Type: application/json" \
  -d '{"nexacroRoot":"C:/path/to/nexacro","legacyServerRoot":"C:/path/to/legacy-server"}'
```

CSV report:

```bash
curl -X POST "http://localhost:8080/api/migration/analyze/report?format=csv" \
  -H "Content-Type: application/json" \
  -d '{"nexacroRoot":"C:/path/to/nexacro","legacyServerRoot":"C:/path/to/legacy-server"}'
```

## Backend Implementation Priority

1. Common code, common combo, and post-login base lookup APIs
2. Search and list APIs
3. Save, update, and delete APIs
4. Batch, Excel, file, mail, and special functions
5. React integration after publishing is ready

## Verification

```bash
mvn test
```

The first analyzer test uses sample `.xfdl/.xjs` content and checks dataset extraction, transaction parsing, search parameter candidates, and generated endpoint candidates.
