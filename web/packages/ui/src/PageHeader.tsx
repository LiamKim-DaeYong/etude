import type { ReactNode } from 'react';

export function PageHeader({
  title,
  description,
  eyebrow,
  action,
}: {
  title: string;
  description?: string;
  eyebrow?: string;
  action?: ReactNode;
}) {
  return (
    <header className="mb-8 flex flex-wrap items-end justify-between gap-4">
      <div>
        {eyebrow && (
          <p className="mb-3 text-xs font-medium tracking-[0.12em] text-muted-foreground uppercase">
            {eyebrow}
          </p>
        )}
        <h1 className="text-2xl font-semibold tracking-tight sm:text-[1.8rem]">
          {title}
        </h1>
        {description && (
          <p className="mt-2 text-sm leading-7 text-muted-foreground">
            {description}
          </p>
        )}
      </div>
      {action}
    </header>
  );
}
