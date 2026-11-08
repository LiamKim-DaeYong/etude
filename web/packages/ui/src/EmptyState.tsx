import type { ReactNode } from 'react';

export function EmptyState({
  icon,
  title,
  description,
  action,
  titleAs: Title = 'h2',
}: {
  icon?: ReactNode;
  title: string;
  description: string;
  action?: ReactNode;
  titleAs?: 'h1' | 'h2' | 'h3';
}) {
  return (
    <div className="flex flex-col items-center gap-3 px-5 py-12 text-center">
      {icon && (
        <span
          className="mb-2 rounded-xl border bg-muted/50 p-3 text-muted-foreground"
          aria-hidden="true"
        >
          {icon}
        </span>
      )}
      <Title className="text-base font-semibold">{title}</Title>
      <p className="max-w-sm text-sm leading-6 text-muted-foreground">
        {description}
      </p>
      {action && <div className="mt-2">{action}</div>}
    </div>
  );
}
