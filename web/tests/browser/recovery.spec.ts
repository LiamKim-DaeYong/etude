import { expect, test } from '@playwright/test';

test('a failed page chunk keeps navigation available and reload recovers it', async ({
  page,
}, testInfo) => {
  const failedChunk = '**/assets/Settings-*.js';
  await page.route(failedChunk, (route) => route.abort('failed'));
  await page.goto('/settings');
  await expect(
    page.getByRole('heading', { name: '화면을 불러오지 못했습니다' }),
  ).toBeVisible();
  await expect(page.getByRole('button', { name: /^화면 검색/ })).toBeVisible();
  await page.screenshot({
    path: testInfo.outputPath('page-error.png'),
    fullPage: true,
  });

  await page.keyboard.press('Control+k');
  await page
    .getByRole('combobox', { name: '화면 이름 검색' })
    .fill('UI 가이드');
  await page.keyboard.press('Enter');
  await expect(
    page.getByRole('heading', { level: 1, name: 'UI 가이드' }),
  ).toBeVisible();
  await expect(page.locator('#main-content')).toBeFocused();

  await page.keyboard.press('Control+k');
  await page
    .getByRole('combobox', { name: '화면 이름 검색' })
    .fill('화면 설정');
  await page.keyboard.press('Enter');
  await expect(
    page.getByRole('heading', { name: '화면을 불러오지 못했습니다' }),
  ).toBeVisible();
  await page.getByRole('link', { name: '워크스페이스로 이동' }).click();
  await expect(
    page.getByRole('heading', { level: 1, name: 'Etude 작업 공간' }),
  ).toBeVisible();

  await page.goto('/settings');
  await expect(
    page.getByRole('heading', { name: '화면을 불러오지 못했습니다' }),
  ).toBeVisible();
  await page.unroute(failedChunk);
  await page.getByRole('button', { name: '다시 불러오기' }).click();
  await expect(
    page.getByRole('heading', { level: 1, name: '화면 설정' }),
  ).toBeVisible();
  await expect(
    page.getByRole('radio', { name: '시스템 설정', exact: true }),
  ).toBeChecked();
});

test('a provider render failure shows a fallback without UI providers', async ({
  page,
}, testInfo) => {
  await page.addInitScript(() => {
    const matchMedia = window.matchMedia.bind(window);
    window.matchMedia = (query) => {
      // Bootstrap runs in the head; fail only after the React root exists.
      if (document.getElementById('root'))
        throw new Error('Simulated provider failure');
      return matchMedia(query);
    };
  });
  await page.goto('/');
  await expect(
    page.getByRole('heading', { level: 1, name: '앱을 불러오지 못했습니다' }),
  ).toBeVisible();
  await expect(
    page.getByRole('button', { name: '다시 불러오기' }),
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
