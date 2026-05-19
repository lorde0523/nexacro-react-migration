# Backend-First Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a Spring Boot backend-first migration scaffold that analyzes Nexacro `.xfdl/.xjs` files and produces API conversion candidates before React work starts.

**Architecture:** The project is a Spring Boot application with a reusable analysis core, a REST endpoint for running analysis, and report renderers for JSON, Markdown, and CSV. The analyzer extracts datasets, transaction calls, search parameter candidates, and source-code mapping hints from configurable directories.

**Tech Stack:** Java 17, Spring Boot, MyBatis starter, JUnit 5, Maven.

---

### Task 1: Project Skeleton And Analyzer Contract

**Files:**
- Create: `pom.xml`
- Create: `src/test/java/com/lorde0523/migration/analysis/NexacroContractExtractorTest.java`
- Create: `src/main/java/com/lorde0523/migration/MigrationApplication.java`
- Create: `src/main/java/com/lorde0523/migration/analysis/model/*.java`
- Create: `src/main/java/com/lorde0523/migration/analysis/parser/NexacroContractExtractor.java`

- [x] **Step 1: Write failing tests**

Tests define that `.xfdl` datasets, transaction calls, input/output dataset mappings, and search parameter candidates are extracted from sample Nexacro scripts.

- [x] **Step 2: Run tests to verify failure**

Run: `mvn test -Dtest=NexacroContractExtractorTest`
Expected: compile failure before implementation classes exist.

- [x] **Step 3: Implement minimal analyzer**

Implement DOM dataset parsing and regex-based transaction/script usage scanning.

- [ ] **Step 4: Run tests to verify pass**

Run: `mvn test`
Expected: tests pass.
Current local blocker: Maven is not installed on this workstation, so `mvn test` cannot run here yet.

### Task 2: Backend API Scaffold

**Files:**
- Create: `src/main/java/com/lorde0523/migration/common/ApiResponse.java`
- Create: `src/main/java/com/lorde0523/migration/common/PageResponse.java`
- Create: `src/main/java/com/lorde0523/migration/session/SessionVo.java`
- Create: `src/main/java/com/lorde0523/migration/analysis/NexacroAnalysisService.java`
- Create: `src/main/java/com/lorde0523/migration/analysis/web/MigrationAnalysisController.java`
- Create: `src/main/java/com/lorde0523/migration/config/MyBatisConfig.java`

- [x] **Step 1: Add common response/session types**

Keep types small so future generated APIs can reuse them.

- [x] **Step 2: Add REST analysis endpoint**

Expose `POST /api/migration/analyze` with Nexacro and legacy server source paths.

- [x] **Step 3: Add MyBatis configuration placeholder**

Enable mapper scanning for future generated API modules.

### Task 3: Report Renderers And Documentation

**Files:**
- Create: `src/main/java/com/lorde0523/migration/analysis/report/MigrationReportRenderer.java`
- Create: `README.md`
- Create: `.gitignore`

- [x] **Step 1: Add Markdown and CSV renderers**

Render screen ID, file path, transaction name, service URL, datasets, parameter candidates, legacy source hints, and endpoint candidates.

- [x] **Step 2: Document the backend-first workflow**

Explain input folders, API endpoint, report formats, and implementation priority.

### Task 4: Verification And Version Control

**Files:**
- Modify generated project files as needed.

- [ ] **Step 1: Run verification**

Run: `mvn test`
Expected: all tests pass.

- [ ] **Step 2: Commit and push**

Run:
```bash
git add .
git commit -m "feat: scaffold backend-first migration analyzer"
git push
```
