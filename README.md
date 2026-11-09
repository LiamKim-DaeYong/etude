# Etude

여러 중소 제조사가 함께 쓰는 업무 시스템(주문·재고·생산)과 홈페이지를 만들며, 데이터 격리·동시성·외부 연동에서 생기는 설계 문제를 직접 구현하고 실험으로 검증하는 프로젝트다. 방향은 [ADR-0005](docs/decisions/0005-product-direction.md)에 정리한다.

## 구성

| 경로 | 내용 |
| --- | --- |
| [server](server) | 백엔드 서버. Kotlin + Spring Boot, Spring Modulith 기반 모듈러 모놀리스 |
| [web](web) | 관리자 웹과 공통 UI. React + TypeScript + Vite, shadcn/ui + Tailwind CSS |
| [docs](docs/README.md) | 설계 결정(ADR)과 개념 노트 |

## 백엔드 실행

```bash
cd server
./gradlew build        # Windows: .\gradlew.bat build
./gradlew bootRun      # local 프로필, compose.yaml의 PostgreSQL을 함께 기동
```

빌드(테스트)와 실행 모두 Docker가 필요하다. 테스트는 Testcontainers로, 로컬 실행은 Docker Compose로 PostgreSQL을 띄운다.

API 계약은 코드에서 생성한 OpenAPI 명세다. 고객 API는 [server/openapi/customer.json](server/openapi/customer.json), 관리자 API는 [server/openapi/admin.json](server/openapi/admin.json)이며, 코드와 다르면 테스트가 실패한다. 로컬 실행 중에는 `http://localhost:8080/swagger-ui.html`에서 볼 수 있다. 운영 기본 설정에서는 명세와 Swagger UI가 꺼져 있다.

의존성 버전은 [server/gradle/libs.versions.toml](server/gradle/libs.versions.toml)에서 관리한다.

## 프런트엔드 실행

Node.js 24를 기준으로 한다. BE와 Docker 없이 UI를 실행할 수 있다.

```bash
cd web
npm ci
npm run dev
```

`http://127.0.0.1:5173`에서 확인한다. PowerShell에서 npm 실행이 차단되면 `npm.cmd`를 사용한다. 구조·검사·보안 정책은 [web/README.md](web/README.md)에 정리한다.

## 자동 검사

PR과 `main` 푸시에서 BE의 `CI / build`와 FE의 `Web CI / web`을 독립적으로 실행한다. FE는 Node.js 24에서 의존성 감사, 타입·lint·포맷·빌드, lint 규칙 검사와 Chromium 데스크톱·모바일 검사를 수행한다. 실패한 브라우저 검사 보고서·추적 파일은 7일간 보관한다.

`main` ruleset의 필수 검사는 GitHub Actions의 `build`와 `web`이다. 두 검사를 통과한 PR만 Squash 머지한다.

## 의존성 업데이트

[Dependabot 설정](.github/dependabot.yml)은 매주 월요일 한국 시간 기준 FE npm 의존성(04:00)과 GitHub Actions(04:15)를 확인한다. npm PR은 `area:frontend`, Actions PR은 `area:shared`로 구분한다. React·Tailwind·Playwright 관련 의존성은 함께 검토할 수 있도록 묶고, Actions의 minor·patch 업데이트도 묶는다. 버전 업데이트 PR 수는 각각 5개·2개로 제한한다.

업데이트는 자동으로 머지하지 않는다. 변경 사항과 라이선스·원본 UI 수정 영향을 검토하고 두 필수 CI를 통과한 뒤 승인된 범위에서 반영한다. 봇의 첫 실행 결과와 취약점 해결 여부는 실제 PR·감사 결과로 확인한다.
