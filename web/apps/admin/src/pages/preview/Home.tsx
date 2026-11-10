import { Link } from 'react-router-dom';
import { ArrowRight } from 'lucide-react';
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
        description="안녕하세요. 최근 공유된 소식을 확인하세요."
      />
      <section aria-labelledby="recent-posts-title">
        <Card>
          <CardHeader className="flex flex-row flex-wrap items-center justify-between gap-3 border-b px-5">
            <h2 id="recent-posts-title" className="text-base font-semibold">
              최근 게시글
            </h2>
            <Link
              to={href(previewPaths.board())}
              className={buttonVariants({ variant: 'ghost' })}
            >
              게시판 전체 보기
              <ArrowRight className="size-4" aria-hidden="true" />
            </Link>
          </CardHeader>
          <CardContent className="px-0">
            <PostList posts={posts.slice(0, 3)} />
          </CardContent>
        </Card>
      </section>
    </>
  );
}
