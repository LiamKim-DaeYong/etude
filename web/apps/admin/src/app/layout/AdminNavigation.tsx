import { NavLink } from 'react-router-dom';
import { Activity, BookOpen, Route } from 'lucide-react';
import { cn } from '@etude/ui';
import { routes } from '../routes';

export function AdminNavigation({
  compact = false,
  close,
}: {
  compact?: boolean;
  close?: () => void;
}) {
  return (
    <nav aria-label="주 메뉴" className="space-y-1">
      <p
        className={cn(
          'px-2 pb-2 text-[11px] font-medium text-muted-foreground',
          compact && 'sr-only',
        )}
      >
        작업 공간
      </p>
      {routes.map(({ path, label, icon: Icon }) => (
        <NavLink
          key={path}
          to={path}
          end
          onClick={close}
          aria-label={label}
          title={compact ? label : undefined}
          className={({ isActive }) =>
            cn(
              'flex h-9 items-center gap-2.5 rounded-md px-2.5 text-[13px] transition-colors hover:bg-accent',
              isActive
                ? 'bg-accent font-medium text-accent-foreground'
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
      <div className="my-5 border-t" />
      <p
        className={cn(
          'px-2 pb-2 text-[11px] font-medium text-muted-foreground',
          compact && 'sr-only',
        )}
      >
        프로젝트 탐색
      </p>
      {[
        { label: '문서와 기록', icon: BookOpen },
        { label: '프로세스', icon: Route },
        { label: '모니터링', icon: Activity },
      ].map(({ label, icon: Icon }) => (
        <div
          key={label}
          className={cn(
            'flex h-9 items-center gap-2.5 px-2.5 text-[13px] text-muted-foreground',
            compact && 'justify-center px-0',
          )}
          title={`${label} · 준비 중`}
        >
          <Icon
            className="size-4 shrink-0"
            strokeWidth={1.6}
            aria-hidden="true"
          />
          {!compact && (
            <>
              <span>{label}</span>
              <span className="ml-auto text-[10px] text-muted-foreground">
                준비 중
              </span>
            </>
          )}
        </div>
      ))}
    </nav>
  );
}
