import type { ReactNode } from 'react';
import { TriangleAlert } from 'lucide-react';
import { EmptyState } from './EmptyState';

export function ErrorState({
  title,
  description,
  action,
  titleAs = 'h2',
}: {
  title: string;
  description: string;
  action?: ReactNode;
  titleAs?: 'h1' | 'h2' | 'h3';
}) {
  return (
    <div role="alert">
      <EmptyState
        titleAs={titleAs}
        icon={<TriangleAlert className="size-5" />}
        title={title}
        description={description}
        action={action}
      />
    </div>
  );
}
