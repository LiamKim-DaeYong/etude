import { Inbox, Info } from 'lucide-react';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  EmptyState,
  Progress,
  Separator,
  Skeleton,
} from '@etude/ui';
import { SampleCard } from './SampleCard';

export function FeedbackExamples() {
  return (
    <div className="grid items-start gap-5 xl:grid-cols-2">
      <SampleCard
        title="빈 화면"
        description="현재 상태와 다음 행동을 안내합니다."
      >
        <EmptyState
          titleAs="h3"
          icon={<Inbox className="size-5" />}
          title="아직 등록된 항목이 없습니다"
          description="목록이 비어 있을 때 어떤 정보가 들어올지 안내합니다."
        />
      </SampleCard>
      <SampleCard
        title="불러오는 중"
        description="화면이 준비되는 동안 콘텐츠의 위치를 보여줍니다."
      >
        <div className="flex gap-4">
          <Skeleton className="size-11 rounded-lg" />
          <div className="flex-1 space-y-3">
            <Skeleton className="h-4 w-2/3" />
            <Skeleton className="h-3 w-full" />
            <Skeleton className="h-3 w-4/5" />
          </div>
        </div>
        <Separator className="my-6" />
        <div className="mb-3 flex justify-between text-xs">
          <span>진행률 예시</span>
          <span className="text-muted-foreground">68%</span>
        </div>
        <Progress value={68} aria-label="진행률 예시" />
      </SampleCard>
      <SampleCard
        title="안내와 오류"
        description="내용의 중요도에 맞춰 메시지를 구분합니다."
        className="xl:col-span-2"
      >
        <div className="grid gap-4 sm:grid-cols-2">
          <Alert>
            <Info />
            <AlertTitle>안내 메시지</AlertTitle>
            <AlertDescription>
              진행 전에 알아두면 좋은 정보를 표시합니다.
            </AlertDescription>
          </Alert>
          <Alert variant="destructive">
            <Info />
            <AlertTitle>입력 내용을 확인해 주세요</AlertTitle>
            <AlertDescription>
              문제가 있는 항목을 수정한 뒤 다시 시도할 수 있습니다.
            </AlertDescription>
          </Alert>
        </div>
      </SampleCard>
    </div>
  );
}
