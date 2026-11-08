import AxeBuilder from '@axe-core/playwright';
import { expect, test, type Locator, type Page } from '@playwright/test';

async function selectFocusGuards(page: Page) {
  const { portal, owner } = await page
    .getByRole('listbox')
    .evaluate((listbox) => {
      const portal = listbox.closest('[data-base-ui-portal]');
      if (!portal?.id)
        throw new Error('Select must have an identifiable portal');
      return {
        portal: `#${CSS.escape(portal.id)}`,
        owner: `[aria-owns="${CSS.escape(portal.id)}"]`,
      };
    });
  const guard = '[data-base-ui-focus-guard][aria-hidden="true"]';
  const guards = page.locator(
    [
      `${portal} ${guard}[data-type="inside"]`,
      `${guard}[data-type="outside"]:has(+ ${owner})`,
      `${owner} + ${guard}[data-type="outside"]`,
    ].join(', '),
  );
  // Dependency changes must trigger a review of this narrowly scoped waiver.
  await expect(guards).toHaveCount(4);
  await expect(guards.locator('> *')).toHaveCount(0);
  return guards;
}

async function settleUI(page: Page) {
  await page.evaluate(() => document.fonts.ready);
  await page.evaluate(async () => {
    // Audit settled UI; decorative infinite animations do not block the scan.
    const animations = document
      .getAnimations()
      .filter(
        (animation) =>
          animation.effect?.getComputedTiming().iterations !== Infinity,
      );
    await Promise.all(
      animations.map((animation) => animation.finished.catch(() => undefined)),
    );
  });
}

async function expectAccessible(
  page: Page,
  state: string,
  allowedSelectGuards?: Locator,
) {
  await settleUI(page);
  const results = await new AxeBuilder({ page })
    .withTags(['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa'])
    .analyze();
  const violations = [];
  const waivedNodes = [];
  for (const violation of results.violations) {
    const nodes = [];
    for (const node of violation.nodes) {
      const [selector] = node.target;
      if (
        violation.id === 'aria-hidden-focus' &&
        allowedSelectGuards &&
        node.target.length === 1 &&
        typeof selector === 'string' &&
        (await page.locator(selector).and(allowedSelectGuards).count()) === 1
      ) {
        // Approved in #72; all other rules and elements remain enforced.
        waivedNodes.push(node);
      } else {
        nodes.push(node);
      }
    }
    if (nodes.length) violations.push({ ...violation, nodes });
  }
  if (waivedNodes.length) {
    await test.info().attach(`${state} focus-guard waiver`, {
      body: JSON.stringify(
        { waivedNodes, violations: results.violations },
        null,
        2,
      ),
      contentType: 'application/json',
    });
  }
  const summary = violations.map(({ id, impact, help, nodes }) => ({
    id,
    impact,
    help,
    nodes: nodes.map(({ target, failureSummary }) => ({
      target,
      failureSummary,
    })),
  }));
  expect(summary, state).toEqual([]);
}

for (const theme of ['light', 'dark'] as const) {
  test.describe(`accessibility in ${theme} theme`, () => {
    test.use({ colorScheme: theme });

    test('workspace, settings and not-found pages', async ({ page }) => {
      for (const path of ['/', '/settings', '/does-not-exist']) {
        await page.goto(path);
        await expect(page.getByRole('heading', { level: 1 })).toBeVisible();
        await expect(page.locator('html')).toHaveAttribute('data-theme', theme);
        await expectAccessible(page, `${theme} ${path}`);
      }
    });

    test('all UI guide categories', async ({ page }) => {
      await page.goto('/ui');
      for (const category of [
        '기본 요소',
        '입력 폼',
        '탐색과 목록',
        '상태와 피드백',
      ]) {
        await page.getByRole('tab', { name: category, exact: true }).click();
        await expect(
          page.getByRole('tab', { name: category, exact: true }),
        ).toHaveAttribute('aria-selected', 'true');
        await expectAccessible(page, `${theme} ${category}`);
      }
    });

    test('dialogs, notifications, select, dropdown and responsive navigation', async ({
      page,
    }, testInfo) => {
      await page.goto('/ui');
      await page.getByRole('button', { name: /^화면 검색/ }).click();
      await expect(
        page.getByRole('dialog', { name: '화면 검색' }),
      ).toBeVisible();
      await expectAccessible(page, `${theme} screen search`);
      await page.keyboard.press('Escape');
      await expect(page.locator('[data-slot="dialog-content"]')).toHaveCount(0);

      await page.getByRole('button', { name: '대화상자 열기' }).click();
      await expect(
        page.getByRole('dialog', { name: '대화상자 미리보기' }),
      ).toBeVisible();
      await expectAccessible(page, `${theme} dialog`);
      await page.keyboard.press('Escape');
      await expect(page.locator('[data-slot="dialog-content"]')).toHaveCount(0);

      await page.getByRole('button', { name: '알림 확인' }).click();
      await expect(
        page.getByText('알림 미리보기', { exact: true }),
      ).toBeVisible();
      // F6 enters Base UI's toast viewport and pauses automatic dismissal.
      await page.keyboard.press('F6');
      const closeToast = page.getByRole('button', { name: '알림 닫기' });
      await closeToast.focus();
      await expect(closeToast).toBeFocused();
      await expectAccessible(page, `${theme} notification`);
      await closeToast.press('Enter');
      await expect(page.locator('[data-slot="toast"]')).toHaveCount(0);

      await page.getByRole('tab', { name: '입력 폼', exact: true }).click();
      await page
        .getByRole('combobox', { name: '상태 선택' })
        .press('ArrowDown');
      await expect(
        page.getByRole('option', { name: '모든 상태' }),
      ).toBeVisible();
      await expectAccessible(
        page,
        `${theme} select`,
        await selectFocusGuards(page),
      );
      await page.keyboard.press('Escape');

      await page.getByRole('tab', { name: '탐색과 목록', exact: true }).click();
      await page
        .getByRole('button', { name: '추가 작업', exact: true })
        .press('Enter');
      await expect(
        page.getByRole('menuitem', { name: '이름순 정렬' }),
      ).toBeVisible();
      await expectAccessible(page, `${theme} dropdown`);
      await page.keyboard.press('Escape');

      if (testInfo.project.name === 'mobile') {
        await page.getByRole('button', { name: '메뉴 열기' }).press('Enter');
        await expect(
          page.getByRole('dialog', { name: 'Etude', exact: true }),
        ).toBeVisible();
        await expectAccessible(page, `${theme} mobile navigation`);
      } else {
        await page.getByRole('button', { name: '사이드바 접기' }).click();
        await expectAccessible(page, `${theme} collapsed navigation`);
      }
    });

    test('select guards release focus and keyboard navigation preserves focus', async ({
      page,
    }) => {
      for (let index = 0; index < 4; index++) {
        // Each guard can close the popup, so start from a fresh page each time.
        await page.goto('/ui');
        await page.getByRole('tab', { name: '입력 폼', exact: true }).click();
        await page
          .getByRole('combobox', { name: '상태 선택' })
          .press('ArrowDown');
        await expect(page.getByRole('listbox')).toBeVisible();
        await expect(
          page.getByRole('option', { name: '모든 상태', exact: true }),
        ).toBeFocused();
        await settleUI(page);
        const guard = (await selectFocusGuards(page)).nth(index);
        await expect(guard).toHaveAttribute('tabindex', '0');
        await guard.focus();
        await page.evaluate(
          () =>
            new Promise((resolve) =>
              requestAnimationFrame(() => requestAnimationFrame(resolve)),
            ),
        );
        await expect
          .poll(
            () =>
              page.evaluate(() => {
                const active = document.activeElement;
                return (
                  active instanceof HTMLElement &&
                  !active.closest(
                    '[data-base-ui-focus-guard], [aria-hidden="true"], [inert]',
                  ) &&
                  active.checkVisibility()
                );
              }),
            {
              timeout: 1000,
              message: `Select guard ${index} must redirect focus`,
            },
          )
          .toBe(true);
      }
      await page.goto('/ui');
      await page.getByRole('tab', { name: '입력 폼', exact: true }).click();
      const trigger = page.getByRole('combobox', { name: '상태 선택' });
      await trigger.press('ArrowDown');
      await expect(
        page.getByRole('option', { name: '모든 상태', exact: true }),
      ).toBeFocused();
      await page.keyboard.press('Tab');
      await expect(
        page.getByRole('checkbox', { name: '안내 내용을 확인했습니다' }),
      ).toBeFocused();
      await expect(page.getByRole('listbox')).toHaveCount(0);
      await trigger.press('ArrowDown');
      await expect(
        page.getByRole('option', { name: '모든 상태', exact: true }),
      ).toBeFocused();
      await page.keyboard.press('Shift+Tab');
      await expect(trigger).toBeFocused();
      await expect(page.getByRole('listbox')).toHaveCount(0);
    });
  });
}
