import { Monitor, Moon, Sun } from 'lucide-react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  PageHeader,
  RadioGroup,
  RadioGroupItem,
  cn,
  isTheme,
  useTheme,
} from '@etude/ui';

export default function Settings() {
  const { theme, setTheme } = useTheme();
  return (
    <>
      <PageHeader
        eyebrow="Preferences"
        title="화면 설정"
        description="이 브라우저에서 사용할 화면 모양을 선택하세요."
      />
      <Card className="max-w-4xl">
        <CardHeader>
          <CardTitle>화면 테마</CardTitle>
          <CardDescription>선택한 테마는 자동으로 저장됩니다.</CardDescription>
        </CardHeader>
        <CardContent>
          <RadioGroup
            value={theme}
            onValueChange={(value) => {
              if (isTheme(value)) setTheme(value);
            }}
            aria-label="화면 테마"
            className="grid gap-4 sm:grid-cols-3"
          >
            {[
              {
                value: 'light',
                label: '라이트',
                description: '밝고 선명한 화면',
                icon: Sun,
              },
              {
                value: 'dark',
                label: '다크',
                description: '차분한 어두운 화면',
                icon: Moon,
              },
              {
                value: 'system',
                label: '시스템 설정',
                description: '기기의 테마에 맞춤',
                icon: Monitor,
              },
            ].map(({ value, label, description, icon: Icon }) => (
              <label
                key={value}
                htmlFor={`theme-${value}`}
                className={cn(
                  'cursor-pointer rounded-xl border p-3 transition-colors hover:bg-muted/30 has-[:focus-visible]:ring-2 has-[:focus-visible]:ring-ring',
                  theme === value && 'border-foreground ring-1 ring-foreground',
                )}
              >
                <div
                  className={cn(
                    'relative mb-4 flex h-28 overflow-hidden rounded-md border border-zinc-200 bg-zinc-50',
                    value === 'dark' && 'border-zinc-700 bg-zinc-950',
                    value === 'system' &&
                      'bg-[linear-gradient(120deg,#fafafa_50%,#18181b_50%)]',
                  )}
                  aria-hidden="true"
                >
                  <span
                    className={cn(
                      'h-full w-1/4 border-r border-zinc-200 bg-white',
                      value === 'dark' && 'border-zinc-700 bg-zinc-900',
                    )}
                  />
                  <div className="flex-1 space-y-2 p-3">
                    <span
                      className={cn(
                        'block h-2 w-3/4 rounded bg-zinc-200',
                        value === 'dark' && 'bg-zinc-700',
                      )}
                    />
                    <span
                      className={cn(
                        'block h-9 rounded border border-zinc-200 bg-white',
                        value === 'dark' && 'border-zinc-700 bg-zinc-900',
                      )}
                    />
                    <span
                      className={cn(
                        'block h-2 w-1/2 rounded bg-zinc-200',
                        value === 'dark' && 'bg-zinc-700',
                      )}
                    />
                  </div>
                </div>
                <span className="flex items-center gap-2">
                  <Icon className="size-3.5" />
                  <span className="text-xs font-medium">{label}</span>
                  <RadioGroupItem
                    className="ml-auto"
                    id={`theme-${value}`}
                    value={value}
                    aria-label={label}
                  />
                </span>
                <span className="mt-2 block text-[11px] text-muted-foreground">
                  {description}
                </span>
              </label>
            ))}
          </RadioGroup>
          <div className="mt-6 border-t pt-4 text-xs leading-6 text-muted-foreground">
            테마는 현재 브라우저에 저장되며, 시스템 설정을 선택하면 기기의 변경
            사항을 따릅니다.
          </div>
        </CardContent>
      </Card>
    </>
  );
}
