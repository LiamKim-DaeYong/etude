import { Link } from 'react-router-dom';
import { Button, ErrorState, buttonVariants } from '@etude/ui';

export function PageErrorFallback({
  publicPage = false,
}: {
  publicPage?: boolean;
}) {
  return (
    <ErrorState
      titleAs="h1"
      title="화면을 불러오지 못했습니다"
      description={
        publicPage
          ? '소개 페이지로 이동하거나 페이지를 다시 불러와 주세요.'
          : '다른 메뉴로 이동하거나 페이지를 다시 불러와 주세요.'
      }
      action={
        <div className="flex flex-wrap justify-center gap-3">
          <Link
            to={publicPage ? '/' : '/workspace'}
            className={buttonVariants({ variant: 'outline' })}
          >
            {publicPage ? '소개 페이지로 이동' : '워크스페이스로 이동'}
          </Link>
          <Button onClick={() => window.location.reload()}>
            다시 불러오기
          </Button>
        </div>
      }
    />
  );
}
