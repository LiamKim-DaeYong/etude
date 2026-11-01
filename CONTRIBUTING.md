# Contributing

Etude는 1인 개발 프로젝트지만, 실제 팀 워크플로우를 따른다.

## 브랜치 전략

```
<type>/<issue-number>-<short-description>
```

- `type`: `feature` / `fix` / `infra` / `experiment` / `refactor` / `docs` / `test` / `chore`
- 예: `feature/3-order-api`, `fix/7-stock-negative`, `experiment/5-lock-comparison`
- `main`에 직접 커밋하지 않는다. 모든 변경은 PR을 통해 들어온다.

## 커밋 메시지

[Conventional Commits](https://www.conventionalcommits.org/)를 따른다.

```
<type>(<scope>): <subject>

<body (선택)>

Refs #<issue-number>
```

- `type`: `feat`, `fix`, `chore`, `refactor`, `docs`, `test`, `perf`
- `scope`: 모듈 이름 (`order`, `inventory`, `payment` 등)
- 예: `feat(order): add order creation api`

## PR 규칙

- PR 템플릿(`.github/PULL_REQUEST_TEMPLATE.md`)을 채운다.
- 관련 Issue를 `Closes #<번호>`로 연결한다. 머지 시 이슈가 자동으로 닫힌다.
- CI(`server`의 `./gradlew build`)를 통과해야 한다.
- 머지 방식은 **Squash merge만 허용**한다.
- 머지 후 브랜치는 자동으로 삭제된다.

## 이슈

- Issue Template(`.github/ISSUE_TEMPLATE/`)을 사용한다: Feature / Bug / Infra / Experiment.
- 라벨: `type:feature`, `type:bug`, `type:infra`, `type:experiment`
- 실험 결과가 설계 결정으로 이어지면 `docs/decisions/`에 ADR로 남긴다.

## 작성 언어

| 대상 | 언어 |
|---|---|
| 커밋 메시지 | 영문 (Conventional Commits) |
| PR 제목 | 한글 (`<type>: ` 프리픽스는 영문 유지) |
| PR 본문 | 한글 |
| Issue 제목 / 본문 | 한글 |
| 코드 주석 / 식별자 | 영문 |
| 문서 (`README`, `CONTRIBUTING`, ADR) | 한글 |
| `CLAUDE.md` | 영문 |
