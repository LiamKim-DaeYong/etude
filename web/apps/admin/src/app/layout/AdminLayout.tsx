import { useState, type PropsWithChildren } from 'react';
import { Link, matchPath, useLocation } from 'react-router-dom';
import {
  ChevronRight,
  Menu,
  Moon,
  PanelLeftClose,
  PanelLeftOpen,
  Sun,
} from 'lucide-react';
import {
  Badge,
  Button,
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
  Tooltip,
  TooltipContent,
  TooltipTrigger,
  cn,
  useTheme,
} from '@etude/ui';
import { routes } from '../routes';
import { Brand } from './Brand';
import { AdminNavigation } from './AdminNavigation';
import { ScreenSearch } from './ScreenSearch';

export function AdminLayout({ children }: PropsWithChildren) {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [collapsed, setCollapsed] = useState(false);
  const { resolvedTheme, setTheme } = useTheme();
  const { pathname } = useLocation();
  const current = routes.find((item) => matchPath(item.path, pathname));
  return (
    <Sheet open={mobileOpen} onOpenChange={setMobileOpen}>
      <div className="min-h-dvh">
        <a
          href="#main-content"
          className="pointer-events-none fixed top-3 left-4 z-[100] rounded-lg border bg-popover px-4 py-3 text-sm opacity-0 shadow-md focus:pointer-events-auto focus:opacity-100"
        >
          본문으로 건너뛰기
        </a>
        <aside
          aria-label="사이드바"
          className={cn(
            'fixed inset-y-0 left-0 z-30 hidden flex-col border-r bg-sidebar p-3 md:flex',
            collapsed ? 'w-[68px]' : 'w-60',
          )}
        >
          <Brand compact={collapsed} />
          {!collapsed && (
            <div className="mx-1 mt-4 mb-6 flex items-center gap-2 rounded-lg border bg-background px-3 py-2.5">
              <span className="size-1.5 rounded-full bg-violet-500" />
              <span className="text-xs font-medium">Etude 프로젝트</span>
              <Badge variant="secondary" className="ml-auto text-[9px]">
                Admin
              </Badge>
            </div>
          )}
          <div className={cn('mt-4', !collapsed && 'mt-0')}>
            <AdminNavigation compact={collapsed} />
          </div>
          <div className="mt-auto border-t pt-3">
            <Button
              variant="ghost"
              size={collapsed ? 'icon' : 'default'}
              className={cn(
                'text-muted-foreground',
                !collapsed && 'w-full justify-start',
              )}
              onClick={() => setCollapsed(!collapsed)}
              aria-label={collapsed ? '사이드바 펼치기' : '사이드바 접기'}
              aria-expanded={!collapsed}
            >
              {collapsed ? <PanelLeftOpen /> : <PanelLeftClose />}
              {!collapsed && <span className="text-xs">사이드바 접기</span>}
            </Button>
          </div>
        </aside>
        <div
          className={cn(
            'flex min-h-dvh flex-col',
            collapsed ? 'md:ml-[68px]' : 'md:ml-60',
          )}
        >
          <header className="flex h-16 shrink-0 items-center justify-between gap-3 border-b bg-background px-4 sm:px-7">
            <div className="flex min-w-0 items-center gap-3">
              <SheetTrigger
                render={
                  <Button
                    variant="ghost"
                    size="icon"
                    className="md:hidden"
                    aria-label="메뉴 열기"
                  />
                }
              >
                <Menu />
              </SheetTrigger>
              <div className="hidden h-4 w-px bg-border md:block" />
              <nav
                aria-label="현재 위치"
                className="flex items-center gap-2 text-xs"
              >
                <Link
                  to="/workspace"
                  className="hidden text-muted-foreground sm:inline"
                >
                  Etude
                </Link>
                <ChevronRight className="hidden size-3 text-muted-foreground sm:inline" />
                <span aria-current="page" className="truncate">
                  {current?.label ?? '페이지 없음'}
                </span>
              </nav>
            </div>
            <div className="flex items-center gap-2">
              <ScreenSearch />
              <Tooltip>
                <TooltipTrigger
                  render={
                    <Button
                      variant="ghost"
                      size="icon"
                      aria-label={
                        resolvedTheme === 'dark'
                          ? '밝은 테마로 전환'
                          : '어두운 테마로 전환'
                      }
                      onClick={() =>
                        setTheme(resolvedTheme === 'dark' ? 'light' : 'dark')
                      }
                    />
                  }
                >
                  {resolvedTheme === 'dark' ? <Sun /> : <Moon />}
                </TooltipTrigger>
                <TooltipContent>
                  {resolvedTheme === 'dark' ? '라이트 모드' : '다크 모드'}
                </TooltipContent>
              </Tooltip>
            </div>
          </header>
          <main
            id="main-content"
            tabIndex={-1}
            className="mx-auto w-full max-w-[1440px] flex-1 p-5 outline-none sm:px-8 sm:py-9 lg:px-10"
          >
            {children}
          </main>
        </div>
        <SheetContent side="left" className="w-72! gap-0 p-3">
          <SheetHeader className="mb-6 px-1">
            <SheetTitle>Etude</SheetTitle>
            <SheetDescription>프로젝트 작업 공간</SheetDescription>
          </SheetHeader>
          <AdminNavigation close={() => setMobileOpen(false)} />
        </SheetContent>
      </div>
    </Sheet>
  );
}
