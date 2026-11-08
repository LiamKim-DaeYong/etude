import { Skeleton } from './components/skeleton';

export function LoadingState({
  title = '화면 불러오는 중',
  description = '잠시만 기다려 주세요.',
  titleAs: Title = 'h2',
}: {
  title?: string;
  description?: string;
  titleAs?: 'h1' | 'h2' | 'h3';
}) {
  return (
    <div role="status" className="grid min-h-72 content-center gap-5 px-5 py-8">
      <div className="space-y-2 text-center">
        <Title className="text-base font-semibold">{title}</Title>
        <p className="text-sm leading-6 text-muted-foreground">{description}</p>
      </div>
      <div aria-hidden="true" className="mx-auto w-full max-w-sm space-y-3">
        <Skeleton className="h-4 w-2/3 motion-reduce:animate-none" />
        <Skeleton className="h-3 w-full motion-reduce:animate-none" />
        <Skeleton className="h-3 w-4/5 motion-reduce:animate-none" />
      </div>
    </div>
  );
}
