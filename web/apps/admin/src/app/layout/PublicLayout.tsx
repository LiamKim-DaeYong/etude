import type { PropsWithChildren } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '@etude/ui';

export function PublicLayout({ children }: PropsWithChildren) {
  return (
    <div className="flex min-h-dvh flex-col">
      <a
        href="#main-content"
        className="pointer-events-none fixed top-3 left-4 z-50 rounded-lg border bg-popover px-4 py-3 text-sm opacity-0 shadow-md focus:pointer-events-auto focus:opacity-100"
      >
        본문으로 건너뛰기
      </a>
      <header className="border-b">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-x-6 gap-y-4 px-5 py-4 sm:px-8">
          <Link
            to="/"
            aria-label="Etude 소개"
            className="text-xl font-semibold"
          >
            Etude
          </Link>
          <div className="flex items-center gap-2 sm:order-last">
            <span id="access-status" className="text-xs text-muted-foreground">
              가입·로그인 준비 중
            </span>
            <Button
              variant="outline"
              size="sm"
              disabled
              aria-describedby="access-status"
            >
              로그인
            </Button>
          </div>
          <nav
            aria-label="서비스 안내"
            className="flex w-full flex-wrap gap-x-6 gap-y-2 text-sm sm:w-auto"
          >
            <Link to="/#overview" className="hover:underline">
              서비스 소개
            </Link>
            <Link to="/#start" className="hover:underline">
              이용 순서
            </Link>
            <Link to="/#modules" className="hover:underline">
              확장 모듈
            </Link>
          </nav>
        </div>
      </header>
      <main
        id="main-content"
        tabIndex={-1}
        className="mx-auto w-full max-w-6xl flex-1 px-5 outline-none sm:px-8"
      >
        {children}
      </main>
      <footer className="mt-16 border-t">
        <div className="mx-auto flex max-w-6xl flex-wrap justify-between gap-3 px-5 py-6 text-sm text-muted-foreground sm:px-8">
          <span className="font-medium text-foreground">Etude</span>
          <p>서비스 준비 중 · 제공 범위와 일정은 추후 안내할 예정이에요.</p>
        </div>
      </footer>
    </div>
  );
}
