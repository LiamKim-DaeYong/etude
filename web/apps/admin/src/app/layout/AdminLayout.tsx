import { useState, type PropsWithChildren } from 'react';
import { matchPath, useLocation } from 'react-router-dom';
import { Menu, PanelLeftClose, PanelLeftOpen } from 'lucide-react';
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
import { routes } from '../routes';
import { Brand } from './Brand';
import { AdminNavigation } from './AdminNavigation';
import { PreviewNotice } from './PreviewNotice';
import { ThemeMenu } from './ThemeMenu';

export function AdminLayout({ children }: PropsWithChildren) {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [collapsed, setCollapsed] = useState(false);
  const { pathname } = useLocation();
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
          <div className="mt-6">
            <AdminNavigation compact={collapsed} />
          </div>
          <div className="mt-auto space-y-3 border-t pt-4">
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
                className="min-w-0 text-sm font-medium"
              >
                <span aria-current="page" className="truncate">
                  {current?.label ?? '페이지 없음'}
                </span>
              </nav>
            </div>
            <div className="flex shrink-0 items-center gap-3">
              <div
                role="group"
                aria-label="회사와 사용자"
                className="text-right"
              >
                <p className="text-sm font-medium">샘플 회사</p>
                <p className="mt-0.5 text-xs text-muted-foreground">
                  김담당 · 직원
                </p>
              </div>
              <ThemeMenu />
            </div>
          </header>
          <PreviewNotice />
          <main
            id="main-content"
            tabIndex={-1}
            className="mx-auto w-full max-w-5xl flex-1 px-5 py-6 outline-none sm:px-8 sm:py-8"
          >
            {children}
          </main>
        </div>
        <SheetContent side="left" className="w-72! gap-0 p-3">
          <SheetHeader className="mb-6 px-2">
            <SheetTitle>메뉴</SheetTitle>
            <SheetDescription>홈과 게시판으로 이동합니다.</SheetDescription>
          </SheetHeader>
          <AdminNavigation close={() => setMobileOpen(false)} />
        </SheetContent>
      </div>
    </Sheet>
  );
}
