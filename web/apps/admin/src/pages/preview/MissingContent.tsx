import { Link } from 'react-router-dom';
import { EmptyState, buttonVariants } from '@etude/ui';
import { previewPaths } from '@/app/routes';
import { usePreviewContent } from './preview-data';

export function MissingContent({ title }: { title: string }) {
  const { href } = usePreviewContent();
  return (
    <EmptyState
      titleAs="h1"
      title={title}
      description="표시할 내용이 없습니다. 게시판 목록에서 다시 확인해 주세요."
      action={
        <Link to={href(previewPaths.board())} className={buttonVariants()}>
          게시판 목록으로
        </Link>
      }
    />
  );
}
