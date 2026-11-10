import { lazy } from 'react';
import { FileText, House, MessagesSquare } from 'lucide-react';

export const previewBoardId = 'general';
export const previewPaths = {
  home: '/preview',
  board: (boardId = previewBoardId) => `/preview/boards/${boardId}`,
  post: (boardId: string, postId: string) =>
    `/preview/boards/${boardId}/posts/${postId}`,
};

export const routes = [
  {
    path: previewPaths.home,
    to: previewPaths.home,
    label: '홈',
    icon: House,
    menu: true,
    end: true,
    Component: lazy(() => import('@/pages/preview/Home')),
  },
  {
    path: '/preview/boards/:boardId',
    to: previewPaths.board(),
    label: '게시판',
    icon: MessagesSquare,
    menu: true,
    end: false,
    Component: lazy(() => import('@/pages/preview/Board')),
  },
  {
    path: '/preview/boards/:boardId/posts/:postId',
    to: previewPaths.board(),
    label: '글 상세',
    icon: FileText,
    menu: false,
    end: false,
    Component: lazy(() => import('@/pages/preview/Post')),
  },
] as const;

export const navigationRoutes = routes.filter((route) => route.menu);
