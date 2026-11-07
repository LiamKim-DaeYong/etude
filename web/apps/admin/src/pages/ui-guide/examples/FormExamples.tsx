import {
  Button,
  Checkbox,
  Input,
  Label,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Separator,
  Switch,
  Textarea,
  toast,
} from '@etude/ui';
import { SampleCard } from './SampleCard';

export function FormExamples() {
  return (
    <div className="grid items-start gap-5 xl:grid-cols-2">
      <SampleCard
        title="텍스트 입력"
        description="이름과 도움말을 통해 필요한 정보를 안내합니다."
      >
        <form
          className="space-y-5"
          onSubmit={(event) => {
            event.preventDefault();
            toast.add({
              title: '입력값을 확인했습니다',
              description: '예시 폼이며 서버에 저장하지 않습니다.',
              type: 'success',
            });
          }}
        >
          <div className="space-y-2">
            <Label htmlFor="example-name">이름</Label>
            <Input
              id="example-name"
              name="name"
              placeholder="이름을 입력하세요"
              required
              maxLength={80}
            />
            <p className="text-xs text-muted-foreground">
              입력 요소를 확인하는 예시입니다.
            </p>
          </div>
          <div className="space-y-2">
            <Label htmlFor="example-email">이메일</Label>
            <Input
              id="example-email"
              name="email"
              type="email"
              placeholder="name@example.com"
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="example-note">메모</Label>
            <Textarea
              id="example-note"
              placeholder="내용을 입력하세요"
              maxLength={1000}
            />
          </div>
          <Button type="submit">입력값 확인</Button>
        </form>
      </SampleCard>
      <SampleCard
        title="선택과 전환"
        description="선택지의 수와 목적에 맞는 요소를 사용합니다."
      >
        <div className="space-y-6">
          <div className="space-y-2">
            <Label>상태 선택</Label>
            <Select
              defaultValue="all"
              items={[
                { value: 'all', label: '모든 상태' },
                { value: 'active', label: '진행 중' },
                { value: 'done', label: '완료' },
              ]}
            >
              <SelectTrigger className="w-full" aria-label="상태 선택">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">모든 상태</SelectItem>
                <SelectItem value="active">진행 중</SelectItem>
                <SelectItem value="done">완료</SelectItem>
              </SelectContent>
            </Select>
          </div>
          <Separator />
          <div className="flex items-start gap-3">
            <Checkbox id="example-checkbox" defaultChecked />
            <div>
              <Label htmlFor="example-checkbox">안내 내용을 확인했습니다</Label>
              <p className="mt-1 text-xs leading-6 text-muted-foreground">
                독립적으로 선택하는 항목에 사용합니다.
              </p>
            </div>
          </div>
          <div className="flex items-center justify-between gap-4 rounded-lg border p-4">
            <div>
              <Label htmlFor="example-switch">알림 받기</Label>
              <p className="mt-1 text-xs text-muted-foreground">
                선택 요소의 동작 예시입니다.
              </p>
            </div>
            <Switch id="example-switch" aria-label="알림 받기" />
          </div>
        </div>
      </SampleCard>
    </div>
  );
}
