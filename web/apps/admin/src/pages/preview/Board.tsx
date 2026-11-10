import { useParams } from 'react-router-dom';
import { Card, CardContent, CardHeader, PageHeader } from '@etude/ui';
import { previewBoard, usePreviewContent } from './preview-data';
import { PostList } from './PostList';
import { MissingContent } from './MissingContent';

export default function Board() {
  const { boardId } = useParams();
  const { posts } = usePreviewContent();
  if (boardId !== previewBoard.id) {
    return <MissingContent title="게시판을 찾을 수 없습니다" />;
  }
  return (
    <>
      <PageHeader
        title={previewBoard.name}
        description={previewBoard.description}
      />
      <Card>
        <CardHeader className="border-b px-6">
          <h2 className="text-sm font-semibold">전체 게시글</h2>
          <p className="mt-1 text-xs text-muted-foreground">
            {posts.length
              ? `샘플 게시글 ${posts.length}개`
              : '등록된 게시글이 없습니다.'}
          </p>
        </CardHeader>
        <CardContent className="px-2 sm:px-2">
          <PostList posts={posts} />
        </CardContent>
      </Card>
    </>
  );
}
