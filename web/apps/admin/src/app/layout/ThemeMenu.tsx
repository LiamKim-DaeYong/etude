import { Monitor, Moon, Sun } from 'lucide-react';
import {
  Button,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
  isTheme,
  useTheme,
} from '@etude/ui';

export function ThemeMenu() {
  const { theme, resolvedTheme, setTheme } = useTheme();
  const Icon =
    theme === 'system' ? Monitor : resolvedTheme === 'dark' ? Moon : Sun;
  return (
    <DropdownMenu>
      <DropdownMenuTrigger
        render={
          <Button variant="ghost" size="icon" aria-label="화면 테마 선택" />
        }
      >
        <Icon aria-hidden="true" />
      </DropdownMenuTrigger>
      <DropdownMenuContent align="end">
        <DropdownMenuRadioGroup
          value={theme}
          onValueChange={(value) => {
            if (isTheme(value)) setTheme(value);
          }}
        >
          <DropdownMenuRadioItem value="light" closeOnClick>
            라이트
          </DropdownMenuRadioItem>
          <DropdownMenuRadioItem value="dark" closeOnClick>
            다크
          </DropdownMenuRadioItem>
          <DropdownMenuRadioItem value="system" closeOnClick>
            시스템 설정
          </DropdownMenuRadioItem>
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
