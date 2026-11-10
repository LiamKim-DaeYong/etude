import AxeBuilder from '@axe-core/playwright';
import { expect, test, type Page } from '@playwright/test';

async function settleUI(page: Page) {
  await page.evaluate(async () => {
    await document.fonts.ready;
    await Promise.all(
      document
        .getAnimations()
        .filter(
          (animation) => animation.effect?.getTiming().iterations !== Infinity,
        )
        .map((animation) => animation.finished.catch(() => undefined)),
    );
  });
}

async function expectAccessible(page: Page, state: string) {
  await expect(page.getByRole('heading', { level: 1 })).toBeVisible();
  await expect(page.getByRole('heading', { level: 1 })).not.toHaveText(
    '화면 불러오는 중',
  );
  await settleUI(page);
  const results = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa'])
    .analyze();
  await test.info().attach(state, {
    body: JSON.stringify(results.violations, null, 2),
    contentType: 'application/json',
  });
  // The preview has no Select. No rules or elements are excluded.
  expect(results.violations, state).toEqual([]);
}

for (const theme of ['light', 'dark'] as const) {
  test.describe(`preview accessibility in ${theme}`, () => {
    test.use({ colorScheme: theme });

    test('home, board and detail', async ({ page }) => {
      for (const path of [
        '/preview',
        '/preview/boards/general',
        '/preview/boards/general/posts/welcome',
      ]) {
        await page.goto(path);
        await expect(page.locator('html')).toHaveAttribute('data-theme', theme);
        await expectAccessible(page, `${theme} ${path}`);
      }
    });

    test('empty screens, missing board, missing post and unknown route', async ({
      page,
    }) => {
      for (const path of [
        '/preview?data=empty',
        '/preview/boards/general?data=empty',
        '/preview/boards/missing',
        '/preview/boards/general/posts/missing',
        '/missing',
      ]) {
        await page.goto(path);
        await expectAccessible(page, `${theme} ${path}`);
      }
    });

    test('theme menu and responsive navigation', async ({ page }, testInfo) => {
      await page.goto('/preview');
      const themeTrigger = page.getByRole('button', { name: '화면 테마 선택' });
      await themeTrigger.press('Enter');
      await expect(
        page.getByRole('menuitemradio', { name: '시스템 설정', exact: true }),
      ).toBeVisible();
      await expectAccessible(page, `${theme} theme menu`);
      await page.keyboard.press('Escape');
      await expect(themeTrigger).toBeFocused();
      if (testInfo.project.name === 'mobile') {
        const trigger = page.getByRole('button', { name: '메뉴 열기' });
        await trigger.press('Enter');
        await expect(
          page.getByRole('dialog', { name: '메뉴', exact: true }),
        ).toBeVisible();
        await settleUI(page);
        const results = await new AxeBuilder({ page })
          .withTags(['wcag2a', 'wcag2aa', 'wcag21aa', 'wcag22aa'])
          .analyze();
        expect(results.violations).toEqual([]);
        await page.keyboard.press('Tab');
        await page.keyboard.press('Shift+Tab');
        await page.keyboard.press('Escape');
        await expect(trigger).toBeFocused();
      } else {
        await page
          .getByRole('button', { name: '사이드바 접기' })
          .press('Enter');
        await expectAccessible(page, `${theme} collapsed sidebar`);
      }
    });
  });
}
