import { useState, type PropsWithChildren } from 'react';
import { Link, matchPath, useLocation } from 'react-router-dom';
import {
  ChevronRight,
  Menu,
  PanelLeftClose,
  PanelLeftOpen,
} from 'lucide-react';
import {
  Button,
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
  SheetTrigger,
  cn,
} from '@etude/ui';
import { previewPaths, routes } from '../routes';
import { usePreviewContent } from '@/pages/preview/preview-data';
import { Brand } from './Brand';
import { AdminNavigation } from './AdminNavigation';
import { PreviewNotice } from './PreviewNotice';
import { ThemeMenu } from './ThemeMenu';

export function AdminLayout({ children }: PropsWithChildren) {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [collapsed, setCollapsed] = useState(false);
  const { pathname } = useLocation();
  const { href } = usePreviewContent();
  const current = routes.find((route) => matchPath(route.path, pathname));
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
            <div className="mx-1 mt-7 mb-6 rounded-lg border bg-background px-3 py-3">
              <p className="text-sm font-semibold">샘플 회사</p>
              <p className="mt-1 text-xs text-muted-foreground">
                회사 정보 예시
              </p>
            </div>
          )}
          <div className={cn(collapsed && 'mt-6')}>
            <AdminNavigation compact={collapsed} />
          </div>
          <div className="mt-auto space-y-3 border-t pt-4">
            {!collapsed && (
              <div className="flex items-center gap-3 px-2 pb-2">
                <span
                  className="grid size-9 shrink-0 place-items-center rounded-full bg-muted text-xs font-semibold"
                  aria-hidden="true"
                >
                  김
                </span>
                <div>
                  <p className="text-sm font-medium">김담당</p>
                  <p className="mt-1 text-xs text-muted-foreground">
                    직원 · 샘플 사용자
                  </p>
                </div>
              </div>
            )}
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
              {collapsed ? (
                <PanelLeftOpen aria-hidden="true" />
              ) : (
                <PanelLeftClose aria-hidden="true" />
              )}
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
          <header className="flex h-16 shrink-0 items-center justify-between gap-3 border-b bg-background px-4 sm:px-8">
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
                <Menu aria-hidden="true" />
              </SheetTrigger>
              <nav
                aria-label="현재 위치"
                className="flex min-w-0 items-center gap-2 text-xs"
              >
                <Link
                  to={href(previewPaths.home)}
                  className="text-muted-foreground"
                >
                  샘플 회사
                </Link>
                <ChevronRight
                  className="size-3 shrink-0 text-muted-foreground"
                  aria-hidden="true"
                />
                <span aria-current="page" className="truncate">
                  {current?.label ?? '페이지 없음'}
                </span>
              </nav>
            </div>
            <div className="flex shrink-0 items-center gap-3">
              <span className="hidden text-xs text-muted-foreground sm:inline">
                김담당 · 직원 예시
              </span>
              <ThemeMenu />
            </div>
          </header>
          <PreviewNotice />
          <main
            id="main-content"
            tabIndex={-1}
            className="mx-auto w-full max-w-5xl flex-1 px-5 py-7 outline-none sm:px-8 sm:py-9"
          >
            {children}
          </main>
          <footer className="px-5 py-5 text-center text-xs text-muted-foreground">
            Etude · 화면 검토용
          </footer>
        </div>
        <SheetContent side="left" className="w-72! gap-0 p-3">
          <SheetHeader className="mb-6 px-2">
            <SheetTitle>샘플 회사</SheetTitle>
            <SheetDescription>김담당 · 직원 · 샘플 사용자</SheetDescription>
          </SheetHeader>
          <AdminNavigation close={() => setMobileOpen(false)} />
        </SheetContent>
      </div>
    </Sheet>
  );
}
