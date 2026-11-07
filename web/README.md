# Etude Web

React + TypeScript + Vite 관리자 앱과 여러 앱에서 재사용할 공통 UI·설정을 관리한다. 현재는 BE 없이 실행하는 UI 기반이다. UI는 공식 shadcn CLI로 가져온 `base-nova` 스타일의 Base UI 컴포넌트를 사용한다.

## 실행

Node.js 24 LTS를 기준으로 하며 로컬에서는 Node.js 22.14 이상도 지원한다. PowerShell 실행 정책이 npm 래퍼를 차단하면 `npm.cmd`, `npx.cmd`를 사용한다.

```bash
cd web
npm ci
npm run dev
```

`http://127.0.0.1:5173`에서 워크스페이스(`/`), UI 가이드(`/ui`), 화면 설정(`/settings`)을 확인한다. 상단 화면 검색은 `Ctrl+K` 또는 `Cmd+K`로 연다. 문서·프로세스·모니터링은 예정 표시이며 아직 연결하지 않았다.

## 구조

```text
web/
├─ apps/admin/src/
│  ├─ app/routes.ts            # 경로·메뉴·검색·제목에 사용하는 화면 정의
│  ├─ app/layout/              # 관리자 전용 레이아웃·메뉴·화면 검색
│  └─ pages/
│     └─ ui-guide/examples/    # UI 가이드 안에서만 사용하는 예시
├─ packages/ui/
│  ├─ components.json          # 공식 레지스트리·스타일·설치 위치
│  └─ src/
│     ├─ components/           # 공식 CLI로 가져온 컴포넌트 원본
│     ├─ styles.css            # 디자인 토큰, Tailwind, 한글 폰트
│     └─ EtudeProvider.tsx     # 테마, 툴팁, 알림
├─ packages/config/            # TypeScript 기준, 배포 보안 헤더
└─ tests/                      # 배포용 빌드 대상 브라우저 검사
```

- 앱이 공통 패키지를 사용한다. 공통 UI는 앱·라우터·업무 규칙에 의존하지 않는다. ESLint가 이 경계를 검사한다.
- 고객 쇼핑몰은 `apps/storefront`, CS는 필요할 때 `apps/cs`로 추가한다. 프레임워크·라우팅·빌드·배포는 앱마다 선택한다. 아직 빈 앱을 만들지 않는다.
- Next.js 앱을 추가하면 소스 패키지인 `@etude/ui`의 `transpilePackages`, client 경계, Tailwind 소스 탐색과 테마 초기화를 별도로 확인한다.
- 공통 테마와 실제로 같은 역할을 하는 부품을 공유한다. 관리자 레이아웃과 메뉴는 관리자 앱 내부에 둔다. 앱 분리가 서버 인증·인가를 대신하지 않는다.

선택 근거와 대안은 [ADR-0002](../docs/decisions/0002-web-foundation.md), 원본 출처와 갱신 기준은 [공통 UI 안내](packages/ui/README.md)에 기록한다.

## UI 추가와 수정

```bash
# web/에서 실행
npm run ui:add -- button
```

공식 CLI가 `components.json`에 따라 공통 패키지에 파일을 설치한다. 기존 파일을 무조건 덮어쓰지 말고, `--dry-run` 또는 `--diff`로 로컬 변경과 비교한다. CLI 버전은 잠금 파일로 고정하지만 원격 레지스트리 내용은 바뀔 수 있으므로 커밋된 소스가 검토 기준이다.

버튼·대화상자·선택 요소를 비슷하게 다시 구현하지 않는다. 설치한 컴포넌트를 import해서 사용하고 앱의 업무 흐름만 조합한다. 가급적 생성된 소스 대신 사용 지점의 props·className과 공통 토큰을 조정한다.

화면 추가 시 `apps/admin/src/pages/`에 화면을 만들고 `app/routes.ts`에 경로·이름·아이콘·지연 로딩할 컴포넌트를 등록한다. 같은 정의가 라우터·메뉴·Breadcrumb·문서 제목·화면 검색에 반영된다. 404는 `App.tsx`의 별도 경로로 유지한다. 제목은 `PageHeader`, 빈 상태는 `EmptyState`를 사용한다.

페이지 안에서만 사용하는 예시는 해당 페이지 폴더에 둔다. 여러 앱이 실제로 공유하는 UI만 `packages/ui`로 옮기고, 관리자 메뉴·라우팅·가이드 예시는 관리자 앱에 유지한다. 아직 사용하지 않는 기능이나 고객·CS 앱의 빈 폴더는 만들지 않는다.

## 테마와 한글 폰트

- 색상·모서리·글꼴은 `packages/ui/src/styles.css`의 토큰으로 관리한다. 라이트·다크 모두 확인한다.
- Pretendard Variable을 npm 패키지에서 포함하고 같은 출처로 제공한다. Unicode 범위별 서브셋을 사용해 필요한 글자 묶음을 불러온다. 외부 폰트 CDN은 사용하지 않는다.
- 테마 설정은 `etude-theme` 키로 저장한다. 저장소 접근이 막혀도 현재 탭의 테마 전환은 동작한다. 다른 탭의 변경과 시스템 테마를 반영한다.
- `public/theme.js`가 React 실행 전 테마를 적용한다. 인라인 스크립트나 `next-themes`의 자동 스크립트 주입은 사용하지 않는다.
- UI 가이드에는 원본 버튼·폼·Select·Checkbox·Switch·탭·메뉴·표·검색·Accordion·대화상자·알림·스켈레톤·진행률 예시가 있다. 실제 업무 데이터가 아니며 입력값을 서버에 저장하지 않는다.

## 검사

```bash
npx playwright install chromium
npm run check
npm run security:check
```

`check`는 타입·ESLint·포맷 검사, 빌드와 브라우저 검사를 실행한다. `npm run format`으로 포맷을 맞춘다.

브라우저 검사는 Chromium의 데스크톱·모바일 화면 크기에서 22개를 실행한다. 경로 직접 접근과 404 복구, 메뉴·키보드 포커스, 테마 저장·시스템 설정, 대화상자·알림, 공식 선택 요소·표 검색, 화면 검색 단축키, 한글 폰트 로딩, 주요 버튼·링크의 명암 대비와 CSP를 확인한다. 모바일 Safari 검증을 의미하지 않는다.

테스트는 `4173` 포트를 직접 사용하며 기존 서버를 재사용하지 않는다. 사용 중이면 먼저 종료한다. 관리자 빌드 결과는 `apps/admin/dist/`에 생성한다. `npm run preview`는 이 빌드와 보안 헤더를 확인하는 로컬 서버이며 운영 서버로 사용하지 않는다.

FE CI 등록과 루트 문서 갱신은 협업 규칙에 따라 별도 `area:shared` 변경으로 관리한다. 현재 브랜치의 FE 검사만으로 원격 CI 통과를 주장하지 않는다.

`security:check`는 개발 도구를 포함해 moderate 이상 알려진 취약점에서 실패한다. 개발 도구의 `minimatch`는 GHSA-g3hv-75wg-m249 대응으로 `11.0.5`를 override한다. 상위 패키지가 수정 버전을 사용하면 override를 제거한다. shadcn CLI의 전이 의존성 `deepmerge` 관련 low 항목은 현재 확인된 개발 도구 제한이며, 강제 하위 버전 설치로 숨기지 않는다. 실제 브라우저에 포함되는 의존성과 전체 개발 도구를 구분해 검사한다.

## 보안과 배포

- 클라이언트 환경변수·정적 파일·문서·검색 인덱스는 공개 정보로 취급한다. 비밀키·비공개 로그를 넣지 않는다.
- CSP는 같은 출처의 외부 스크립트만 허용하고 인라인 스크립트·이벤트 핸들러·`eval`·객체 삽입·프레임 포함을 제한한다.
- Base UI가 팝업 위치·크기를 계산하므로 스타일 속성만 허용한다. 인라인 `<style>`과 스크립트는 허용하지 않는다. 새 컴포넌트는 배포용 빌드에서 위반 여부를 검사한다.
- 빌드 시 `_headers`를 생성한다. 해당 형식을 지원하지 않는 호스팅에서는 CDN·서버에 같은 HTTP 응답 헤더를 설정한다. 배포 후 실제 헤더를 다시 확인한다.
- 정적 자산 외의 앱 경로는 `index.html`로 연결한다. 현재는 도메인 루트 경로 배포를 기준으로 한다.
- HTTPS·HSTS·캐시 정책은 배포 환경이 정해지면 설정한다. 개발 서버는 HMR 때문에 프로덕션 CSP를 적용하지 않으며 외부에 공개하지 않는다.
- 인증·API·실제 모니터링 연결은 아직 없다. 연결 시 서버 인증·인가, 세션·CSRF와 공개 데이터 범위를 별도로 설계한다.

## 라이선스

shadcn/ui와 Base UI는 MIT, Lucide는 ISC, Pretendard는 SIL OFL 1.1이다. 배포물의 `/licenses/`에 관련 고지와 폰트 라이선스를 포함한다. 외부 템플릿·이미지·확장은 별도 확인한다.
