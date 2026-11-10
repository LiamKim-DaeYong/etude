import { useSearchParams } from 'react-router-dom';
import { previewBoardId } from '@/app/routes';

// UI review fixtures only. These types do not define a backend contract.
export interface PreviewPost {
  id: string;
  title: string;
  author: string;
  publishedAt: string;
  paragraphs: readonly string[];
}

export const previewBoard = {
  id: previewBoardId,
  name: '게시판',
  description: '회사에서 함께 알아야 할 소식과 업무 내용을 나눠요.',
};

export const previewPosts: readonly PreviewPost[] = [
  {
    id: 'welcome',
    title: '함께 쓰는 게시판을 시작합니다',
    author: '김담당',
    publishedAt: '2026-11-10T00:30:00Z',
    paragraphs: [
      '팀에서 함께 알아야 할 소식과 업무 내용을 이곳에서 확인해 보세요.',
      '일정이 달라지거나 공유할 내용이 생기면 게시판에 모아 두고 함께 확인하는 모습을 생각하고 있어요.',
      '이 글은 화면을 살펴보기 위한 샘플입니다. 실제 회사의 공지나 저장된 업무 기록이 아닙니다.',
    ],
  },
  {
    id: 'weekly-notes',
    title: '이번 주 공유할 내용을 모아 주세요',
    author: '이담당',
    publishedAt: '2026-11-09T02:00:00Z',
    paragraphs: [
      '이번 주 함께 확인할 일정이나 업무 변경 사항을 정리하는 예시입니다.',
      '누가 확인해야 하는지, 언제까지 필요한 일인지 함께 적으면 내용을 이해하기 쉬워요.',
    ],
  },
  {
    id: 'handover-notes',
    title: '업무 인수인계는 이렇게 남겨 보세요',
    author: '김담당',
    publishedAt: '2026-11-06T06:00:00Z',
    paragraphs: [
      '현재 진행 상황과 다음에 할 일을 짧게 정리한 인수인계 글의 예시입니다.',
      '담당자가 자리를 비워도 다른 사람이 이어서 확인할 수 있도록 필요한 내용을 모아 봅니다.',
    ],
  },
];

export function usePreviewContent() {
  const [params] = useSearchParams();
  const empty = params.get('data') === 'empty';
  return {
    empty,
    posts: empty ? [] : previewPosts,
    href: (path: string) => (empty ? `${path}?data=empty` : path),
  };
}

export function formatPreviewDate(instant: string) {
  return new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' }).format(
    new Date(instant),
  );
}
