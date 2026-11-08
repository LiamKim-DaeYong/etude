import { useState } from 'react';
import { Inbox, Info } from 'lucide-react';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Button,
  EmptyState,
  ErrorState,
  LoadingState,
  Progress,
  Separator,
} from '@etude/ui';
import { SampleCard } from './SampleCard';

export function FeedbackExamples() {
  const [recovered, setRecovered] = useState(false);

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
        description="로딩 안내와 콘텐츠 자리 표시자를 함께 보여줍니다."
      >
        <LoadingState titleAs="h3" />
        <Separator className="my-6" />
        <div className="mb-3 flex justify-between text-xs">
          <span>진행률 예시</span>
          <span className="text-muted-foreground">68%</span>
        </div>
        <Progress value={68} aria-label="진행률 예시" />
      </SampleCard>
      <SampleCard
        title="오류와 복구"
        description="아래 버튼으로 오류 표시와 복구 후 표시를 확인할 수 있습니다."
        className="xl:col-span-2"
      >
        {recovered ? (
          <div role="status">
            <EmptyState
              titleAs="h3"
              title="화면 표시를 복구했습니다"
              description="복구 동작을 확인하는 예시입니다. 실제 요청은 보내지 않습니다."
            />
          </div>
        ) : (
          <ErrorState
            titleAs="h3"
            title="내용을 불러오지 못했습니다"
            description="복구 동작을 확인하는 예시입니다. 실제 통신 오류가 아닙니다."
          />
        )}
        <div className="flex justify-center">
          <Button
            variant="outline"
            onClick={() => setRecovered((current) => !current)}
          >
            {recovered ? '오류 예시 다시 보기' : '복구 예시 확인'}
          </Button>
        </div>
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
