import { Blocks, LayoutDashboard, Settings2 } from 'lucide-react';

export const navigation = [
  { path: '/', label: '워크스페이스', icon: LayoutDashboard },
  { path: '/ui', label: 'UI 가이드', icon: Blocks },
  { path: '/settings', label: '화면 설정', icon: Settings2 },
] as const;
