import { Suspense, useEffect, useRef } from 'react';
import { matchPath, Route, Routes, useLocation } from 'react-router-dom';
import { LoadingState } from '@etude/ui';
import { AdminLayout } from './layout/AdminLayout';
import { PublicLayout } from './layout/PublicLayout';
import { publicRoute, routes } from './routes';
import { NotFound } from '@/pages/NotFound';
import { ErrorBoundary } from './ErrorBoundary';
import { PageErrorFallback } from './PageErrorFallback';

export function App() {
  const { pathname, hash } = useLocation();
  const previousLocation = useRef(pathname + hash);
  const isAdminPage = routes.some((item) => matchPath(item.path, pathname));
  const Layout = isAdminPage ? AdminLayout : PublicLayout;

  useEffect(() => {
    const current = [publicRoute, ...routes].find((item) =>
      matchPath(item.path, pathname),
    );
    document.title = `${current?.label ?? '페이지를 찾을 수 없습니다'} · Etude`;
    if (previousLocation.current !== pathname + hash && !hash) {
      document.getElementById('main-content')?.focus();
      window.scrollTo({ top: 0, behavior: 'instant' });
    }
    previousLocation.current = pathname + hash;
  }, [pathname, hash]);

  return (
    <Layout>
      <ErrorBoundary
        key={pathname}
        fallback={<PageErrorFallback publicPage={!isAdminPage} />}
      >
        <Suspense fallback={<LoadingState titleAs="h1" />}>
          <Routes>
            {[publicRoute, ...routes].map(({ path, Component }) => (
              <Route key={path} path={path} element={<Component />} />
            ))}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </Suspense>
      </ErrorBoundary>
    </Layout>
  );
}
