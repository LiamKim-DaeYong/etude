import { useState } from 'react';
import { Check, Search } from 'lucide-react';
import {
  Badge,
  Input,
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@etude/ui';
import { SampleCard } from './SampleCard';

const componentRows = [
  { name: 'Button', category: '기본 요소', description: '사용자의 주요 행동' },
  { name: 'Input', category: '입력 폼', description: '텍스트와 값 입력' },
  { name: 'Select', category: '입력 폼', description: '목록에서 항목 선택' },
  { name: 'Dialog', category: '피드백', description: '확인과 추가 정보' },
  { name: 'Tabs', category: '탐색', description: '관련 콘텐츠 전환' },
  { name: 'Table', category: '목록', description: '데이터 비교와 탐색' },
];

export function ComponentTable() {
  const [query, setQuery] = useState('');
  const rows = componentRows.filter((row) =>
    `${row.name} ${row.category} ${row.description}`
      .toLowerCase()
      .includes(query.toLowerCase()),
  );
  return (
    <SampleCard
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
    </SampleCard>
  );
}
