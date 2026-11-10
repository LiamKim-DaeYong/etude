import { Link } from 'react-router-dom';
import { Button, ErrorState, buttonVariants } from '@etude/ui';
import { previewPaths } from './routes';

export function PageErrorFallback() {
  return (
    <ErrorState
      titleAs="h1"
      title="화면을 불러오지 못했습니다"
      description="다른 메뉴로 이동하거나 페이지를 다시 불러와 주세요."
      action={
        <div className="flex flex-wrap justify-center gap-3">
          <Link
            to={previewPaths.home}
            className={buttonVariants({ variant: 'outline' })}
          >
            홈으로 이동
          </Link>
          <Button onClick={() => window.location.reload()}>
            다시 불러오기
          </Button>
        </div>
      }
    />
  );
}
