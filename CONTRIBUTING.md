# Contributing

Etude는 저장소 주인 1명이 운영하고, 백엔드(`server/`)는 Claude, 프런트엔드(`web/`)는 Codex가 작업한다. 실제 팀 워크플로우를 따른다.

## 브랜치 전략

```
<type>/<issue-number>-<short-description>
```

- `type`: `feature` / `fix` / `infra` / `experiment` / `refactor` / `docs` / `test` / `chore`
- 예: `feature/3-order-api`, `fix/7-stock-negative`, `experiment/5-lock-comparison`
- `main`에 직접 커밋하지 않는다. 모든 변경은 PR을 통해 들어온다.
- 브랜치는 작업(이슈)마다 최신 `main`에서 새로 만든다. 작업 공간(worktree)의 고정 브랜치에서 직접 작업하지 않는다.

## 커밋 메시지

[Conventional Commits](https://www.conventionalcommits.org/)를 따른다.

```
<type>(<scope>): <subject>

<body (선택)>

Refs #<issue-number>
```

- `type`: `feat`, `fix`, `chore`, `refactor`, `docs`, `test`, `perf`
- `scope`: 백엔드는 모듈 이름 (`order`, `inventory`, `payment` 등), 프런트엔드는 앱·패키지 이름
- 예: `feat(order): 주문 생성 API 추가`

## PR 규칙

- PR 템플릿(`.github/PULL_REQUEST_TEMPLATE.md`)을 채운다.
- 관련 Issue를 `Closes #<번호>`로 연결한다. 머지 시 이슈가 자동으로 닫힌다.
- CI 필수 체크를 통과해야 한다.
- 머지는 저장소 주인이 승인한 뒤 한다.
- 머지 방식은 **Squash merge만 허용**한다.
- 머지 후 브랜치는 자동으로 삭제된다.

## 이슈

- Issue Template(`.github/ISSUE_TEMPLATE/`)을 사용한다: Feature / Bug / Infra / Experiment.
- 라벨
  - 유형: `type:feature`, `type:bug`, `type:infra`, `type:experiment`
  - 영역: `area:backend`, `area:frontend`, `area:shared`(루트 문서, `.github/`, 저장소 공통 설정)
  - 표시: `api-change`(API 경로·요청·응답 형식이 바뀌는 PR)
- 실험 결과가 설계 결정으로 이어지면 `docs/decisions/`에 ADR로 남긴다.

## 협업 규칙 (BE·FE)

담당자가 모두 같은 계정이므로 작업 영역은 라벨로 구분하고, 진행 상황은 말로 전달하지 않고 이슈와 PR에 남긴다.

- **이슈는 한 영역만 맡는다.** 모든 이슈와 PR에 `area:` 라벨을 하나 붙인다. BE·FE에 걸친 기능은 상위 이슈를 만들고, 그 아래 영역별 하위 이슈(sub-issue)로 나눈다.
- **의존 관계를 적는다.** 다른 영역의 작업을 기다리는 이슈는 본문에 `#<번호> 완료 후 진행`을 쓰거나 blocked-by로 연결한다.
- **API 변경을 알린다.** API 경로, 요청, 응답, 오류 형식을 바꾸는 BE PR은 `api-change` 라벨을 붙이고 PR 템플릿의 "API 변경"에 바뀐 내용과 하위 호환 여부를 적는다. FE는 이 라벨로 연동할 변경을 찾는다.
- **PR 하나는 한 영역만 고친다.** `server/`와 `web/`을 한 PR에서 함께 고치지 않는다. 공유 파일(루트 `README.md`·`CONTRIBUTING.md`·`AGENTS.md`, `.github/`, `docs/README.md`)은 `area:shared` PR로 따로 고친다. 루트 `CLAUDE.md`는 Claude 전용 백엔드 규칙이므로 백엔드 영역에 속한다.
- **ADR 번호는 PR을 올릴 때 정한다.** `main`에 있는 마지막 번호 다음 번호를 쓴다. 번호를 미리 예약하지 않는다. 충돌하면 나중에 머지하는 쪽이 번호를 바꾼다.

## 작성 언어

| 대상 | 언어 |
|---|---|
| 커밋 메시지 | 한글 (`<type>(<scope>): ` 프리픽스는 영문 유지) |
| PR 제목 | 한글 (`<type>: ` 프리픽스는 영문 유지) |
| PR 본문 | 한글 |
| Issue 제목 / 본문 | 한글 |
| 코드 주석 / 식별자 | 영문 |
| 문서 (`README`, `CONTRIBUTING`, ADR) | 한글 |
| `AGENTS.md`, `CLAUDE.md` (에이전트 지침) | 영문 |
