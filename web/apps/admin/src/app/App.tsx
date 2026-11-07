import { lazy, Suspense, useEffect, useRef } from 'react';
import { Route, Routes, useLocation } from 'react-router-dom';
import { LoaderCircle } from 'lucide-react';
import { AdminLayout } from './layout/AdminLayout';
import { navigation } from './navigation';
import { NotFound } from '@/pages/NotFound';

const Workspace = lazy(() => import('@/pages/Workspace'));
const UiGuide = lazy(() => import('@/pages/UiGuide'));
const Settings = lazy(() => import('@/pages/Settings'));

export function App() {
  const { pathname } = useLocation();
  const previousPath = useRef(pathname);

  useEffect(() => {
    const current = navigation.find((item) => item.path === pathname);
    document.title = `${current?.label ?? '페이지를 찾을 수 없습니다'} · Etude`;
    if (previousPath.current !== pathname) {
      document.getElementById('main-content')?.focus();
      window.scrollTo({ top: 0, behavior: 'instant' });
      previousPath.current = pathname;
    }
  }, [pathname]);

  return (
    <AdminLayout>
      <Suspense
        fallback={
          <div
            className="grid min-h-72 place-items-center"
            role="status"
            aria-label="화면 불러오는 중"
          >
            <LoaderCircle className="size-5 animate-spin text-muted-foreground" />
          </div>
        }
      >
        <Routes>
          <Route path="/" element={<Workspace />} />
          <Route path="/ui" element={<UiGuide />} />
          <Route path="/settings" element={<Settings />} />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </Suspense>
    </AdminLayout>
  );
}
