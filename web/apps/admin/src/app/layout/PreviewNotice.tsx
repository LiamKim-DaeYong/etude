import { useSearchParams } from 'react-router-dom';
import { Badge, Button } from '@etude/ui';

export function PreviewNotice() {
  const [params, setParams] = useSearchParams();
  const empty = params.get('data') === 'empty';
  function setEmpty(value: boolean) {
    const next = new URLSearchParams(params);
    if (value) next.set('data', 'empty');
    else next.delete('data');
    void setParams(next);
  }
  return (
    <section
      aria-label="화면 검토 안내"
      className="border-b bg-muted/40 px-5 py-3 sm:px-8"
    >
      <div className="mx-auto flex max-w-5xl flex-wrap items-center justify-between gap-3">
        <div className="min-w-0">
          <div className="flex items-center gap-2 text-xs font-medium">
            <Badge variant="outline">화면 검토용</Badge>
            로그인 이후 화면 초안
          </div>
          <p className="mt-1.5 text-xs leading-5 text-muted-foreground">
            회사·사용자·게시글은 샘플입니다. 실제 로그인이나 서버 저장은
            연결되지 않았습니다.
          </p>
        </div>
        <div
          role="group"
          aria-label="검토할 데이터"
          className="flex shrink-0 items-center gap-1 rounded-lg border bg-background p-1"
        >
          <Button
            size="sm"
            variant={empty ? 'ghost' : 'secondary'}
            aria-pressed={!empty}
            onClick={() => setEmpty(false)}
          >
            샘플 글
          </Button>
          <Button
            size="sm"
            variant={empty ? 'secondary' : 'ghost'}
            aria-pressed={empty}
            onClick={() => setEmpty(true)}
          >
            글 없음
          </Button>
        </div>
      </div>
    </section>
  );
}
