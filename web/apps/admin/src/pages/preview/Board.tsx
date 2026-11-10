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
        <CardHeader className="flex flex-row items-center justify-between gap-3 border-b px-5">
          <h2 className="text-sm font-semibold">전체 게시글</h2>
          <p className="text-xs text-muted-foreground">{posts.length}개</p>
        </CardHeader>
        <CardContent className="px-0">
          <PostList posts={posts} />
        </CardContent>
      </Card>
    </>
  );
}
