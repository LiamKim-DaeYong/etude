import { NavLink } from 'react-router-dom';
import { cn } from '@etude/ui';
import { navigationRoutes } from '../routes';
import { usePreviewContent } from '@/pages/preview/preview-data';

export function AdminNavigation({
  compact = false,
  close,
}: {
  compact?: boolean;
  close?: () => void;
}) {
  const { href } = usePreviewContent();
  return (
    <nav aria-label="주 메뉴" className="space-y-1">
      {navigationRoutes.map(({ to, label, icon: Icon, end }) => (
        <NavLink
          key={to}
          to={href(to)}
          end={end}
          onClick={close}
          aria-label={label}
          title={compact ? label : undefined}
          className={({ isActive }) =>
            cn(
              'flex min-h-11 items-center gap-3 rounded-lg px-3 text-sm hover:bg-accent',
              isActive
                ? 'bg-accent font-semibold text-accent-foreground'
                : 'text-muted-foreground',
              compact && 'justify-center px-0',
            )
          }
        >
          <Icon
            className="size-4 shrink-0"
            strokeWidth={1.7}
            aria-hidden="true"
          />
          {!compact && label}
        </NavLink>
      ))}
    </nav>
  );
}
