// External bootstrap keeps theme initialization compatible with script-src 'self'.
(() => {
  let theme = 'system';
  try {
    const stored = localStorage.getItem('etude-theme');
    if (['light', 'dark', 'system'].includes(stored)) theme = stored;
  } catch {
    /* Browser storage may be unavailable. */
  }
  const dark =
    theme === 'dark' ||
    (theme === 'system' && matchMedia('(prefers-color-scheme: dark)').matches);
  document.documentElement.classList.toggle('dark', dark);
  document.documentElement.dataset.theme = dark ? 'dark' : 'light';
})();
