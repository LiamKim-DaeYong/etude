import { useEffect, useState, type PropsWithChildren } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom';
import {
  Activity,
  ArrowUpRight,
  BookOpen,
  ChevronRight,
  Command as CommandIcon,
  Layers2,
  Menu,
  Moon,
  PanelLeftClose,
  PanelLeftOpen,
  Route,
  Search,
  Sun,
} from 'lucide-react';
import {
  Badge,
  Button,
  Command,
  CommandEmpty,
  CommandGroup,
  CommandInput,
  CommandItem,
  CommandList,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
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
import { navigation } from '../navigation';

function Brand({ compact = false }: { compact?: boolean }) {
  return (
    <Link
      to="/"
      className="flex min-h-10 items-center gap-2.5 rounded-lg p-1"
      aria-label="Etude 워크스페이스"
    >
      <span className="grid size-8 shrink-0 place-items-center rounded-[9px] bg-violet-600 text-white shadow-sm">
        <Layers2 className="size-[18px]" strokeWidth={1.8} />
      </span>
      {!compact && (
        <span className="text-[17px] font-semibold tracking-tight">
          etude
          <span className="ml-2 rounded border bg-background px-1 py-0.5 text-[9px] font-medium tracking-normal text-muted-foreground">
            workspace
          </span>
        </span>
      )}
    </Link>
  );
}

function Navigation({
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
      {navigation.map(({ path, label, icon: Icon }) => (
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

function ScreenSearch() {
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  useEffect(() => {
    const listener = (event: KeyboardEvent) => {
      if (event.key.toLowerCase() === 'k' && (event.ctrlKey || event.metaKey)) {
        event.preventDefault();
        setOpen((value) => !value);
      }
    };
    window.addEventListener('keydown', listener);
    return () => window.removeEventListener('keydown', listener);
  }, []);
  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger
        render={
          <Button
            variant="outline"
            className="h-8 gap-2 bg-background text-xs text-muted-foreground sm:w-52 sm:justify-start"
            aria-label="화면 검색"
          />
        }
      >
        <Search className="size-3.5" />
        <span className="hidden sm:inline">화면 검색...</span>
        <kbd className="ml-auto hidden rounded border px-1 text-[10px] sm:inline">
          Ctrl K
        </kbd>
      </DialogTrigger>
      <DialogContent
        className="overflow-hidden p-0 sm:max-w-md"
        showCloseButton={false}
      >
        <DialogHeader className="sr-only">
          <DialogTitle>화면 검색</DialogTitle>
          <DialogDescription>이동할 화면 이름을 입력하세요.</DialogDescription>
        </DialogHeader>
        <Command>
          <CommandInput
            placeholder="어떤 화면을 찾으시나요?"
            aria-label="화면 이름 검색"
          />
          <CommandList>
            <CommandEmpty>일치하는 화면이 없습니다.</CommandEmpty>
            <CommandGroup heading="작업 공간">
              {navigation.map(({ path, label, icon: Icon }) => (
                <CommandItem
                  key={path}
                  value={label}
                  onSelect={() => {
                    setOpen(false);
                    navigate(path);
                  }}
                >
                  <Icon className="size-4" />
                  {label}
                </CommandItem>
              ))}
            </CommandGroup>
          </CommandList>
        </Command>
      </DialogContent>
    </Dialog>
  );
}

export function AdminLayout({ children }: PropsWithChildren) {
  const [mobileOpen, setMobileOpen] = useState(false);
  const [collapsed, setCollapsed] = useState(false);
  const { resolvedTheme, setTheme } = useTheme();
  const { pathname } = useLocation();
  const current = navigation.find((item) => item.path === pathname);
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
            <Navigation compact={collapsed} />
          </div>
          <div className="mt-auto border-t pt-3">
            {!collapsed && (
              <div className="mb-3 flex items-center gap-2.5 px-2 py-2">
                <span className="grid size-8 place-items-center rounded-lg border bg-background text-xs font-medium">
                  E
                </span>
                <div>
                  <p className="text-xs font-medium">Etude</p>
                  <p className="mt-0.5 text-[11px] text-muted-foreground">
                    설계 · 구현 · 검증
                  </p>
                </div>
                <ArrowUpRight className="ml-auto size-3.5 text-muted-foreground" />
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
                <Link to="/" className="hidden text-muted-foreground sm:inline">
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
          <footer className="flex flex-wrap items-center justify-between gap-2 border-t px-5 py-4 text-[11px] text-muted-foreground sm:px-8">
            <span className="flex items-center gap-1.5">
              <CommandIcon className="size-3" /> Etude Workspace
            </span>
            <span>조금씩 만들고, 더 깊이 이해합니다.</span>
          </footer>
        </div>
        <SheetContent side="left" className="w-72! gap-0 p-3">
          <SheetHeader className="mb-6 px-1">
            <SheetTitle>Etude</SheetTitle>
            <SheetDescription>프로젝트 작업 공간</SheetDescription>
          </SheetHeader>
          <Navigation close={() => setMobileOpen(false)} />
        </SheetContent>
      </div>
    </Sheet>
  );
}
