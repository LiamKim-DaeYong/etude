import { ChevronDown } from 'lucide-react';
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
  Button,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  Separator,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
  toast,
} from '@etude/ui';
import { SampleCard } from './SampleCard';
import { ComponentTable } from './ComponentTable';

export function NavigationExamples() {
  return (
    <>
      <ComponentTable />
      <div className="grid items-start gap-5 xl:grid-cols-2">
        <SampleCard
          title="탭과 메뉴"
          description="관련 콘텐츠와 추가 행동을 구분해 제공합니다."
        >
          <Tabs defaultValue="first">
            <TabsList aria-label="탭 예시">
              <TabsTrigger value="first">첫 번째</TabsTrigger>
              <TabsTrigger value="second">두 번째</TabsTrigger>
            </TabsList>
            <TabsContent
              value="first"
              className="rounded-lg border p-4 text-muted-foreground"
            >
              첫 번째 탭의 내용입니다.
            </TabsContent>
            <TabsContent
              value="second"
              className="rounded-lg border p-4 text-muted-foreground"
            >
              두 번째 탭의 내용입니다.
            </TabsContent>
          </Tabs>
          <Separator className="my-5" />
          <DropdownMenu>
            <DropdownMenuTrigger render={<Button variant="outline" />}>
              추가 작업
              <ChevronDown />
            </DropdownMenuTrigger>
            <DropdownMenuContent>
              {['이름순 정렬', '분류순 정렬'].map((label) => (
                <DropdownMenuItem
                  key={label}
                  onClick={() =>
                    toast.add({
                      title: label,
                      description: '메뉴 선택 동작의 예시입니다.',
                      type: 'info',
                    })
                  }
                >
                  {label}
                </DropdownMenuItem>
              ))}
            </DropdownMenuContent>
          </DropdownMenu>
        </SampleCard>
        <SampleCard
          title="접고 펼치기"
          description="필요한 정보를 선택적으로 확인합니다."
        >
          <Accordion defaultValue={['source']}>
            <AccordionItem value="source">
              <AccordionTrigger>
                이 컴포넌트는 어디에서 왔나요?
              </AccordionTrigger>
              <AccordionContent>
                공식 shadcn CLI로 가져온 Base UI 기반 컴포넌트입니다.
              </AccordionContent>
            </AccordionItem>
            <AccordionItem value="keyboard">
              <AccordionTrigger>키보드로 사용할 수 있나요?</AccordionTrigger>
              <AccordionContent>
                Tab 키로 이동하고 Enter 또는 Space 키로 선택할 수 있습니다.
              </AccordionContent>
            </AccordionItem>
          </Accordion>
        </SampleCard>
      </div>
    </>
  );
}
