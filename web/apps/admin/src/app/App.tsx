import { Suspense, useEffect, useRef } from 'react';
import {
  matchPath,
  Navigate,
  Route,
  Routes,
  useLocation,
} from 'react-router-dom';
import { LoadingState } from '@etude/ui';
import { AdminLayout } from './layout/AdminLayout';
import { previewPaths, routes } from './routes';
import { NotFound } from '@/pages/NotFound';
import { ErrorBoundary } from './ErrorBoundary';
import { PageErrorFallback } from './PageErrorFallback';

export function App() {
  const { pathname, search } = useLocation();
  const previousPath = useRef(pathname);

  useEffect(() => {
    const current = routes.find((item) => matchPath(item.path, pathname));
    document.title = `${current?.label ?? '페이지를 찾을 수 없습니다'} · Etude`;
    if (previousPath.current !== pathname) {
      document.getElementById('main-content')?.focus();
      window.scrollTo({ top: 0, behavior: 'instant' });
      previousPath.current = pathname;
    }
  }, [pathname]);

  return (
    <AdminLayout>
      <ErrorBoundary key={pathname + search} fallback={<PageErrorFallback />}>
        <Suspense fallback={<LoadingState titleAs="h1" />}>
          <Routes>
            <Route
              path="/"
              element={<Navigate to={previewPaths.home} replace />}
            />
            {routes.map(({ path, Component }) => (
              <Route key={path} path={path} element={<Component />} />
            ))}
            <Route path="*" element={<NotFound />} />
          </Routes>
        </Suspense>
      </ErrorBoundary>
    </AdminLayout>
  );
}
