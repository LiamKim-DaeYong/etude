import { Link } from 'react-router-dom';
import { ChevronRight, MessagesSquare } from 'lucide-react';
import { EmptyState } from '@etude/ui';
import { previewPaths } from '@/app/routes';
import {
  formatPreviewDate,
  previewBoard,
  type PreviewPost,
} from './preview-data';

export function PostList({ posts }: { posts: readonly PreviewPost[] }) {
  if (!posts.length) {
    return (
      <EmptyState
        titleAs="h3"
        icon={<MessagesSquare className="size-5" />}
        title="아직 게시글이 없어요"
        description="함께 나눌 소식이 생기면 이곳에서 확인할 수 있어요."
      />
    );
  }

  return (
    <ul className="divide-y">
      {posts.map((post) => (
        <li key={post.id}>
          <Link
            to={previewPaths.post(previewBoard.id, post.id)}
            className="group flex items-center gap-4 rounded-lg px-4 py-5 outline-offset-2 hover:bg-muted/50 sm:px-6"
          >
            <div className="min-w-0 flex-1">
              <span className="block text-sm leading-6 font-medium wrap-anywhere group-hover:underline group-hover:underline-offset-4">
                {post.title}
              </span>
              <span className="mt-1.5 flex flex-wrap gap-x-3 gap-y-1 text-xs text-muted-foreground">
                <span>{post.author}</span>
                <time dateTime={post.publishedAt}>
                  {formatPreviewDate(post.publishedAt)}
                </time>
              </span>
            </div>
            <ChevronRight
              className="size-4 shrink-0 text-muted-foreground"
              aria-hidden="true"
            />
          </Link>
        </li>
      ))}
    </ul>
  );
}
