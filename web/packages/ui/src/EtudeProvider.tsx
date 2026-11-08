'use client';

import {
  useEffect,
  useState,
  useSyncExternalStore,
  type PropsWithChildren,
} from 'react';
import {
  ThemeContext,
  isTheme,
  themeStorageKey,
  type Theme,
} from './theme-context';
import { TooltipProvider } from './components/tooltip';
import { Toaster } from './components/toast';
import { CSPProvider } from '@base-ui/react/csp-provider';

function readTheme(): Theme {
  try {
    const stored = window.localStorage.getItem(themeStorageKey);
    return isTheme(stored) ? stored : 'system';
  } catch {
    return 'system';
  }
}
function subscribeSystem(callback: () => void) {
  const media = window.matchMedia('(prefers-color-scheme: dark)');
  media.addEventListener('change', callback);
  return () => media.removeEventListener('change', callback);
}
const getSystemTheme = () =>
  window.matchMedia('(prefers-color-scheme: dark)').matches;

export function EtudeProvider({ children }: PropsWithChildren) {
  const [theme, updateTheme] = useState<Theme>(readTheme);
  const systemDark = useSyncExternalStore(
    subscribeSystem,
    getSystemTheme,
    () => false,
  );
  const resolvedTheme =
    theme === 'system' ? (systemDark ? 'dark' : 'light') : theme;

  useEffect(() => {
    document.documentElement.classList.toggle('dark', resolvedTheme === 'dark');
    document.documentElement.dataset.theme = resolvedTheme;
  }, [resolvedTheme]);

  useEffect(() => {
    const handleStorage = (event: StorageEvent) => {
      if (event.key === themeStorageKey || event.key === null)
        updateTheme(readTheme());
    };
    window.addEventListener('storage', handleStorage);
    return () => window.removeEventListener('storage', handleStorage);
  }, []);

  function setTheme(value: Theme) {
    if (!isTheme(value)) return;
    updateTheme(value);
    try {
      window.localStorage.setItem(themeStorageKey, value);
    } catch {
      /* Theme still works when storage is blocked. */
    }
  }

  return (
    <ThemeContext value={{ theme, resolvedTheme, setTheme }}>
      <CSPProvider disableStyleElements>
        <TooltipProvider delay={200}>
          <Toaster>{children}</Toaster>
        </TooltipProvider>
      </CSPProvider>
    </ThemeContext>
  );
}
