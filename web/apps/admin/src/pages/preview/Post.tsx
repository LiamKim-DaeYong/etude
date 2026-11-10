import { Link, useParams } from 'react-router-dom';
import { ArrowLeft } from 'lucide-react';
import { buttonVariants, Card, CardContent } from '@etude/ui';
import { previewPaths } from '@/app/routes';
import {
  formatPreviewDate,
  previewBoard,
  usePreviewContent,
} from './preview-data';
import { MissingContent } from './MissingContent';

export default function Post() {
  const { boardId, postId } = useParams();
  const { posts, href } = usePreviewContent();
  const post = posts.find((item) => item.id === postId);
  if (boardId !== previewBoard.id || !post) {
    return <MissingContent title="게시글을 찾을 수 없습니다" />;
  }
  return (
    <>
      <Link
        to={href(previewPaths.board())}
        className={buttonVariants({ variant: 'ghost', className: 'mb-5' })}
      >
        <ArrowLeft className="size-4" aria-hidden="true" />
        게시판으로
      </Link>
      <article>
        <Card>
          <CardContent className="px-5 py-3 sm:px-8 sm:py-6">
            <header className="border-b pb-6">
              <p className="mb-3 text-xs font-medium text-muted-foreground">
                게시판
              </p>
              <h1 className="text-xl leading-relaxed font-semibold tracking-tight wrap-anywhere sm:text-2xl">
                {post.title}
              </h1>
              <dl className="mt-4 flex flex-wrap gap-x-5 gap-y-2 text-xs text-muted-foreground">
                <div className="flex gap-2">
                  <dt>작성자</dt>
                  <dd>{post.author}</dd>
                </div>
                <div className="flex gap-2">
                  <dt>작성일</dt>
                  <dd>
                    <time dateTime={post.publishedAt}>
                      {formatPreviewDate(post.publishedAt)}
                    </time>
                  </dd>
                </div>
              </dl>
            </header>
            <div className="space-y-5 py-8 text-sm leading-8 wrap-anywhere">
              {post.paragraphs.map((paragraph) => (
                <p key={paragraph}>{paragraph}</p>
              ))}
            </div>
            <footer className="border-t pt-5">
              <Link
                to={href(previewPaths.board())}
                className={buttonVariants({ variant: 'outline' })}
              >
                목록으로 돌아가기
              </Link>
            </footer>
          </CardContent>
        </Card>
      </article>
    </>
  );
}
