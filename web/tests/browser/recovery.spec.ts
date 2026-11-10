import { expect, test } from '@playwright/test';

test('failed board chunk keeps the shell and reload recovers it', async ({
  page,
}, testInfo) => {
  const failedChunk = '**/assets/Board-*.js';
  await page.route(failedChunk, (route) => route.abort('failed'));
  await page.goto('/preview/boards/general');
  await expect(
    page.getByRole('heading', { name: '화면을 불러오지 못했습니다' }),
  ).toBeVisible();
  await expect(
    page.getByRole('region', { name: '화면 검토 안내' }),
  ).toBeVisible();
  await page.screenshot({
    path: testInfo.outputPath('page-error.png'),
    fullPage: true,
  });
  await page.getByRole('link', { name: '홈으로 이동' }).press('Enter');
  await expect(
    page.getByRole('heading', { name: '홈', exact: true }),
  ).toBeVisible();
  await expect(page.locator('#main-content')).toBeFocused();
  await page.getByRole('link', { name: '게시판 전체 보기' }).click();
  await expect(
    page.getByRole('heading', { name: '화면을 불러오지 못했습니다' }),
  ).toBeVisible();
  await page.unroute(failedChunk);
  await page.getByRole('button', { name: '다시 불러오기' }).click();
  await expect(
    page.getByRole('heading', { level: 1, name: '게시판', exact: true }),
  ).toBeVisible();
});

test('loading state honors reduced motion before the actual screen appears', async ({
  page,
}, testInfo) => {
  const chunk = '**/assets/Board-*.js';
  let release!: () => void;
  const gate = new Promise<void>((resolve) => {
    release = resolve;
  });
  await page.route(chunk, async (route) => {
    await gate;
    await route.continue();
  });
  await page.emulateMedia({ reducedMotion: 'reduce' });
  try {
    await page.goto('/preview/boards/general', {
      waitUntil: 'domcontentloaded',
    });
    await expect(
      page.getByRole('heading', { level: 1, name: '화면 불러오는 중' }),
    ).toBeVisible();
    await expect(page.getByRole('status')).toContainText(
      '잠시만 기다려 주세요.',
    );
    for (const skeleton of await page.locator('[data-slot="skeleton"]').all())
      await expect(skeleton).toHaveCSS('animation-name', 'none');
    await page.screenshot({
      path: testInfo.outputPath('page-loading.png'),
      fullPage: true,
    });
  } finally {
    release();
  }
  await expect(
    page.getByRole('heading', { level: 1, name: '게시판', exact: true }),
  ).toBeVisible();
});

test('provider failure has recovery without providers or raw exception text', async ({
  page,
}, testInfo) => {
  await page.addInitScript(() => {
    const matchMedia = window.matchMedia.bind(window);
    window.matchMedia = (query) => {
      if (document.getElementById('root'))
        throw new Error('Simulated provider failure');
      return matchMedia(query);
    };
  });
  await page.goto('/preview');
  await expect(
    page.getByRole('heading', { name: '앱을 불러오지 못했습니다' }),
  ).toBeVisible();
  await expect(page.getByText('Simulated provider failure')).toHaveCount(0);
  await page.screenshot({
    path: testInfo.outputPath('root-error.png'),
    fullPage: true,
  });
  await page.keyboard.press('Tab');
  await expect(
    page.getByRole('button', { name: '다시 불러오기' }),
  ).toBeFocused();
});
