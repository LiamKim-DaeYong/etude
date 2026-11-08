import { useRef, useState } from 'react';
import {
  Button,
  Checkbox,
  FormField,
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
  const nameRef = useRef<HTMLInputElement>(null);
  const emailRef = useRef<HTMLInputElement>(null);
  const [errors, setErrors] = useState<{ name?: string; email?: string }>({});

  function nameError(input: HTMLInputElement) {
    return input.value.trim() && input.validity.valid
      ? undefined
      : '이름을 1~80자로 입력해 주세요.';
  }

  function emailError(input: HTMLInputElement) {
    return input.validity.valid
      ? undefined
      : '올바른 이메일 주소를 입력해 주세요.';
  }

  return (
    <div className="grid items-start gap-5 xl:grid-cols-2">
      <SampleCard
        title="텍스트 입력"
        description="이름과 도움말을 통해 필요한 정보를 안내합니다."
      >
        <form
          aria-label="텍스트 입력 예시"
          className="space-y-5"
          noValidate
          onSubmit={(event) => {
            event.preventDefault();
            const name = nameRef.current;
            const email = emailRef.current;
            if (!name || !email) return;
            const nextErrors = {
              name: nameError(name),
              email: emailError(email),
            };
            setErrors(nextErrors);
            if (nextErrors.name || nextErrors.email) {
              (nextErrors.name ? name : email).focus();
              return;
            }
            toast.add({
              title: '입력값을 확인했습니다',
              description: '예시 폼이며 서버에 저장하지 않습니다.',
              type: 'success',
            });
          }}
        >
          <FormField
            label="이름"
            description="필수 입력입니다. 80자 이내로 입력해 주세요."
            error={errors.name}
          >
            {(controlProps) => (
              <Input
                {...controlProps}
                ref={nameRef}
                name="name"
                autoComplete="name"
                placeholder="이름을 입력하세요"
                required
                maxLength={80}
                onChange={(event) => {
                  if (errors.name) {
                    const error = nameError(event.currentTarget);
                    setErrors((current) => ({ ...current, name: error }));
                  }
                }}
              />
            )}
          </FormField>
          <FormField
            label="이메일"
            description="필수 입력입니다. name@example.com 형식으로 입력해 주세요."
            error={errors.email}
          >
            {(controlProps) => (
              <Input
                {...controlProps}
                ref={emailRef}
                name="email"
                type="email"
                autoComplete="email"
                placeholder="name@example.com"
                required
                onChange={(event) => {
                  if (errors.email) {
                    const error = emailError(event.currentTarget);
                    setErrors((current) => ({ ...current, email: error }));
                  }
                }}
              />
            )}
          </FormField>
          <FormField
            label="메모"
            description="선택 입력입니다. 최대 1,000자입니다."
          >
            {(controlProps) => (
              <Textarea
                {...controlProps}
                name="note"
                placeholder="내용을 입력하세요"
                maxLength={1000}
              />
            )}
          </FormField>
          <Button type="submit">입력값 확인</Button>
        </form>
      </SampleCard>
      <SampleCard
        title="선택과 전환"
        description="선택지의 수와 목적에 맞는 요소를 사용합니다."
      >
        <div className="space-y-6">
          <FormField
            label="상태 선택"
            description="표시할 상태를 선택하는 예시입니다."
          >
            {(controlProps) => (
              <Select
                defaultValue="all"
                items={[
                  { value: 'all', label: '모든 상태' },
                  { value: 'active', label: '진행 중' },
                  { value: 'done', label: '완료' },
                ]}
              >
                <SelectTrigger {...controlProps} className="w-full">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">모든 상태</SelectItem>
                  <SelectItem value="active">진행 중</SelectItem>
                  <SelectItem value="done">완료</SelectItem>
                </SelectContent>
              </Select>
            )}
          </FormField>
          <Separator />
          <div className="flex items-start gap-3">
            <Checkbox
              id="example-checkbox"
              aria-describedby="example-checkbox-description"
              defaultChecked
            />
            <div>
              <Label htmlFor="example-checkbox">안내 내용을 확인했습니다</Label>
              <p
                id="example-checkbox-description"
                className="mt-1 text-xs leading-6 text-muted-foreground"
              >
                독립적으로 선택하는 항목에 사용합니다.
              </p>
            </div>
          </div>
          <div className="flex items-center justify-between gap-4 rounded-lg border p-4">
            <div>
              <Label htmlFor="example-switch">알림 받기</Label>
              <p
                id="example-switch-description"
                className="mt-1 text-xs text-muted-foreground"
              >
                선택 요소의 동작 예시입니다.
              </p>
            </div>
            <Switch
              id="example-switch"
              aria-describedby="example-switch-description"
            />
          </div>
        </div>
      </SampleCard>
    </div>
  );
}
