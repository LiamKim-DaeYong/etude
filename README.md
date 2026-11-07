# Etude

주문·재고·결제 시스템을 중심으로, 높은 동시성과 분산 환경에서 생기는 설계 문제를 직접 구현하고 실험으로 검증하는 프로젝트다.

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
./gradlew bootRun      # compose.yaml의 PostgreSQL을 함께 기동
```

빌드(테스트)와 실행 모두 Docker가 필요하다. 테스트는 Testcontainers로, 로컬 실행은 Docker Compose로 PostgreSQL을 띄운다.

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
