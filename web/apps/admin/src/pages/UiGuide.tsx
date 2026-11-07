import { useState, type PropsWithChildren } from 'react';
import {
  Bell,
  Check,
  ChevronDown,
  Inbox,
  Info,
  Search,
  Settings2,
} from 'lucide-react';
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
  Alert,
  AlertDescription,
  AlertTitle,
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Checkbox,
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  EmptyState,
  Input,
  Label,
  PageHeader,
  Progress,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
  Separator,
  Skeleton,
  Switch,
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
  Textarea,
  toast,
} from '@etude/ui';

function Sample({
  title,
  description,
  children,
  className = '',
}: PropsWithChildren<{
  title: string;
  description: string;
  className?: string;
}>) {
  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle>
          <h2>{title}</h2>
        </CardTitle>
        <CardDescription className="leading-6">{description}</CardDescription>
      </CardHeader>
      <CardContent>{children}</CardContent>
    </Card>
  );
}

const componentRows = [
  { name: 'Button', category: '기본 요소', description: '사용자의 주요 행동' },
  { name: 'Input', category: '입력 폼', description: '텍스트와 값 입력' },
  { name: 'Select', category: '입력 폼', description: '목록에서 항목 선택' },
  { name: 'Dialog', category: '피드백', description: '확인과 추가 정보' },
  { name: 'Tabs', category: '탐색', description: '관련 콘텐츠 전환' },
  { name: 'Table', category: '목록', description: '데이터 비교와 탐색' },
];

function ComponentTable() {
  const [query, setQuery] = useState('');
  const rows = componentRows.filter((row) =>
    `${row.name} ${row.category} ${row.description}`
      .toLowerCase()
      .includes(query.toLowerCase()),
  );
  return (
    <Sample
      title="표와 검색"
      description="목록을 읽고 필요한 항목을 찾아보세요."
    >
      <div className="relative mb-4 max-w-xs">
        <Search
          className="absolute top-2.5 left-2.5 size-3.5 text-muted-foreground"
          aria-hidden="true"
        />
        <Input
          aria-label="컴포넌트 검색"
          placeholder="컴포넌트 검색..."
          className="pl-8"
          value={query}
          onChange={(event) => setQuery(event.target.value)}
        />
      </div>
      <Table>
        <TableCaption>
          현재 UI 기반에서 사용하는 컴포넌트 예시입니다.
        </TableCaption>
        <TableHeader>
          <TableRow>
            <TableHead>컴포넌트</TableHead>
            <TableHead>분류</TableHead>
            <TableHead className="hidden sm:table-cell">역할</TableHead>
            <TableHead className="text-right">상태</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {rows.map((row) => (
            <TableRow key={row.name}>
              <TableCell className="font-medium">{row.name}</TableCell>
              <TableCell className="text-muted-foreground">
                {row.category}
              </TableCell>
              <TableCell className="hidden text-muted-foreground sm:table-cell">
                {row.description}
              </TableCell>
              <TableCell className="text-right">
                <Badge variant="outline" className="gap-1">
                  <Check className="size-3" />
                  사용 가능
                </Badge>
              </TableCell>
            </TableRow>
          ))}
          {!rows.length && (
            <TableRow>
              <TableCell
                colSpan={4}
                className="py-10 text-center text-muted-foreground"
              >
                검색 결과가 없습니다.
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </Sample>
  );
}

export default function UiGuide() {
  const [dialogOpen, setDialogOpen] = useState(false);
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
          <div className="grid items-start gap-5 xl:grid-cols-2">
            <Sample
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
                  onClick={() =>
                    toast.add({ title: '보조 버튼', type: 'info' })
                  }
                >
                  보조 버튼
                </Button>
                <Button
                  variant="secondary"
                  onClick={() =>
                    toast.add({ title: '대체 버튼', type: 'info' })
                  }
                >
                  대체 버튼
                </Button>
                <Button
                  variant="ghost"
                  onClick={() =>
                    toast.add({ title: '간단한 동작', type: 'info' })
                  }
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
                  onClick={() =>
                    toast.add({ title: '설정 버튼', type: 'info' })
                  }
                >
                  <Settings2 />
                </Button>
                <p className="text-xs leading-6 text-muted-foreground">
                  아이콘 버튼에도 동작을 설명하는 이름을 제공합니다.
                </p>
              </div>
            </Sample>
            <Sample
              title="대화상자와 알림"
              description="사용자가 확인해야 할 정보와 처리 결과를 전달합니다."
            >
              <div className="flex flex-wrap gap-2">
                <Dialog open={dialogOpen} onOpenChange={setDialogOpen}>
                  <DialogTrigger render={<Button />}>
                    대화상자 열기
                  </DialogTrigger>
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>대화상자 미리보기</DialogTitle>
                      <DialogDescription>
                        중요한 내용을 확인하거나 선택이 필요할 때 사용합니다.
                      </DialogDescription>
                    </DialogHeader>
                    <p className="text-sm leading-7 text-muted-foreground">
                      키보드로 항목 사이를 이동할 수 있고, Esc 키로 닫을 수
                      있습니다.
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
            </Sample>
            <Sample
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
            </Sample>
            <Sample
              title="한글 타이포그래피"
              description="한글과 영문을 같은 글꼴과 굵기 기준으로 표시합니다."
            >
              <p className="text-xl font-semibold tracking-tight">
                읽기 편한 화면의 기본
              </p>
              <p className="mt-3 text-sm leading-7 text-muted-foreground">
                복잡한 흐름도 명확하게 전달할 수 있도록 제목과 본문, 보조 설명
                사이에 일관된 위계를 둡니다.
              </p>
              <p className="mt-4 text-xs text-muted-foreground">
                Pretendard Variable · 가변 글꼴
              </p>
            </Sample>
          </div>
        </TabsContent>
        <TabsContent value="forms">
          <div className="grid items-start gap-5 xl:grid-cols-2">
            <Sample
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
            </Sample>
            <Sample
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
                    <Label htmlFor="example-checkbox">
                      안내 내용을 확인했습니다
                    </Label>
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
            </Sample>
          </div>
        </TabsContent>
        <TabsContent value="navigation" className="space-y-5">
          <ComponentTable />
          <div className="grid items-start gap-5 xl:grid-cols-2">
            <Sample
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
            </Sample>
            <Sample
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
                  <AccordionTrigger>
                    키보드로 사용할 수 있나요?
                  </AccordionTrigger>
                  <AccordionContent>
                    Tab 키로 이동하고 Enter 또는 Space 키로 선택할 수 있습니다.
                  </AccordionContent>
                </AccordionItem>
              </Accordion>
            </Sample>
          </div>
        </TabsContent>
        <TabsContent value="feedback">
          <div className="grid items-start gap-5 xl:grid-cols-2">
            <Sample
              title="빈 화면"
              description="현재 상태와 다음 행동을 안내합니다."
            >
              <EmptyState
                icon={<Inbox className="size-5" />}
                title="아직 등록된 항목이 없습니다"
                description="목록이 비어 있을 때 어떤 정보가 들어올지 안내합니다."
              />
            </Sample>
            <Sample
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
            </Sample>
            <Sample
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
            </Sample>
          </div>
        </TabsContent>
      </Tabs>
      <p className="mt-8 border-t pt-4 text-xs leading-6 text-muted-foreground">
        모든 항목은 UI 동작을 확인하는 예시입니다. 입력 내용은 서버에 저장되지
        않습니다.
      </p>
    </>
  );
}
