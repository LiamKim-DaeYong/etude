import { Link } from 'react-router-dom';
import { ArrowRight, Building2, MessagesSquare } from 'lucide-react';
import {
  buttonVariants,
  Card,
  CardContent,
  CardHeader,
  PageHeader,
} from '@etude/ui';
import { previewPaths } from '@/app/routes';
import { usePreviewContent } from './preview-data';
import { PostList } from './PostList';

export default function Home() {
  const { posts, href } = usePreviewContent();
  return (
    <>
      <PageHeader
        title="홈"
        description="회사 소식과 최근 공유한 내용을 확인하세요."
      />
      <div className="mb-8 grid gap-4 lg:grid-cols-[1.4fr_1fr]">
        <Card>
          <CardContent className="flex items-start gap-4 py-2 sm:p-6">
            <span className="grid size-11 shrink-0 place-items-center rounded-xl bg-muted">
              <Building2 className="size-5" aria-hidden="true" />
            </span>
            <div className="min-w-0">
              <p className="mb-2 text-xs text-muted-foreground">샘플 회사</p>
              <h2 className="text-lg font-semibold">김담당님, 안녕하세요</h2>
              <p className="mt-2 text-sm leading-6 text-muted-foreground">
                오늘 함께 확인할 소식이 있는지 살펴보세요.
              </p>
              <p className="mt-4 text-xs text-muted-foreground">
                직원 · 샘플 사용자
              </p>
            </div>
          </CardContent>
        </Card>
        <Link
          to={href(previewPaths.board())}
          className="rounded-xl outline-offset-4"
        >
          <Card className="h-full hover:bg-muted/40">
            <CardContent className="flex h-full items-center gap-4 py-2 sm:p-6">
              <span className="grid size-11 shrink-0 place-items-center rounded-xl bg-primary/10 text-primary">
                <MessagesSquare className="size-5" aria-hidden="true" />
              </span>
              <div className="min-w-0 flex-1">
                <h2 className="text-base font-semibold">게시판 바로가기</h2>
                <p className="mt-2 text-sm leading-6 text-muted-foreground">
                  회사에서 함께 나누는 소식
                </p>
              </div>
              <ArrowRight className="size-4 shrink-0" aria-hidden="true" />
            </CardContent>
          </Card>
        </Link>
      </div>
      <section aria-labelledby="recent-posts-title">
        <Card>
          <CardHeader className="flex flex-row flex-wrap items-center justify-between gap-3 border-b px-4 sm:px-6">
            <div>
              <h2 id="recent-posts-title" className="text-base font-semibold">
                최근 게시글
              </h2>
              <p className="mt-1 text-xs text-muted-foreground">
                게시판에 공유된 글을 모았어요.
              </p>
            </div>
            <Link
              to={href(previewPaths.board())}
              className={buttonVariants({ variant: 'ghost' })}
            >
              게시판 전체 보기
              <ArrowRight className="size-4" aria-hidden="true" />
            </Link>
          </CardHeader>
          <CardContent className="px-2 sm:px-2">
            <PostList posts={posts.slice(0, 3)} />
          </CardContent>
        </Card>
      </section>
    </>
  );
}
