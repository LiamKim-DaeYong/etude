# Etude

주문·재고·결제 시스템을 중심으로, 높은 동시성과 분산 환경에서 생기는 설계 문제를 직접 구현하고 실험으로 검증하는 프로젝트다.

## 구성

| 경로 | 내용 |
| --- | --- |
| [server](server) | 백엔드 서버. Kotlin + Spring Boot, Spring Modulith 기반 모듈러 모놀리스 |
| [web](web) | 웹 프런트엔드. 기술 스택 미정 |
| [docs](docs/README.md) | 설계 결정(ADR)과 개념 노트 |

## 백엔드 실행

```bash
cd server
./gradlew build        # Windows: .\gradlew.bat build
./gradlew bootRun      # compose.yaml의 PostgreSQL을 함께 기동
```

빌드(테스트)와 실행 모두 Docker가 필요하다. 테스트는 Testcontainers로, 로컬 실행은 Docker Compose로 PostgreSQL을 띄운다.

의존성 버전은 [server/gradle/libs.versions.toml](server/gradle/libs.versions.toml)에서 관리한다.
