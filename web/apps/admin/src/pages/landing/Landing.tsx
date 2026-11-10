import { useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { ArrowDown, ArrowRight } from 'lucide-react';
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
  Badge,
  Button,
  Card,
  CardContent,
  buttonVariants,
} from '@etude/ui';

const basics = [
  {
    title: '회사 기본 정보',
    description:
      '회사 이름과 국가, 업무에 사용할 시간대 등 회사의 기본 환경을 설정하는 흐름이에요.',
  },
  {
    title: '직원 ID와 권한',
    description:
      '관리자가 직원 ID를 발급하고, 직원별로 사용할 기능과 업무 범위를 정하는 방식이에요.',
  },
  {
    title: '필요한 기능 선택',
    description:
      '회사에 필요한 업무부터 구성해요. 거래처나 상품 관리가 모든 회사에 필수인 것은 아니에요.',
  },
];

const steps = [
  {
    title: '대표가 회원가입해요',
    description: '회사와 대표 계정을 함께 만들고, 회사의 업무 공간을 시작해요.',
  },
  {
    title: '회사 기본 정보를 설정해요',
    description: '회사의 기본 정보와 업무 환경을 정리해요.',
  },
  {
    title: '직원 ID와 권한을 정해요',
    description:
      '관리자가 직원 ID를 발급하고, 맡은 업무에 필요한 권한을 부여해요.',
  },
  {
    title: '필요한 기능을 더해요',
    description:
      '기본 공간에서 시작하고, 회사에 필요한 모듈을 구독해 업무를 넓혀 가요.',
  },
];

const modules = [
  {
    title: '홈페이지',
    description: '회사 소개와 제품·서비스 정보를 외부에 알리는 공간',
  },
  { title: '물류', description: '입고와 출고, 재고의 흐름을 확인하는 업무' },
  {
    title: '제조',
    description: '생산 계획과 현장의 작업 진행을 기록하는 업무',
  },
  {
    title: '회계',
    description: '회사의 수입·지출과 회계 자료를 정리하는 업무',
  },
  {
    title: '쇼핑몰',
    description: '상품을 소개하고 온라인 주문을 받는 판매 공간',
  },
];

export default function Landing() {
  const { hash } = useLocation();
  useEffect(() => {
    // Run after the lazy page mounts, including direct fragment navigation.
    const target = document.getElementById(hash.slice(1));
    if (target) {
      target.scrollIntoView();
      target.focus({ preventScroll: true });
    }
  }, [hash]);

  return (
    <>
      <div className="mt-6 flex flex-wrap items-center gap-3 rounded-lg border bg-muted/40 px-4 py-3 text-sm">
        <Badge variant="secondary">서비스 준비 중</Badge>
        <p>
          아래 내용은 예정된 이용 방식이에요. 현재는 서비스 소개만 확인할 수
          있어요.
        </p>
      </div>
      <section aria-labelledby="landing-title" className="py-14 sm:py-20">
        <p className="mb-4 text-sm font-medium text-muted-foreground">
          중소기업을 위한 업무 공간
        </p>
        <h1
          id="landing-title"
          className="max-w-3xl text-3xl leading-tight font-semibold tracking-tight sm:text-5xl sm:leading-tight"
        >
          회사에 필요한 업무를,
          <br />
          하나씩 연결하세요.
        </h1>
        <p className="mt-6 max-w-2xl text-base leading-relaxed text-muted-foreground sm:text-lg">
          회사 기본 정보와 직원 계정부터 정리하고, 필요한 업무 모듈을 단계적으로
          더하는 서비스를 준비하고 있어요.
        </p>
        <div className="mt-8 flex flex-wrap gap-3">
          <Link to="#start" className={buttonVariants({ size: 'lg' })}>
            이용 순서 살펴보기 <ArrowDown aria-hidden="true" />
          </Link>
          <Link
            to="#modules"
            className={buttonVariants({ variant: 'outline', size: 'lg' })}
          >
            모듈 구성 보기 <ArrowRight aria-hidden="true" />
          </Link>
        </div>
      </section>

      <section aria-labelledby="overview" className="border-t py-12 sm:py-16">
        <h2
          id="overview"
          tabIndex={-1}
          className="scroll-mt-6 text-2xl font-semibold tracking-tight"
        >
          회사 운영의 기본부터 시작해요
        </h2>
        <p className="mt-3 max-w-2xl leading-relaxed text-muted-foreground">
          업종이나 규모에 맞춰 필요한 기능을 고르는 구성이에요. 처음부터 모든
          모듈을 도입할 필요는 없어요.
        </p>
        <div className="mt-8 grid gap-4 md:grid-cols-3">
          {basics.map(({ title, description }) => (
            <Card key={title}>
              <CardContent className="space-y-3">
                <h3 className="font-semibold">{title}</h3>
                <p className="text-sm leading-relaxed text-muted-foreground">
                  {description}
                </p>
              </CardContent>
            </Card>
          ))}
        </div>
      </section>

      <section aria-labelledby="start" className="border-t py-12 sm:py-16">
        <h2
          id="start"
          tabIndex={-1}
          className="scroll-mt-6 text-2xl font-semibold tracking-tight"
        >
          이렇게 이용하게 돼요
        </h2>
        <p className="mt-3 text-muted-foreground">
          회사 가입부터 직원의 업무 시작까지, 준비하고 있는 순서예요.
        </p>
        <ol className="mt-8 grid gap-6 sm:grid-cols-2">
          {steps.map(({ title, description }, index) => (
            <li key={title} className="flex items-start gap-4">
              <span
                aria-hidden="true"
                className="grid size-9 shrink-0 place-items-center rounded-full border bg-muted text-sm font-medium"
              >
                {index + 1}
              </span>
              <div>
                <h3 className="font-semibold">{title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-muted-foreground">
                  {description}
                </p>
              </div>
            </li>
          ))}
        </ol>
        <p className="mt-8 rounded-lg border px-4 py-3 text-sm leading-relaxed text-muted-foreground">
          모듈 구독은 회사에서 사용할 기능을 정하고, 직원 권한은 각 직원이 할 수
          있는 일을 정해요.
        </p>
      </section>

      <section aria-labelledby="modules" className="border-t py-12 sm:py-16">
        <h2
          id="modules"
          tabIndex={-1}
          className="scroll-mt-6 text-2xl font-semibold tracking-tight"
        >
          필요해질 때 더하는 업무 모듈
        </h2>
        <p className="mt-3 max-w-2xl leading-relaxed text-muted-foreground">
          기본 업무 공간에 연결할 확장 모듈을 구상하고 있어요. 제공 범위와 가격,
          개발 순서는 아직 정해지지 않았어요.
        </p>
        <ul className="mt-8 divide-y rounded-lg border">
          {modules.map(({ title, description }) => (
            <li
              key={title}
              className="flex flex-wrap items-start justify-between gap-3 p-5"
            >
              <div>
                <h3 className="font-semibold">{title}</h3>
                <p className="mt-2 text-sm leading-relaxed text-muted-foreground">
                  {description}
                </p>
              </div>
              <Badge variant="outline">계획 중</Badge>
            </li>
          ))}
        </ul>
      </section>

      <section aria-labelledby="questions" className="border-t py-12 sm:py-16">
        <h2 id="questions" className="text-2xl font-semibold tracking-tight">
          궁금한 점을 확인하세요
        </h2>
        <Accordion className="mt-5">
          <AccordionItem value="business">
            <AccordionTrigger>
              공장이나 제조업만 이용할 수 있나요?
            </AccordionTrigger>
            <AccordionContent>
              여러 중소기업이 회사에 맞게 구성하는 서비스를 목표로 하고 있어요.
              업종별로 필요한 업무는 설정과 확장 모듈로 더하는 방향이에요.
            </AccordionContent>
          </AccordionItem>
          <AccordionItem value="required">
            <AccordionTrigger>
              거래처·상품 관리를 꼭 사용해야 하나요?
            </AccordionTrigger>
            <AccordionContent>
              모든 회사에 필수로 요구하는 방향은 아니에요. 회사에 필요한 기능을
              선택하고, 필요해질 때 확장하는 구성을 준비하고 있어요.
            </AccordionContent>
          </AccordionItem>
          <AccordionItem value="access">
            <AccordionTrigger>지금 가입해서 사용할 수 있나요?</AccordionTrigger>
            <AccordionContent>
              현재는 서비스 소개 페이지를 먼저 공개한 단계예요. 이 화면에서는
              회원가입이나 로그인, 모듈 구독을 진행할 수 없어요. 이용 시작
              일정과 요금은 추후 안내할 예정이에요.
            </AccordionContent>
          </AccordionItem>
        </Accordion>
      </section>

      <section
        aria-labelledby="access-title"
        className="rounded-xl border bg-muted/40 p-6 sm:p-8"
      >
        <h2 id="access-title" className="text-xl font-semibold">
          이용 시작 안내
        </h2>
        <p
          id="signup-status"
          className="mt-3 max-w-2xl text-sm leading-relaxed text-muted-foreground"
        >
          회원가입과 로그인은 준비 중이에요. 가입이 열리면 대표가 회사를
          등록하고, 직원 ID와 권한을 직접 관리하는 방식으로 시작할 수 있어요.
        </p>
        <Button className="mt-5" disabled aria-describedby="signup-status">
          회원가입 준비 중
        </Button>
      </section>
    </>
  );
}
