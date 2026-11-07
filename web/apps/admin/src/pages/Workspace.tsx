import { Link } from 'react-router-dom';
import { ArrowUpRight, Blocks, Palette, Route } from 'lucide-react';
import { Card, CardContent, PageHeader } from '@etude/ui';

export default function Workspace() {
  return (
    <>
      <PageHeader
        title="Etude 작업 공간"
        description="관리 화면과 표시 설정에 접근할 수 있습니다."
      />
      <h2 className="mb-4 text-sm font-semibold">바로가기</h2>
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
          준비 중인 기능
        </div>
        <p className="mt-2 text-xs leading-6 text-muted-foreground">
          문서 조회, 프로세스, 모니터링 기능은 아직 제공하지 않습니다.
        </p>
      </div>
    </>
  );
}
