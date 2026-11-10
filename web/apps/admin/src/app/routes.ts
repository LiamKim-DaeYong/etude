import { lazy } from 'react';
import { Blocks, LayoutDashboard, Settings2 } from 'lucide-react';

export const publicRoute = {
  path: '/',
  label: '서비스 소개',
  Component: lazy(() => import('@/pages/landing/Landing')),
} as const;

export const routes = [
  {
    path: '/workspace',
    label: '워크스페이스',
    icon: LayoutDashboard,
    Component: lazy(() => import('@/pages/Workspace')),
  },
  {
    path: '/ui',
    label: 'UI 가이드',
    icon: Blocks,
    Component: lazy(() => import('@/pages/ui-guide/UiGuide')),
  },
  {
    path: '/settings',
    label: '화면 설정',
    icon: Settings2,
    Component: lazy(() => import('@/pages/Settings')),
  },
] as const;
