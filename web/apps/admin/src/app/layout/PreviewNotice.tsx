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
      className="border-b bg-muted/40 px-5 py-2 sm:px-8"
    >
      <div className="mx-auto flex max-w-5xl flex-wrap items-center justify-between gap-2">
        <div className="flex min-w-0 flex-wrap items-center gap-x-2 gap-y-1">
          <Badge variant="outline">화면 검토용</Badge>
          <p className="text-xs leading-5 text-muted-foreground">
            회사·사용자·글은 예시이며 로그인·서버 저장은 연결되지 않았습니다.
          </p>
        </div>
        <div
          role="group"
          aria-label="검토할 데이터"
          className="flex shrink-0 items-center gap-1 rounded-lg border bg-background p-0.5"
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
