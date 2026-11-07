import { useState } from 'react';
import { Bell, Info, Settings2 } from 'lucide-react';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  Badge,
  Button,
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  Separator,
  toast,
} from '@etude/ui';
import { SampleCard } from './SampleCard';

export function BasicsExamples() {
  const [dialogOpen, setDialogOpen] = useState(false);
  return (
    <div className="grid items-start gap-5 xl:grid-cols-2">
      <SampleCard
        title="버튼"
        description="행동의 중요도와 목적에 맞춰 선택합니다."
      >
        <div className="flex flex-wrap gap-2">
          <Button
            onClick={() =>
              toast.add({
                title: '기본 버튼',
                description: '강조할 행동에 사용하는 버튼입니다.',
                type: 'info',
              })
            }
          >
            기본 버튼
          </Button>
          <Button
            variant="outline"
            onClick={() => toast.add({ title: '보조 버튼', type: 'info' })}
          >
            보조 버튼
          </Button>
          <Button
            variant="secondary"
            onClick={() => toast.add({ title: '대체 버튼', type: 'info' })}
          >
            대체 버튼
          </Button>
          <Button
            variant="ghost"
            onClick={() => toast.add({ title: '간단한 동작', type: 'info' })}
          >
            간단한 동작
          </Button>
          <Button disabled>비활성</Button>
        </div>
        <Separator className="my-5" />
        <div className="flex items-center gap-3">
          <Button
            variant="outline"
            size="icon"
            aria-label="설정 버튼 예시"
            onClick={() => toast.add({ title: '설정 버튼', type: 'info' })}
          >
            <Settings2 />
          </Button>
          <p className="text-xs leading-6 text-muted-foreground">
            아이콘 버튼에도 동작을 설명하는 이름을 제공합니다.
          </p>
        </div>
      </SampleCard>
      <SampleCard
        title="대화상자와 알림"
        description="사용자가 확인해야 할 정보와 처리 결과를 전달합니다."
      >
        <div className="flex flex-wrap gap-2">
          <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
            <DialogTrigger render={<Button />}>대화상자 열기</DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>대화상자 미리보기</DialogTitle>
                <DialogDescription>
                  중요한 내용을 확인하거나 선택이 필요할 때 사용합니다.
                </DialogDescription>
              </DialogHeader>
              <p className="text-sm leading-7 text-muted-foreground">
                키보드로 항목 사이를 이동할 수 있고, Esc 키로 닫을 수 있습니다.
              </p>
              <DialogFooter>
                <DialogClose render={<Button variant="outline" />}>
                  닫기
                </DialogClose>
                <Button
                  onClick={() => {
                    setDialogOpen(false);
                    toast.add({
                      title: '확인했습니다',
                      description: '대화상자를 닫았습니다.',
                      type: 'success',
                    });
                  }}
                >
                  확인
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
          <Button
            variant="outline"
            onClick={() =>
              toast.add({
                title: '알림 미리보기',
                description: '알림은 화면 오른쪽 아래에 표시됩니다.',
                type: 'success',
              })
            }
          >
            <Bell />
            알림 확인
          </Button>
        </div>
        <Alert className="mt-5">
          <Info />
          <AlertTitle>필요한 순간에, 명확하게</AlertTitle>
          <AlertDescription>
            대화상자는 확인이 필요할 때, 알림은 작업 결과를 전달할 때
            사용합니다.
          </AlertDescription>
        </Alert>
      </SampleCard>
      <SampleCard
        title="상태 표시"
        description="색상과 텍스트를 함께 사용해 의미를 전달합니다."
      >
        <div className="flex flex-wrap gap-2">
          <Badge>기본</Badge>
          <Badge variant="secondary">보조</Badge>
          <Badge variant="outline">대기</Badge>
          <Badge variant="destructive">확인 필요</Badge>
          <Badge variant="outline" className="gap-1.5">
            <span className="size-1.5 rounded-full bg-emerald-500" />
            완료
          </Badge>
        </div>
      </SampleCard>
      <SampleCard
        title="한글 타이포그래피"
        description="한글과 영문을 같은 글꼴과 굵기 기준으로 표시합니다."
      >
        <p className="text-xl font-semibold tracking-tight">
          읽기 편한 화면의 기본
        </p>
        <p className="mt-3 text-sm leading-7 text-muted-foreground">
          복잡한 흐름도 명확하게 전달할 수 있도록 제목과 본문, 보조 설명 사이에
          일관된 위계를 둡니다.
        </p>
        <p className="mt-4 text-xs text-muted-foreground">
          Pretendard Variable · 가변 글꼴
        </p>
      </SampleCard>
    </div>
  );
}
