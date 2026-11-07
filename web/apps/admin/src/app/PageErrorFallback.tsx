import { Link } from 'react-router-dom';
import { Button, PageHeader, buttonVariants } from '@etude/ui';

export function PageErrorFallback() {
  return (
    <section role="alert">
      <PageHeader
        title="화면을 불러오지 못했습니다"
        description="다른 메뉴로 이동하거나 페이지를 다시 불러와 주세요."
      />
      <div className="flex flex-wrap gap-3">
        <Link to="/" className={buttonVariants({ variant: 'outline' })}>
          워크스페이스로 이동
        </Link>
        <Button onClick={() => window.location.reload()}>다시 불러오기</Button>
      </div>
    </section>
  );
}
