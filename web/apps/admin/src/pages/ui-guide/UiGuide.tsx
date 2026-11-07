import {
  Badge,
  PageHeader,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@etude/ui';
import { BasicsExamples } from './examples/BasicsExamples';
import { FormExamples } from './examples/FormExamples';
import { NavigationExamples } from './examples/NavigationExamples';
import { FeedbackExamples } from './examples/FeedbackExamples';

export default function UiGuide() {
  return (
    <>
      <PageHeader
        eyebrow="Design system"
        title="UI 가이드"
        description="화면을 구성하는 공통 요소를 직접 사용해 보세요."
        action={
          <Badge variant="outline" className="py-1">
            shadcn/ui · Base UI
          </Badge>
        }
      />
      <Tabs defaultValue="basics" className="gap-6">
        <div className="overflow-x-auto pb-1">
          <TabsList
            aria-label="컴포넌트 분류"
            variant="line"
            className="h-10 gap-5 border-b"
          >
            <TabsTrigger value="basics">기본 요소</TabsTrigger>
            <TabsTrigger value="forms">입력 폼</TabsTrigger>
            <TabsTrigger value="navigation">탐색과 목록</TabsTrigger>
            <TabsTrigger value="feedback">상태와 피드백</TabsTrigger>
          </TabsList>
        </div>
        <TabsContent value="basics" className="space-y-5">
          <BasicsExamples />
        </TabsContent>
        <TabsContent value="forms">
          <FormExamples />
        </TabsContent>
        <TabsContent value="navigation" className="space-y-5">
          <NavigationExamples />
        </TabsContent>
        <TabsContent value="feedback">
          <FeedbackExamples />
        </TabsContent>
      </Tabs>
      <p className="mt-8 border-t pt-4 text-xs leading-6 text-muted-foreground">
        모든 항목은 UI 동작을 확인하는 예시입니다. 입력 내용은 서버에 저장되지
        않습니다.
      </p>
    </>
  );
}
