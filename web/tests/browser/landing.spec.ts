import { expect, test } from '@playwright/test';

test('public introduction opens without an account and does not offer working authentication', async ({
  page,
}, testInfo) => {
  const writes: string[] = [];
  page.on('request', (request) => {
    if (!['GET', 'HEAD'].includes(request.method())) writes.push(request.url());
  });
  await page.goto('/');
  await expect(page).toHaveTitle('서비스 소개 · Etude');
  await expect(page.getByRole('heading', { level: 1 })).toContainText(
    '회사에 필요한 업무를,',
  );
  await expect(
    page.getByRole('navigation', { name: '서비스 안내' }),
  ).toBeVisible();
  await expect(page.getByRole('navigation', { name: '주 메뉴' })).toHaveCount(
    0,
  );
  await expect(page.getByRole('button', { name: /^화면 검색/ })).toHaveCount(0);
  await expect(
    page.getByRole('button', { name: '로그인', exact: true }),
  ).toBeDisabled();
  await expect(
    page.getByRole('button', { name: '회원가입 준비 중' }),
  ).toBeDisabled();
  await expect(page.locator('input')).toHaveCount(0);
  await page.reload();
  await expect(page.getByRole('heading', { level: 1 })).toContainText(
    '회사에 필요한 업무를,',
  );
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth,
    ),
  ).toBe(true);
  expect(writes).toEqual([]);
  await page.screenshot({
    path: testInfo.outputPath('landing-light.png'),
    fullPage: true,
  });
});

test('section links support keyboard navigation, direct fragments and browser history', async ({
  page,
}) => {
  await page.goto('/');
  const start = page.getByRole('link', { name: '이용 순서 살펴보기' });
  await start.focus();
  await start.press('Enter');
  await expect(page).toHaveURL(/\/#start$/);
  await expect(
    page.getByRole('heading', { name: '이렇게 이용하게 돼요' }),
  ).toBeFocused();
  await page
    .getByRole('navigation', { name: '서비스 안내' })
    .getByRole('link', { name: '확장 모듈' })
    .click();
  await expect(page.locator('#modules')).toBeFocused();
  await page.goBack();
  await expect(page.locator('#start')).toBeFocused();
  await page.goto('/#modules');
  await page.reload();
  await expect(page.locator('#modules')).toBeFocused();
  await page.getByRole('link', { name: 'Etude 소개', exact: true }).click();
  await expect(page).toHaveURL('/');
  await expect(page.locator('#main-content')).toBeFocused();
  expect(await page.evaluate(() => window.scrollY)).toBe(0);
});

test('questions explain the planned scope and current availability', async ({
  page,
}, testInfo) => {
  await page.emulateMedia({ colorScheme: 'dark' });
  await page.goto('/');
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  const question = page.getByRole('button', {
    name: '지금 가입해서 사용할 수 있나요?',
  });
  await question.focus();
  await question.press('Enter');
  await expect(question).toHaveAttribute('aria-expanded', 'true');
  await expect(
    page.getByText('현재는 서비스 소개 페이지를 먼저 공개한 단계예요.', {
      exact: false,
    }),
  ).toBeVisible();
  await question.press('Enter');
  await expect(question).toHaveAttribute('aria-expanded', 'false');
  await page.screenshot({
    path: testInfo.outputPath('landing-dark.png'),
    fullPage: true,
  });
});

test('a public page chunk failure keeps public navigation and reload recovery', async ({
  page,
}) => {
  const failedChunk = '**/assets/Landing-*.js';
  await page.route(failedChunk, (route) => route.abort('failed'));
  await page.goto('/');
  await expect(
    page.getByRole('heading', { name: '화면을 불러오지 못했습니다' }),
  ).toBeVisible();
  await expect(
    page.getByRole('navigation', { name: '서비스 안내' }),
  ).toBeVisible();
  await expect(page.getByRole('navigation', { name: '주 메뉴' })).toHaveCount(
    0,
  );
  await page.unroute(failedChunk);
  await page.getByRole('button', { name: '다시 불러오기' }).click();
  await expect(page.getByRole('heading', { level: 1 })).toContainText(
    '회사에 필요한 업무를,',
  );
});
