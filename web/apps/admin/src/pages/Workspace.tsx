import { Link } from 'react-router-dom';
import {
  ArrowRight,
  ArrowUpRight,
  Blocks,
  BookOpen,
  Check,
  Code2,
  Layers2,
  Palette,
  Route,
} from 'lucide-react';
import {
  Badge,
  Card,
  CardContent,
  PageHeader,
  buttonVariants,
  cn,
} from '@etude/ui';

export default function Workspace() {
  return (
    <>
      <PageHeader
        eyebrow="Overview"
        title="Etude 작업 공간"
        description="프로젝트의 설계와 구현을 차근차근 탐색하세요."
        action={
          <Badge variant="outline" className="gap-1.5 py-1">
            <span className="size-1.5 rounded-full bg-violet-500" />
            Engineering workspace
          </Badge>
        }
      />
      <section
        aria-labelledby="workspace-heading"
        className="relative mb-8 overflow-hidden rounded-2xl border bg-gradient-to-br from-muted/70 via-background to-violet-50/50 p-6 sm:p-8 dark:to-violet-950/15"
      >
        <div className="relative z-10 grid items-center gap-8 lg:grid-cols-[1.1fr_1fr]">
          <div>
            <span className="mb-5 inline-flex items-center gap-2 text-xs font-medium text-muted-foreground">
              <Layers2 className="size-3.5" />
              한곳에서 이어지는 프로젝트
            </span>
            <h2
              id="workspace-heading"
              className="text-3xl leading-[1.4] font-semibold tracking-tight sm:text-4xl"
            >
              작은 질문에서,
              <br />
              명확한 이해까지.
            </h2>
            <p className="mt-4 max-w-md text-sm leading-7 text-muted-foreground">
              고민한 이유와 선택한 방법, 직접 확인한 결과를
              <br className="hidden sm:block" /> 하나의 흐름으로 연결하는
              공간입니다.
            </p>
            <Link
              to="/ui"
              className={cn(buttonVariants({ size: 'lg' }), 'mt-6 px-4')}
            >
              UI 가이드 둘러보기
              <ArrowRight />
            </Link>
          </div>
          <div className="hidden select-none sm:block" aria-hidden="true">
            <div className="mx-auto max-w-sm rounded-xl border bg-card shadow-lg shadow-black/[0.025]">
              <div className="flex items-center gap-1.5 border-b px-4 py-3">
                <i className="size-2 rounded-full bg-border" />
                <i className="size-2 rounded-full bg-border" />
                <i className="size-2 rounded-full bg-border" />
                <span className="ml-auto font-mono text-[10px] text-muted-foreground">
                  etude / workspace
                </span>
              </div>
              <div className="space-y-3 p-5">
                {[
                  { label: '문제를 이해하고', icon: BookOpen },
                  { label: '직접 구현하고', icon: Code2 },
                  { label: '결과를 검증합니다', icon: Check },
                ].map(({ label, icon: Icon }, index) => (
                  <div
                    key={label}
                    className="flex items-center gap-3 rounded-lg border bg-background px-3 py-3"
                  >
                    <span className="grid size-7 place-items-center rounded-md bg-muted text-muted-foreground">
                      <Icon className="size-3.5" />
                    </span>
                    <span className="text-xs font-medium">{label}</span>
                    <span className="ml-auto font-mono text-[10px] text-muted-foreground">
                      0{index + 1}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-sm font-semibold">빠르게 시작하기</h2>
        <span className="text-xs text-muted-foreground">
          필요한 공간으로 이동하세요
        </span>
      </div>
      <div className="grid gap-4 lg:grid-cols-2">
        {[
          {
            title: 'UI 가이드',
            description: '버튼부터 표와 검색까지, 공통 요소를 직접 살펴보세요.',
            path: '/ui',
            icon: Blocks,
          },
          {
            title: '화면 설정',
            description: '편안한 화면을 위한 테마를 선택하세요.',
            path: '/settings',
            icon: Palette,
          },
        ].map(({ title, description, path, icon: Icon }) => (
          <Link
            key={path}
            to={path}
            className="group rounded-xl outline-offset-4"
          >
            <Card className="h-full transition-colors hover:bg-muted/30">
              <CardContent className="flex items-center gap-4">
                <span className="grid size-11 shrink-0 place-items-center rounded-lg border bg-muted/40">
                  <Icon
                    className="size-5 text-muted-foreground"
                    strokeWidth={1.6}
                  />
                </span>
                <div>
                  <h3 className="text-sm font-semibold">{title}</h3>
                  <p className="mt-1 text-xs leading-6 text-muted-foreground">
                    {description}
                  </p>
                </div>
                <ArrowUpRight className="ml-auto size-4 shrink-0 text-muted-foreground transition-transform group-hover:translate-x-0.5 group-hover:-translate-y-0.5" />
              </CardContent>
            </Card>
          </Link>
        ))}
      </div>
      <div className="mt-8 rounded-xl border border-dashed p-5">
        <div className="flex items-center gap-2 text-sm font-medium">
          <Route className="size-4 text-muted-foreground" />
          다음으로 이어질 공간
        </div>
        <p className="mt-2 text-xs leading-6 text-muted-foreground">
          문서와 기록, 프로세스, 모니터링을 차례로 연결할 예정입니다.
        </p>
      </div>
    </>
  );
}
