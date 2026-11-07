# Etude 공통 UI

컴포넌트는 `shadcn@4.23.0` 공식 CLI의 `base-nova` 레지스트리에서 2026-11-08에 가져왔다. Base UI를 동작 기반으로 사용하며 앱이 비슷한 디자인을 다시 구현한 것은 아니다.

## 가져온 방법

`web/`에서 다음 명령을 실행했다.

```bash
npx shadcn add button badge card input label textarea select checkbox switch tabs dialog sheet dropdown-menu tooltip separator table skeleton alert progress command scroll-area radio-group accordion --cwd packages/ui --yes
npx shadcn add toast --cwd packages/ui --yes
```

`command`가 사용하는 `input-group`도 CLI가 함께 추가했다. 컴포넌트는 `src/components/`에 있고 런타임 의존성은 이 패키지의 `package.json`에서 관리한다. `src/styles.css`는 공식 `shadcn/tailwind.css`와 Tailwind·애니메이션 스타일을 함께 불러온다.

`src/styles.css`의 소스 탐색 범위는 공통 UI 내부로 제한한다. 소비 앱은 자신의 CSS에서 이 파일을 가져온 뒤 앱 소스의 `@source`를 등록한다. 관리자 예시는 `apps/admin/src/styles.css`에 있다. 공통 CSS에 소비 앱 경로를 넣지 않는다.

## 원본에서 변경한 부분

- 저장소 포맷 규칙 적용
- Dialog·Sheet·Toast의 닫기 접근성 이름을 한글로 변경
- 앱과 다른 컴포넌트에서 참조하지 않는 ScrollArea 소스 제거. 필요해지면 공식 CLI로 다시 추가한다.

색상·한글 폰트와 제품 공통 요소는 원본 컴포넌트 밖에서 관리한다. `EtudeProvider`, `PageHeader`, `EmptyState`, 테마 저장과 앱 레이아웃은 Etude 코드다.

## 갱신

```bash
npm run ui:add -- button --dry-run
npm run ui:add -- button --diff
```

기존 소스와 비교하고 필요한 변경을 적용한 뒤 `npm run check`와 `npm run security:check`를 실행한다. CLI 버전이 같아도 원격 레지스트리가 바뀔 수 있으므로 생성된 소스 diff를 확인한다. 소스를 관리하는 방식이므로 업데이트를 프로젝트에서 검토해야 한다.

## 원본과 라이선스

- [공식 shadcn 문서](https://ui.shadcn.com/docs)
- [공식 모노레포 구성](https://ui.shadcn.com/docs/monorepo)
- [Base UI](https://base-ui.com/)
- [Pretendard](https://github.com/orioncactus/pretendard)
- [배포 고지 파일](../../apps/admin/public/licenses/)
