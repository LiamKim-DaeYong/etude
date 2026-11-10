import { expect, test } from '@playwright/test';

const home = '/preview';
const board = '/preview/boards/general';
const post = `${board}/posts/welcome`;
const title = '함께 쓰는 게시판을 시작합니다';

test('explicit preview entry, sample labels and removal of demo routes', async ({
  page,
}) => {
  const apiRequests: string[] = [];
  page.on('request', (request) => {
    if (request.method() !== 'GET' || /\/(admin|api)\/v1\//.test(request.url()))
      apiRequests.push(request.url());
  });
  await page.goto('/');
  await expect(page).toHaveURL(/\/preview$/);
  await expect(
    page.getByRole('heading', { name: '홈', exact: true }),
  ).toBeVisible();
  await expect(
    page.getByRole('region', { name: '화면 검토 안내' }),
  ).toContainText(
    '회사·사용자·글은 예시이며 로그인·서버 저장은 연결되지 않았습니다.',
  );
  await expect(
    page.getByRole('button', { name: '샘플 글', exact: true }),
  ).toHaveAttribute('aria-pressed', 'true');
  await expect(page.locator('input[type="password"]')).toHaveCount(0);
  await expect(
    page.getByRole('button', { name: /로그인|글쓰기|게시판 추가/ }),
  ).toHaveCount(0);
  await expect(page.getByRole('button', { name: /^화면 검색/ })).toHaveCount(0);
  for (const path of [
    '/ui',
    '/settings',
    '/workspace',
    '/process',
    '/products',
  ]) {
    await page.goto(path);
    await expect(
      page.getByRole('heading', { name: '페이지를 찾을 수 없습니다' }),
    ).toBeVisible();
  }
  await page.getByRole('link', { name: '홈으로 돌아가기' }).click();
  await expect(page).toHaveURL(/\/preview$/);
  expect(apiRequests).toEqual([]);
});

test('home to board to post works with direct reload, back and recovery', async ({
  page,
}, testInfo) => {
  await page.goto(home);
  await expect(
    page.getByRole('heading', { name: '최근 게시글' }),
  ).toBeVisible();
  await page.screenshot({
    path: testInfo.outputPath('home-light.png'),
    fullPage: true,
  });
  await expect(
    page.getByRole('group', { name: '회사와 사용자' }),
  ).toBeVisible();
  await expect(page.getByText('샘플 회사', { exact: true })).toHaveCount(1);
  await expect(
    page.getByRole('link', { name: new RegExp(`^${title}`) }),
  ).toBeInViewport({ ratio: 1 });
  await page.getByRole('link', { name: '게시판 전체 보기' }).click();
  await expect(page).toHaveURL(new RegExp(`${board}$`));
  await expect(
    page.getByRole('heading', { level: 1, name: '게시판', exact: true }),
  ).toBeVisible();
  await expect(page).toHaveTitle('게시판 · Etude');
  await page.screenshot({
    path: testInfo.outputPath('board-light.png'),
    fullPage: true,
  });
  await page
    .getByRole('link', { name: new RegExp(`^${title}`) })
    .press('Enter');
  await expect(
    page.getByRole('heading', { level: 1, name: title }),
  ).toBeVisible();
  await expect(page.locator('#main-content')).toBeFocused();
  await expect(page).toHaveTitle('글 상세 · Etude');
  await page.reload();
  await expect(page.getByRole('heading', { name: title })).toBeVisible();
  await expect(page.locator('article')).toContainText(
    '실제 회사의 공지나 저장된 업무 기록이 아닙니다.',
  );
  await page.screenshot({
    path: testInfo.outputPath('post-light.png'),
    fullPage: true,
  });
  await page
    .getByRole('link', { name: '목록으로 돌아가기', exact: true })
    .click();
  await expect(
    page.getByRole('heading', { level: 1, name: '게시판', exact: true }),
  ).toBeVisible();
  await page.goBack();
  await expect(page.getByRole('heading', { name: title })).toBeVisible();
  for (const path of ['/preview/boards/missing', `${board}/posts/missing`]) {
    await page.goto(path);
    await expect(
      page.getByRole('heading', { level: 1, name: /찾을 수 없습니다/ }),
    ).toBeVisible();
    await page.getByRole('link', { name: '게시판 목록으로' }).click();
    await expect(page).toHaveURL(new RegExp(`${board}$`));
  }
});

test('empty example survives menu navigation, deep links and browser history', async ({
  page,
}, testInfo) => {
  await page.goto(home);
  await page
    .getByRole('button', { name: '글 없음', exact: true })
    .press('Enter');
  await expect(page).toHaveURL(/\/preview\?data=empty$/);
  await expect(
    page.getByRole('heading', { name: '아직 게시글이 없어요' }),
  ).toBeVisible();
  await expect(page.getByRole('link', { name: new RegExp(title) })).toHaveCount(
    0,
  );
  await page.screenshot({
    path: testInfo.outputPath('home-empty.png'),
    fullPage: true,
  });
  await page.getByRole('link', { name: '게시판 전체 보기' }).click();
  await expect(page).toHaveURL(/\/boards\/general\?data=empty$/);
  await page.reload();
  await expect(
    page.getByRole('heading', { name: '아직 게시글이 없어요' }),
  ).toBeVisible();
  await expect(
    page.getByRole('button', { name: '글 없음', exact: true }),
  ).toHaveAttribute('aria-pressed', 'true');
  await page.screenshot({
    path: testInfo.outputPath('board-empty.png'),
    fullPage: true,
  });
  await page
    .getByRole('button', { name: '샘플 글', exact: true })
    .press('Enter');
  await expect(
    page.getByRole('link', { name: new RegExp(title) }),
  ).toBeVisible();
  await page.goBack();
  await expect(
    page.getByRole('heading', { name: '아직 게시글이 없어요' }),
  ).toBeVisible();
});

test('navigation has only home and board and keeps keyboard focus', async ({
  page,
}, testInfo) => {
  await page.goto(home);
  await expect(
    page.getByRole('heading', { name: '홈', exact: true }),
  ).toBeVisible();
  if (testInfo.project.name === 'mobile') {
    const trigger = page.getByRole('button', { name: '메뉴 열기' });
    await trigger.press('Enter');
    const drawer = page.getByRole('dialog', { name: '메뉴', exact: true });
    await expect(drawer).toBeVisible();
    await expect(
      drawer.getByRole('navigation', { name: '주 메뉴' }).getByRole('link'),
    ).toHaveCount(2);
    await page.keyboard.press('Escape');
    await expect(trigger).toBeFocused();
    await trigger.press('Enter');
    await drawer
      .getByRole('link', { name: '게시판', exact: true })
      .press('Enter');
    await expect(drawer).not.toBeVisible();
  } else {
    await page.getByRole('button', { name: '사이드바 접기' }).click();
    await expect(
      page.getByRole('link', { name: '홈', exact: true }),
    ).toBeVisible();
    await page.getByRole('button', { name: '사이드바 펼치기' }).click();
    const navigation = page.getByRole('navigation', { name: '주 메뉴' });
    await expect(navigation.getByRole('link')).toHaveCount(2);
    await navigation
      .getByRole('link', { name: '게시판', exact: true })
      .press('Enter');
  }
  await expect(
    page.getByRole('heading', { level: 1, name: '게시판', exact: true }),
  ).toBeVisible();
  await expect(page.locator('#main-content')).toBeFocused();
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= innerWidth,
    ),
  ).toBe(true);
  await page.goto(post);
  if (testInfo.project.name === 'desktop')
    await expect(
      page
        .getByRole('navigation', { name: '주 메뉴' })
        .getByRole('link', { name: '게시판', exact: true }),
    ).toHaveAttribute('aria-current', 'page');
});

test('theme selection persists and system mode follows device preferences', async ({
  page,
}, testInfo) => {
  await page.goto(home);
  const trigger = page.getByRole('button', { name: '화면 테마 선택' });
  await trigger.press('Enter');
  await page.getByRole('menuitemradio', { name: '다크', exact: true }).click();
  await expect(page.getByRole('menu')).not.toBeVisible();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  await page.reload();
  await expect(
    page.getByRole('heading', { level: 1, name: '홈', exact: true }),
  ).toBeVisible();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  await page.screenshot({
    path: testInfo.outputPath('home-dark.png'),
    fullPage: true,
  });
  await trigger.press('Enter');
  await expect(
    page.getByRole('menuitemradio', { name: '다크', exact: true }),
  ).toBeChecked();
  await page
    .getByRole('menuitemradio', { name: '시스템 설정', exact: true })
    .click();
  await page.emulateMedia({ colorScheme: 'light' });
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'light');
  await page.emulateMedia({ colorScheme: 'dark' });
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
});

test('production screens and overlays work under CSP without runtime errors', async ({
  page,
}) => {
  const failures: string[] = [];
  page.on('pageerror', (error) => failures.push(error.message));
  page.on('console', (message) => {
    if (message.type() === 'error') failures.push(message.text());
  });
  await page.addInitScript(() =>
    document.addEventListener('securitypolicyviolation', (event) =>
      console.error(
        `CSP violation: ${event.violatedDirective} ${event.blockedURI}`,
      ),
    ),
  );
  for (const path of [
    home,
    board,
    post,
    `${home}?data=empty`,
    `${board}?data=empty`,
    '/missing',
  ]) {
    await page.goto(path);
    await expect(page.getByRole('heading', { level: 1 })).not.toHaveText(
      '화면 불러오는 중',
    );
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible();
  }
  await page.getByRole('button', { name: '화면 테마 선택' }).click();
  await page.getByRole('menuitemradio', { name: '다크', exact: true }).click();
  await expect(page.getByRole('menu')).not.toBeVisible();
  const menu = page.getByRole('button', { name: '메뉴 열기' });
  if (await menu.isVisible()) {
    await menu.click();
    await expect(page.getByRole('dialog')).toBeVisible();
    await page.keyboard.press('Escape');
  }
  expect(failures).toEqual([]);
});

test('production headers block inline script execution', async ({
  page,
  request,
}) => {
  const response = await request.get(home);
  expect(response.headers()['content-security-policy']).toContain(
    "script-src 'self'",
  );
  expect(response.headers()['content-security-policy']).toContain(
    "frame-ancestors 'none'",
  );
  expect(response.headers()['x-content-type-options']).toBe('nosniff');
  await page.goto(home);
  const blocked = await page.evaluate(
    () =>
      new Promise<boolean>((resolve) => {
        document.addEventListener(
          'securitypolicyviolation',
          (event) => {
            if (event.effectiveDirective === 'script-src-elem') resolve(true);
          },
          { once: true },
        );
        const script = document.createElement('script');
        script.textContent =
          'document.documentElement.dataset.inlineExecuted = "yes"';
        document.head.append(script);
      }),
  );
  expect(blocked).toBe(true);
  await expect(page.locator('html')).not.toHaveAttribute(
    'data-inline-executed',
    'yes',
  );
});

test('skip link and self-hosted Korean font remain available', async ({
  page,
}) => {
  await page.goto(home);
  await expect(
    page.getByRole('heading', { name: '홈', exact: true }),
  ).toBeVisible();
  await page.keyboard.press('Tab');
  await expect(
    page.getByRole('link', { name: '본문으로 건너뛰기' }),
  ).toBeFocused();
  await page.keyboard.press('Enter');
  await expect(page.locator('#main-content')).toBeFocused();
  await page.evaluate(() => document.fonts.ready);
  expect(
    await page.evaluate(() =>
      [...document.fonts].some(
        (font) =>
          font.family.includes('Pretendard') && font.status === 'loaded',
      ),
    ),
  ).toBe(true);
  await expect(page.getByRole('heading', { level: 1 })).toHaveCSS(
    'font-family',
    /Pretendard/,
  );
  const fonts = await page.evaluate(() =>
    performance
      .getEntriesByType('resource')
      .map((entry) => entry.name)
      .filter((name) => name.includes('.woff2')),
  );
  expect(fonts.length).toBeGreaterThan(0);
  for (const url of fonts)
    expect(new URL(url).origin).toBe('http://127.0.0.1:4173');
});
