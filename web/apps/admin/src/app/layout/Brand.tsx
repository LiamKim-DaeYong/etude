import { Link } from 'react-router-dom';
import { Layers2 } from 'lucide-react';

export function Brand({ compact = false }: { compact?: boolean }) {
  return (
    <Link
      to="/"
      className="flex min-h-10 items-center gap-2.5 rounded-lg p-1"
      aria-label={compact ? 'Etude workspace' : undefined}
    >
      <span className="grid size-8 shrink-0 place-items-center rounded-[9px] bg-violet-600 text-white shadow-sm">
        <Layers2 className="size-[18px]" strokeWidth={1.8} />
      </span>
      {!compact && (
        <span className="text-[17px] font-semibold tracking-tight">
          etude{' '}
          <span className="ml-2 rounded border bg-background px-1 py-0.5 text-[9px] font-medium tracking-normal text-muted-foreground">
            workspace
          </span>
        </span>
      )}
    </Link>
  );
}
