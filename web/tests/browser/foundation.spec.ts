import { expect, test } from '@playwright/test';

test('routes survive direct navigation and unknown routes provide a way home', async ({
  page,
}) => {
  await page.goto('/settings');
  await expect(
    page.getByRole('heading', { level: 1, name: '화면 설정' }),
  ).toBeVisible();
  await expect(page).toHaveTitle('화면 설정 · Etude');
  await page.reload();
  await expect(
    page.getByRole('radio', { name: '시스템 설정', exact: true }),
  ).toBeChecked();
  await page.goto('/does-not-exist');
  await expect(
    page.getByRole('heading', { name: '페이지를 찾을 수 없습니다' }),
  ).toBeVisible();
  await page.getByRole('link', { name: '워크스페이스로 돌아가기' }).click();
  await expect(
    page.getByRole('heading', { level: 1, name: 'Etude 작업 공간' }),
  ).toBeVisible();
});

test('theme choice persists and system mode follows device preferences', async ({
  page,
}, testInfo) => {
  await page.goto('/settings');
  await page.getByRole('radio', { name: '다크', exact: true }).check();
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  await page.reload();
  await expect(
    page.getByRole('radio', { name: '다크', exact: true }),
  ).toBeChecked();
  await page.getByRole('radio', { name: '시스템 설정', exact: true }).check();
  await page.emulateMedia({ colorScheme: 'light' });
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'light');
  await page.emulateMedia({ colorScheme: 'dark' });
  await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');
  await page.screenshot({
    path: testInfo.outputPath('settings-dark.png'),
    fullPage: true,
  });
});

test('navigation works at the project viewport without horizontal overflow', async ({
  page,
}, testInfo) => {
  await page.goto('/');
  await expect(
    page.getByRole('heading', { level: 1, name: 'Etude 작업 공간' }),
  ).toBeVisible();
  await page.screenshot({
    path: testInfo.outputPath('workspace.png'),
    fullPage: true,
  });
  if (testInfo.project.name === 'mobile') {
    await page.getByRole('button', { name: '메뉴 열기' }).click();
    const drawer = page.getByRole('dialog');
    await expect(drawer).toBeVisible();
    await drawer.getByRole('link', { name: 'UI 가이드', exact: true }).click();
    await expect(drawer).not.toBeVisible();
  } else {
    await page.getByRole('button', { name: '사이드바 접기' }).click();
    await page.getByRole('button', { name: '사이드바 펼치기' }).click();
    await page
      .getByRole('navigation', { name: '주 메뉴' })
      .getByRole('link', { name: 'UI 가이드', exact: true })
      .click();
  }
  await expect(
    page.getByRole('heading', { level: 1, name: 'UI 가이드' }),
  ).toBeVisible();
  await expect(page.locator('#main-content')).toBeFocused();
  expect(
    await page.evaluate(
      () => document.documentElement.scrollWidth <= window.innerWidth,
    ),
  ).toBe(true);
  await page.screenshot({
    path: testInfo.outputPath('ui-guide.png'),
    fullPage: true,
  });
});

test('dialog supports keyboard dismissal and notifications are usable', async ({
  page,
}) => {
  await page.goto('/ui');
  const trigger = page.getByRole('button', { name: '대화상자 열기' });
  await trigger.click();
  await expect(
    page.getByRole('dialog', { name: '대화상자 미리보기' }),
  ).toBeVisible();
  expect(
    await page.evaluate(() =>
      [document.body, document.documentElement].some(
        (element) => getComputedStyle(element).overflow === 'hidden',
      ),
    ),
  ).toBe(true);
  await page.keyboard.press('Escape');
  await expect(page.getByRole('dialog')).not.toBeVisible();
  await expect(trigger).toBeFocused();
  await page.getByRole('button', { name: '알림 확인' }).click();
  await expect(page.getByText('알림 미리보기', { exact: true })).toBeVisible();
});

test('production UI renders under CSP without blocked styles or runtime errors', async ({
  page,
}) => {
  const failures: string[] = [];
  page.on('pageerror', (error) => failures.push(error.message));
  page.on('console', (message) => {
    if (message.type() === 'error') failures.push(message.text());
  });
  await page.addInitScript(() => {
    document.addEventListener('securitypolicyviolation', (event) => {
      console.error(
        `CSP violation: ${event.violatedDirective} ${event.blockedURI}`,
      );
    });
  });
  for (const path of ['/', '/settings', '/ui']) {
    await page.goto(path);
    await expect(page.getByRole('heading', { level: 1 })).toBeVisible();
    if (path === '/settings') {
      await page.getByRole('radio', { name: '다크', exact: true }).check();
      await page.getByRole('radio', { name: '라이트', exact: true }).check();
    }
  }
  const menu = page.getByRole('button', { name: '메뉴 열기' });
  if (await menu.isVisible()) {
    await menu.click();
    await expect(page.getByRole('dialog')).toBeVisible();
    await page.keyboard.press('Escape');
    await expect(page.getByRole('dialog')).not.toBeVisible();
  }
  await page.getByRole('button', { name: '대화상자 열기' }).click();
  await expect(page.getByRole('dialog')).toBeVisible();
  await page.keyboard.press('Escape');
  await page.getByRole('button', { name: '알림 확인' }).click();
  await expect(page.getByText('알림 미리보기', { exact: true })).toBeVisible();
  await page.getByRole('tab', { name: '입력 폼', exact: true }).click();
  await page.getByRole('combobox', { name: '상태 선택' }).press('ArrowDown');
  await expect(page.getByRole('listbox')).toBeVisible();
  await expect(page.locator('.base-ui-disable-scrollbar')).toHaveCSS(
    'scrollbar-width',
    'none',
  );
  await page.keyboard.press('Escape');
  await page.getByRole('tab', { name: '탐색과 목록', exact: true }).click();
  await page
    .getByRole('button', { name: '추가 작업', exact: true })
    .press('Enter');
  await expect(
    page.getByRole('menuitem', { name: '이름순 정렬' }),
  ).toBeVisible();
  await page.keyboard.press('Escape');
  await page.getByRole('tab', { name: '입력 폼', exact: true }).click();
  await page
    .getByRole('button', { name: '입력값 확인', exact: true })
    .press('Enter');
  await expect(
    page.getByRole('textbox', { name: '이름', exact: true }),
  ).toHaveAttribute('aria-invalid', 'true');
  await page.getByRole('tab', { name: '상태와 피드백', exact: true }).click();
  await page
    .getByRole('button', { name: '복구 예시 확인', exact: true })
    .press('Enter');
  await expect(
    page.getByRole('heading', { name: '화면 표시를 복구했습니다' }),
  ).toBeVisible();
  await page
    .getByRole('button', { name: '오류 예시 다시 보기', exact: true })
    .press('Enter');
  expect(failures).toEqual([]);
});

test('production policy blocks inline script execution', async ({
  page,
  request,
}) => {
  const response = await request.get('/');
  const policy = response.headers()['content-security-policy'];
  expect(policy).toContain("script-src 'self'");
  expect(policy).toContain("frame-ancestors 'none'");
  expect(response.headers()['x-content-type-options']).toBe('nosniff');
  await page.goto('/');
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

test('keyboard users can skip navigation', async ({ page }) => {
  await page.goto('/');
  await page.keyboard.press('Tab');
  await expect(
    page.getByRole('link', { name: '본문으로 건너뛰기' }),
  ).toBeFocused();
  await page.keyboard.press('Enter');
  await expect(page.locator('#main-content')).toBeFocused();
});

test('primary action text stays readable in light and dark themes', async ({
  page,
}) => {
  const luminance = (color: string) => {
    const channels = color
      .match(/[\d.]+/g)
      ?.slice(0, 3)
      .map((value) => Number(value) / 255);
    if (!channels || channels.length !== 3)
      throw new Error(`Unexpected color: ${color}`);
    return channels.reduce((sum, value, index) => {
      const linear =
        value <= 0.04045 ? value / 12.92 : ((value + 0.055) / 1.055) ** 2.4;
      return sum + linear * [0.2126, 0.7152, 0.0722][index]!;
    }, 0);
  };
  await page.goto('/ui');
  for (const theme of ['light', 'dark']) {
    if (theme === 'dark')
      await page.getByRole('button', { name: '어두운 테마로 전환' }).click();
    for (const path of ['/ui', '/does-not-exist']) {
      await page.goto(path);
      const action =
        path === '/ui'
          ? page.getByRole('button', { name: '대화상자 열기' })
          : page.getByRole('link', { name: '워크스페이스로 돌아가기' });
      const colors = await action.evaluate((element) => {
        const style = getComputedStyle(element);
        const canvas = document.createElement('canvas');
        canvas.width = canvas.height = 1;
        const context = canvas.getContext('2d')!;
        const rgb = (color: string) => {
          context.clearRect(0, 0, 1, 1);
          context.fillStyle = color;
          context.fillRect(0, 0, 1, 1);
          const [red, green, blue, alpha] = context.getImageData(
            0,
            0,
            1,
            1,
          ).data;
          return { color: `rgb(${red}, ${green}, ${blue})`, alpha };
        };
        const background = rgb(style.backgroundColor);
        return {
          background: background.color,
          text: rgb(style.color).color,
          alpha: background.alpha,
        };
      });
      expect(colors.alpha).toBe(255);
      const light = Math.max(
        luminance(colors.background),
        luminance(colors.text),
      );
      const dark = Math.min(
        luminance(colors.background),
        luminance(colors.text),
      );
      expect(
        (light + 0.05) / (dark + 0.05),
        `${theme} ${path}: ${JSON.stringify(colors)}`,
      ).toBeGreaterThanOrEqual(4.5);
    }
  }
});

test('official form controls and searchable table work without a backend', async ({
  page,
}) => {
  await page.goto('/ui');
  await page.getByRole('tab', { name: '입력 폼', exact: true }).click();
  await page.getByRole('combobox', { name: '상태 선택' }).click();
  await page.getByRole('option', { name: '완료', exact: true }).click();
  await expect(page.getByRole('combobox', { name: '상태 선택' })).toContainText(
    '완료',
  );
  await page.getByRole('switch', { name: '알림 받기' }).check();
  await expect(page.getByRole('switch', { name: '알림 받기' })).toBeChecked();
  await page.getByRole('tab', { name: '탐색과 목록', exact: true }).click();
  await page.getByRole('textbox', { name: '컴포넌트 검색' }).fill('Dialog');
  await expect(
    page.getByRole('row').filter({ hasText: 'Dialog' }),
  ).toBeVisible();
  await expect(page.getByRole('row').filter({ hasText: 'Button' })).toHaveCount(
    0,
  );
  await page
    .getByRole('textbox', { name: '컴포넌트 검색' })
    .fill('no-matching-component');
  await expect(
    page.getByText('검색 결과가 없습니다.', { exact: true }),
  ).toBeVisible();
});

test('screen search supports keyboard navigation', async ({ page }) => {
  await page.goto('/');
  await page.keyboard.press('Control+k');
  await expect(page.getByRole('dialog', { name: '화면 검색' })).toBeVisible();
  await page
    .getByRole('combobox', { name: '화면 이름 검색' })
    .fill('화면 설정');
  await page.keyboard.press('Enter');
  await expect(page).toHaveURL(/\/settings$/);
  await expect(
    page.getByRole('heading', { level: 1, name: '화면 설정' }),
  ).toBeVisible();
});

test('Korean variable font loads from the app origin', async ({ page }) => {
  await page.goto('/');
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
  const fontUrls = await page.evaluate(() =>
    performance
      .getEntriesByType('resource')
      .map((entry) => entry.name)
      .filter((name) => name.includes('.woff2')),
  );
  expect(fontUrls.length).toBeGreaterThan(0);
  for (const url of fontUrls)
    expect(new URL(url).origin).toBe('http://127.0.0.1:4173');
});
