import type { ReactNode } from 'react';

export function EmptyState({
  icon,
  title,
  description,
  action,
}: {
  icon?: ReactNode;
  title: string;
  description: string;
  action?: ReactNode;
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
      <h2 className="text-base font-semibold">{title}</h2>
      <p className="max-w-sm text-sm leading-6 text-muted-foreground">
        {description}
      </p>
      {action && <div className="mt-2">{action}</div>}
    </div>
  );
}
