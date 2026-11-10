# Etude Web

React + TypeScript + Vite 관리자 앱과 여러 앱에서 재사용하는 공통 UI·설정이다. 현재 앱은 **로그인 이후 홈·게시판의 느낌을 확인하는 화면 초안**이다. 회사·사용자·게시글은 샘플이며 실제 인증·권한 검사·서버 저장을 구현하지 않았다. 공식 shadcn `base-nova` / Base UI와 Tailwind v4를 재사용한다.

## 실행과 검토 경로

Node.js 26.13.0을 기준으로 `.node-version`, package.json, CI를 함께 관리한다. PowerShell에서는 `npm.cmd`, `npx.cmd`를 사용할 수 있다.

```bash
cd web
npm ci
npm run dev
```

기본 개발 주소는 `http://127.0.0.1:5173`이다.

| 경로                                    | 화면                                           |
| --------------------------------------- | ---------------------------------------------- |
| `/`                                     | `/preview`로 이동                              |
| `/preview`                              | 회사·사용자 영역, 최근 게시글, 게시판 바로가기 |
| `/preview/boards/general`               | 처음 하나만 노출하는 게시판                    |
| `/preview/boards/general/posts/welcome` | 샘플 글 상세                                   |
| `/preview?data=empty`                   | 최근 게시글이 없는 홈                          |
| `/preview/boards/general?data=empty`    | 게시글이 없는 목록                             |

상단의 **샘플 글 / 글 없음**으로 상태를 바꾸며, 빈 상태는 메뉴 이동·새로고침에서도 유지된다. 인증 토큰·세션을 만들거나 API를 호출하지 않는다. `/preview`는 인증이 필요한 실제 업무 URL의 우회 경로가 아니라 공개된 샘플 UI 전용 경로다. 글 작성·수정·삭제·검색·댓글·첨부·게시판 설정은 제공하지 않는다.

홈과 게시판만 메뉴에 노출한다. 기존 UI 가이드·프로젝트 탐색·워크스페이스 시연과 별도 화면 설정 페이지는 정리했고, 화면 테마는 헤더 메뉴에서 라이트·다크·시스템 설정을 선택한다. 이전 시연 경로는 404를 표시한다. PR #116의 미병합 랜딩과 #95의 별도 미커밋 작업은 가져오거나 폐기하지 않았다.

## 구조와 연동 경계

```text
web/
├─ apps/admin/src/
│  ├─ app/routes.ts             # 라우트·메뉴·현재 위치·문서 제목
│  ├─ app/layout/               # 셸·모바일 메뉴·테마·검토 안내
│  ├─ lib/http/admin-transport.ts # 기존 요청 보안 기반, 샘플 화면 미연결
│  └─ pages/preview/            # 홈·목록·상세·검토용 데이터
├─ packages/ui/                 # 공식 UI, 폰트, 테마, 공통 화면 상태
├─ packages/config/             # CSP 등 공통 프런트엔드 설정
└─ tests/                       # 단위·설정·실제 화면 브라우저 검사
```

게시판 경로는 `boardId`를 받지만 샘플 게시판은 `general` 하나다. 목록/상세 컴포넌트와 경로를 여러 게시판에서도 재사용할 수 있게 두었으며 게시판 추가·권한 관리 UI는 만들지 않았다. `PreviewPost`와 샘플 데이터는 FE 검토용이며 BE 도메인 모델·API 계약이 아니다.

실제 연동은 승인·반영된 `server/openapi/admin.json`에서 타입을 생성하는 별도 작업이다. 기존 `admin-transport.ts`는 상대 관리자 경로·Problem 오류·CSRF 처리 기반을 유지한다. 라우팅이나 FE 셸은 서버 인증·인가·테넌트 격리를 보장하지 않는다. 게시글 내용은 React 텍스트로 렌더링하며 HTML을 주입하지 않는다. 샘플 시각은 UTC ISO-8601이고 클라이언트의 시간대로 날짜를 표시한다.

페이지는 `app/routes.ts`에 지연 로딩 컴포넌트를 등록한다. 같은 메타데이터에서 메뉴·경로·현재 위치·문서 제목을 파생한다. 404와 루트 이동은 App에서 처리한다. 페이지 전환 시 본문으로 포커스를 옮기고, 잘못된 게시판·글과 청크 로딩 실패에는 복구 경로를 제공한다.

## 공통 UI와 보안

`PageHeader`, `LoadingState`, `EmptyState`, `ErrorState`, `FormField`와 테마 Provider는 유지한다. 공통 UI는 앱·라우터·업무 규칙을 import하지 않는다. 공식 컴포넌트의 로컬 조정과 라이선스는 `packages/ui/README.md`를 따른다.

shadcn CLI와 `ui:add`는 기존 보안 사유로 중지돼 있다. `npx`로 우회하지 않는다. Pretendard와 폰트·컴포넌트 라이선스는 앱에서 직접 제공한다. 테마 초기화는 외부 `theme.js`이며 CSP의 인라인 스크립트·eval 차단을 유지한다. 인증 토큰은 localStorage에 저장하지 않는다. 테마 설정의 기존 로컬 저장만 유지한다.

## 검증

```bash
npm run check
npm run security:check
```

`check`는 타입·lint·포맷·프로덕션 빌드·단위·설정·브라우저 검사를 실행한다. 타입 검사는 TS7 `@typescript/native` alias를, ESLint는 TS6 호환 API alias를 사용한다. 브라우저 검사에는 `npx playwright install chromium`이 필요하며 빌드 결과를 전용 포트 4173에서 검사한다.

브라우저 검사는 데스크톱/모바일, 라이트/다크, 키보드 메뉴·본문 이동, 목록/상세·빈 상태·직접 URL·404, 테마 저장/시스템 설정, 오류 복구, self-hosted 한글 폰트, 실제 프로덕션 CSP를 확인한다. 삭제한 UI 가이드의 전용 예시 검사는 정리했다. 새 화면의 axe 검사는 예외 없이 수행한다. 기존 공통 Select의 네 focus guard 예외는 `AGENTS.md`와 UI 문서에 그대로 남아 있지만 이번 화면은 Select를 렌더링하지 않는다.

샘플 화면 검증은 실제 인증·테넌트 격리·게시판 API, 운영 배포, 모든 브라우저·보조공학 지원의 검증을 대신하지 않는다. 화면 캡처와 구체적인 실행 결과는 #119 및 연결 PR에 기록한다.
