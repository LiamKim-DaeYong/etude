# Etude Web

React + TypeScript + Vite 관리자 앱과 여러 앱에서 재사용할 공통 UI·설정을 관리한다. 현재는 BE 없이 실행하는 UI 기반이다. UI는 공식 shadcn CLI로 가져온 `base-nova` 스타일의 Base UI 컴포넌트를 사용한다.

## 실행

Node.js 26.13.0 LTS를 기준으로 한다. `.node-version`과 `package.json`의 실행 환경 범위를 함께 관리하며 CI도 이 버전을 읽는다. PowerShell 실행 정책이 npm 래퍼를 차단하면 `npm.cmd`, `npx.cmd`를 사용한다.

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
│  ├─ lib/http/                # 관리자 요청 보안 모듈 (업무 화면 미연결)
│  ├─ styles.css              # 관리자 CSS 진입점과 관리자 소스 탐색 범위
│  └─ pages/
│     └─ ui-guide/examples/    # UI 가이드 안에서만 사용하는 예시
├─ packages/ui/
│  ├─ components.json          # 공식 레지스트리·스타일·설치 위치
│  └─ src/
│     ├─ components/           # 공식 CLI로 가져온 컴포넌트 원본
│     ├─ styles.css            # 디자인 토큰, Tailwind, 한글 폰트
│     └─ EtudeProvider.tsx     # 테마, 툴팁, 알림
├─ packages/config/            # TypeScript 기준, 배포 보안 헤더
└─ tests/
   ├─ unit/                   # BE 없이 검사하는 요청 보안 경계
   ├─ browser/                # 배포용 빌드 대상 브라우저 검사
   └─ tooling/                # lint 규칙의 정상·실패 사례 검사
```

- 앱이 공통 패키지를 사용한다. 공통 UI는 앱·라우터·업무 규칙에 의존하지 않는다. ESLint가 이 경계를 검사한다.
- 고객 쇼핑몰은 `apps/storefront`, CS는 필요할 때 `apps/cs`로 추가한다. 프레임워크·라우팅·빌드·배포는 앱마다 선택한다. 아직 빈 앱을 만들지 않는다.
- Next.js 앱을 추가하면 소스 패키지인 `@etude/ui`의 `transpilePackages`, client 경계, Tailwind 소스 탐색과 테마 초기화를 별도로 확인한다.
- 공통 테마와 실제로 같은 역할을 하는 부품을 공유한다. 관리자 레이아웃과 메뉴는 관리자 앱 내부에 둔다. 앱 분리가 서버 인증·인가를 대신하지 않는다.

공통 CSS는 Tailwind 자동 탐색을 끄고 공통 UI 소스만 명시적으로 탐색한다. 각 앱의 CSS 진입점이 `@etude/ui/styles.css`를 가져오고 자신의 `@source`를 등록한다. 공통 패키지에 앱 경로를 추가하지 않는다. 관리자와 공통 UI의 `components.json`은 각각 해당 CSS 진입점을 가리킨다.

선택 근거와 대안은 [ADR-0002](../docs/decisions/0002-web-foundation.md), 원본 출처와 갱신 기준은 [공통 UI 안내](packages/ui/README.md)에 기록한다.

## UI 추가와 수정

공식 shadcn CLI의 최신 안정판 `4.23.0`도 수정 버전이 없는 `deepmerge` 취약점([GHSA-gcqx-w48m-w2h4](https://github.com/advisories/GHSA-gcqx-w48m-w2h4))에 의존한다. 2026-11-08에 CLI 개발 의존성과 `ui:add` 명령을 제거했다. 기존 컴포넌트 소스는 저장소에 있으므로 앱 설치·빌드·실행에는 CLI가 필요 없다. 취약한 CLI를 `npx`로 우회 실행하지 않는다.

새 공식 컴포넌트 도입은 취약점이 해결된 CLI 릴리스를 확인한 후 재개한다. 재도입 시 버전을 고정하고 전이 의존성까지 보안 검사를 통과해야 한다. 이후 `components.json`에 따른 설치와 `--dry-run`·`--diff` 결과를 검토한다. 원격 레지스트리 내용은 바뀔 수 있으므로 커밋된 소스가 검토 기준이다.

버튼·대화상자·선택 요소를 비슷하게 다시 구현하지 않는다. 설치한 컴포넌트를 import해서 사용하고 앱의 업무 흐름만 조합한다. 가급적 생성된 소스 대신 사용 지점의 props·className과 공통 토큰을 조정한다.

화면 추가 시 `apps/admin/src/pages/`에 화면을 만들고 `app/routes.ts`에 경로·이름·아이콘·지연 로딩할 컴포넌트를 등록한다. 같은 정의가 라우터·메뉴·Breadcrumb·문서 제목·화면 검색에 반영된다. 404는 `App.tsx`의 별도 경로로 유지한다. 제목은 `PageHeader`, 빈 상태는 `EmptyState`를 사용한다. `EmptyState`의 제목은 기본 `h2`이며 전체 페이지에는 `titleAs="h1"`, 하위 섹션에는 적절한 제목 수준을 지정한다.

페이지 안에서만 사용하는 예시는 해당 페이지 폴더에 둔다. 여러 앱이 실제로 공유하는 UI만 `packages/ui`로 옮기고, 관리자 메뉴·라우팅·가이드 예시는 관리자 앱에 유지한다. 아직 사용하지 않는 기능이나 고객·CS 앱의 빈 폴더는 만들지 않는다.

### 화면 상태와 입력 피드백

`@etude/ui`의 `LoadingState`는 공식 `Skeleton`과 보이는 로딩 안내를 `role="status"`로 묶는다. `App`의 페이지 지연 로딩에 적용했으며 동작 줄이기 설정에서는 스켈레톤 애니메이션을 끈다. `EmptyState`는 빈 결과를, 이를 조합한 `ErrorState`는 `role="alert"`와 복구 행동을 표시한다. 세 컴포넌트의 제목은 기본 `h2`다. 전체 페이지에는 `titleAs="h1"`, UI 가이드 같은 하위 예시에는 `titleAs="h3"`을 지정한다.

`ErrorState`의 `action`에는 호출자가 버튼·링크를 전달한다. 관리자 페이지 오류 화면은 메뉴를 유지하며 워크스페이스 이동과 페이지 다시 불러오기를 제공한다. 공통 UI는 라우터·요청·재시도를 수행하지 않는다. 제목·설명·오류에는 사용자를 위한 한글 안내만 전달하고 서버 `detail`이나 원문 예외를 그대로 넣지 않는다. 최상위 `RootErrorFallback`은 공통 UI·Provider에 의존하지 않는 기본 HTML을 유지한다.

`FormField`는 공식 `Label`과 도움말·오류를 조합한다. render prop이 제공하는 속성을 실제 입력 요소에 모두 전달해야 연결이 유지된다.

```tsx
<FormField label="이름" description="필수 입력입니다." error={nameError}>
  {(controlProps) => <Input {...controlProps} name="name" required />}
</FormField>
```

기본 ID는 React `useId`로 생성하며 필요하면 `id`를 지정한다. `label`의 `htmlFor`, 도움말·오류의 `aria-describedby`, 오류가 있을 때의 `aria-invalid`를 함께 관리하고, 오류를 지우면 연결도 정리한다. 입력값·검증·포커스·서버 오류 매핑은 호출자가 담당한다. 도움말과 오류는 일반 텍스트로 렌더링하며 공통 컴포넌트가 포커스를 강제로 옮기거나 각 오류를 별도로 알리지 않는다.

예시 폼은 제출 시 한글 필드 오류를 표시하고 첫 오류 입력으로 포커스를 옮긴다. 수정해 유효해지면 오류가 사라진다. `noValidate`로 브라우저의 기본 팝업 대신 이 안내를 사용하며 이메일 형식은 브라우저의 입력 유효성으로 확인한다. 서버 저장·실제 업무 검증은 하지 않는다. UI 가이드의 복구 버튼도 예시 표시만 바꾸며 요청을 보내지 않는다.

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

타입 검사·빌드의 `tsc`는 `@typescript/native: npm:typescript@7.0.2` 별칭으로 설치한 TS7 실행 파일이다. ESLint의 `typescript` import는 `npm:@typescript/typescript6@6.0.2` 호환 패키지에 연결한다. 이 패키지는 TS6 API를 다시 내보내고 실행 파일은 `tsc6` 이름으로 제공해 TS7의 `tsc`와 충돌하지 않는다. [Microsoft 공식 병행 구성](https://devblogs.microsoft.com/typescript/announcing-typescript-7-0/)을 사용하며, 타입 정보를 사용하는 lint 규칙을 유지한다.

버전 갱신 시 npm `dist-tags`와 각 도구의 `peerDependencies`를 함께 확인한다. `npm outdated`만으로 최신 메이저의 호환성을 판단하거나, 의존성 충돌을 `--force`·`--legacy-peer-deps`로 우회하지 않는다. ESLint가 TS7 API를 지원하면 호환 패키지 제거를 검토하고 재설치·타입 검사·lint·보안 감사·전체 CI를 확인한다.

ESLint는 브라우저 소스와 Node 설정·테스트의 전역을 구분한다. 브라우저에서 Node 모듈·전역과 디버그 로그를 사용하거나, 공통 UI가 앱·라우터에 의존하면 실패한다. 관리자·공통 UI 소스에는 타입 정보를 사용한 권장 검사를 적용해 처리하지 않은 Promise 등을 탐지한다. React Hooks·Fast Refresh 위반도 오류이며, 공식 컴포넌트의 혼합 export 예외는 해당 파일에만 적용한다. 경고가 남아도 `lint`는 실패한다. 포맷은 Prettier에서 별도로 검사한다.

`npm run test:unit`는 관리자 요청 모듈을 Node 내장 테스트로 검사한다. `npm run test:lint`는 위 규칙의 정상·실패 사례 7개를 검사하고, `npm run test:browser`는 Chromium 검사를 실행한다. `npm test`와 `npm run check`는 세 종류를 모두 포함한다. 새 앱을 추가할 때는 해당 TypeScript 프로젝트를 ESLint의 `typedSources`에도 등록한다. lint는 런타임 보안·서버 권한 검사나 배포 검증을 대신하지 않는다.

브라우저 검사는 Chromium의 데스크톱·모바일 화면 크기에서 54개를 실행한다. 기존 동작·보안 검사 26개는 경로 직접 접근과 404 복구, 메뉴·키보드 포커스, 테마 저장·시스템 설정, 대화상자·알림, 공식 선택 요소·표 검색, 화면 검색 단축키, 한글 폰트 로딩, 주요 버튼·링크의 명암 대비와 CSP를 확인한다. 페이지 청크 요청 실패 시 다른 화면으로 이동하고 다시 불러와 복구하는 흐름과 공통 Provider 렌더링 오류도 재현한다. 프로덕션 CSP 검사에는 폼 오류 표시와 복구 예시 전환도 포함한다. 모바일 Safari 검증을 의미하지 않는다.

접근성 관련 검사 28개는 `@axe-core/playwright`로 라이트·다크의 기본 화면·404·모든 UI 가이드 분류와 검색·대화상자·알림·Select·메뉴·반응형 탐색을 검사한다. 폼 오류·수정 후 상태와 실제 페이지 로딩·오류 상태, 복구 예시의 전환도 포함한다. label 클릭·도움말/오류 연결·첫 오류 포커스·오류 해제·키보드 복구·동작 줄이기와 예시 폼의 변경 요청 0건을 함께 확인한다. WCAG 2.0/2.1 A·AA 및 2.2 AA에 연결된 axe 규칙을 사용한다. 글꼴 로딩과 유한 애니메이션 완료를 기다린 뒤 검사하고, 알림은 Base UI의 `F6` 키로 영역에 진입해 자동 닫힘을 일시 정지한 상태에서 검사한 뒤 키보드로 닫는다.

#72에서 소유자가 승인한 예외는 열린 Select 포털과 연결된 빈 포커스 가드 4개의 `aria-hidden-focus` 결과뿐이다. [Base UI 유지보수자 설명](https://github.com/mui/base-ui/issues/4668)을 참고했으며, 다른 요소·규칙은 계속 검사한다. 예외 결과는 테스트 보고서의 JSON 첨부에 원본과 함께 남긴다. 가드 개수·구조가 달라지면 검사에 실패하며, 별도 검사에서 각 가드가 포커스를 유지하지 않는지와 실제 Tab·Shift+Tab 이동이 다음 체크박스·원래 트리거에 도달하는지 확인한다. 직접 가드를 포커스하면 팝업 닫힘에 따라 `body`로 포커스가 돌아갈 수 있으므로, 가드 진단과 실제 키보드 이동을 구분한다.

자동 접근성 검사는 규칙으로 탐지 가능한 문제를 확인하며 WCAG 전체 준수나 실제 보조기술 사용성 검증을 의미하지 않는다. 기존 키보드 검사를 유지하고 공개 전에는 화면 읽기 도구·실제 모바일·다른 브라우저로 수동 확인한다.

테스트는 `4173` 포트를 직접 사용하며 기존 서버를 재사용하지 않는다. 사용 중이면 먼저 종료한다. 관리자 빌드 결과는 `apps/admin/dist/`에 생성한다. `npm run preview`는 이 빌드와 보안 헤더를 확인하는 로컬 서버이며 운영 서버로 사용하지 않는다.

FE CI 등록과 루트 문서 갱신은 협업 규칙에 따라 별도 `area:shared` 변경으로 관리한다. 현재 브랜치의 FE 검사만으로 원격 CI 통과를 주장하지 않는다.

`security:check`는 개발 도구를 포함해 low 이상 알려진 취약점에서 실패한다. 브라우저에 포함되는 의존성은 `npm audit --omit=dev --audit-level=low`로도 확인한다. 개발 도구의 `minimatch`는 GHSA-g3hv-75wg-m249 대응으로 `11.0.5`를 override한다. 상위 패키지가 수정 버전을 사용하면 override를 제거한다. 최신 버전 여부와 보안 검사는 별개이며, 수정 버전이 없으면 불필요한 도구·의존성을 제거하거나 사용을 중단한다. 보안 검사 통과는 알려지지 않은 취약점의 부재를 보장하지 않는다.

잠금 파일은 `.node-version`의 Node.js 환경에서 갱신하고 `npm ci`로 재설치를 확인한다. 패키지는 최신 안정판과 지원 중인 최신 LTS 실행 환경을 기준으로 검토한다. 버전 변경 시 보안 공지와 호환성을 확인하고 전체 기능·보안 검사를 통과한 뒤 반영한다. CI 도입 시 누락된 Tailwind WASM 번들 의존성은 Node.js 24 / npm 11.19.1에서 재생성해 보완했다.

## 오류 복구

최상위 오류 경계는 공통 Provider까지 감싼다. 이 fallback은 공통 UI나 Provider를 사용하지 않는 기본 HTML로 표시한다. 페이지 오류 경계는 관리자 레이아웃 안에 있어 메뉴·화면 검색을 유지하고, 다른 경로로 이동하면 초기화한다. 실패한 청크를 다시 요청하려면 `다시 불러오기`로 페이지를 새로 연다. 사용자 화면에 내부 예외 메시지는 표시하지 않는다. 이 경계는 React 렌더링 오류를 처리하며 이벤트 핸들러·비동기 요청 오류는 해당 기능에서 별도로 처리한다.

## 보안과 배포

### 관리자 요청 공통 모듈

`apps/admin/src/lib/http/admin-transport.ts`의 `createAdminTransport()`는 BE 없이 준비한 전송 기반이다. 현재 업무 화면에서는 호출하지 않는다. 실제 로그인·세션·CSRF 발급, API 타입 생성, 프록시·업무 화면 연결은 #83·#85의 후속 작업이다. FE 모듈은 서버 인증·인가를 대신하지 않는다.

- `request({ path, parse, ... })`는 `/admin/v1/` 아래의 상대 경로만 허용한다. 외부 출처·고객 API·점 경로·인코딩된 구분자·이중 인코딩·세미콜론 경로를 전송 전에 거부한다. 쿼리 값은 `URLSearchParams`로 인코딩하고 자격 증명을 넣지 않는다.
- 같은 출처 자격 증명·요청 모드, `no-store`, redirect 거부, referrer 미전송을 고정한다. 임의 헤더·fetch 설정을 덮어쓰는 옵션은 제공하지 않는다. 서버에도 응답 `Cache-Control: no-store`와 접근 통제가 필요하다.
- GET 외 요청은 호출자가 받은 `csrf: { headerName, value }`가 필요하다. 현재 표준 헤더 `X-CSRF-TOKEN`·`X-XSRF-TOKEN`만 지원한다. 토큰을 발급·갱신·저장하지 않으며 실제 사용 시 BE 계약에 맞춰 연결해야 한다. 빈 토큰·헤더 삽입·요청 방식 변경 헤더는 거부한다.
- 성공 JSON은 필수 `parse(unknown)` 함수가 런타임 검증한 결과만 돌려준다. 타입 단언만으로 검증을 생략하지 않는다. 204는 파서에 `undefined`를 전달한다. HTML·예상하지 않은 JSON 형식·redirect는 성공 데이터로 취급하지 않는다.
- `AdminRequestError`는 안전한 한글 메시지와 `kind`, HTTP `status`, 형식을 확인한 `Problem.code`만 제공한다. 원문 예외·서버 `detail`·`title`·필드 `message`는 전달하거나 기록하지 않는다. code를 그대로 표시하지 않고 후속 업무 화면에서 알려진 코드만 한글 안내로 매핑한다. 401·403은 HTTP 상태로 구분하며 403의 권한/CSRF 구별은 향후 BE 코드 계약을 따른다.
- 기본 제한 시간은 응답 본문 읽기를 포함해 15초이며 `timeoutMs`로 1~120,000ms 범위에서 지정한다. `signal`로 취소할 수 있고 취소 후 도착한 응답도 반환하지 않는다. 자동 재시도나 인증 이동은 수행하지 않는다.
- 전송한 상태 변경 요청의 연결 단절·취소·시간 초과·응답 검증 실패·서버 5xx는 `outcomeUnknown: true`다. 서버 처리 여부 확인이 필요하며 자동 재전송하지 않는다. `false`도 멱등성이나 재전송 안전성을 보장하지 않는다. 금액 등 JSON 값은 호출자가 전달한 문자열·타입을 유지한다.

`npm run test:unit`는 fetch 대역으로 경로·헤더·상태·JSON 파서·시간 제한·취소·무재시도 경계를 확인한다. 실제 브라우저 통신, BE 인증·인가·CSRF 검증, 운영 쿠키·프록시·캐시 정책 검증을 뜻하지 않는다. 새 의존성은 추가하지 않았다.

### 배포 기준

- 클라이언트 환경변수·정적 파일·문서·검색 인덱스는 공개 정보로 취급한다. 비밀키·비공개 로그를 넣지 않는다.
- CSP는 같은 출처의 외부 스크립트만 허용하고 인라인 스크립트·이벤트 핸들러·`eval`·객체 삽입·프레임 포함을 제한한다.
- Base UI가 팝업 위치·크기를 계산하므로 스타일 속성만 허용한다. 인라인 `<style>`과 스크립트는 허용하지 않는다. `CSPProvider disableStyleElements`로 Select의 인라인 스타일 생성을 끄고 공식 스크롤바 CSS를 공통 외부 스타일에 둔다. 새 컴포넌트는 배포용 빌드에서 위반 여부를 검사한다.
- 빌드 시 `_headers`를 생성한다. 해당 형식을 지원하지 않는 호스팅에서는 CDN·서버에 같은 HTTP 응답 헤더를 설정한다. 배포 후 실제 헤더를 다시 확인한다.
- 정적 자산 외의 앱 경로는 `index.html`로 연결한다. 현재는 도메인 루트 경로 배포를 기준으로 한다.
- HTTPS·HSTS·캐시 정책은 배포 환경이 정해지면 설정한다. 개발 서버는 HMR 때문에 프로덕션 CSP를 적용하지 않으며 외부에 공개하지 않는다.
- 인증·API·실제 모니터링 연결은 아직 없다. 연결 시 서버 인증·인가, 세션·CSRF와 공개 데이터 범위를 별도로 설계한다.

## 라이선스

shadcn/ui와 Base UI는 MIT, Lucide는 ISC, Pretendard는 SIL OFL 1.1이다. 배포물의 `/licenses/`에 관련 고지와 폰트 라이선스를 포함한다. 외부 템플릿·이미지·확장은 별도 확인한다.
