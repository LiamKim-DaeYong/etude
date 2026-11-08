import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Search } from 'lucide-react';
import {
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
} from '@etude/ui';
import { routes } from '../routes';

export function ScreenSearch() {
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
            aria-keyshortcuts="Control+K Meta+K"
          />
        }
      >
        <Search className="size-3.5" aria-hidden="true" />
        <span className="sr-only sm:not-sr-only">화면 검색</span>{' '}
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
              {routes.map(({ path, label, icon: Icon }) => (
                <CommandItem
                  key={path}
                  value={label}
                  onSelect={() => {
                    setOpen(false);
                    void navigate(path);
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
