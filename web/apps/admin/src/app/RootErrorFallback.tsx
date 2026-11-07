export function RootErrorFallback() {
  return (
    <main className="grid min-h-dvh place-items-center p-6">
      <section role="alert" className="max-w-md text-center">
        <h1 className="text-xl font-semibold">앱을 불러오지 못했습니다</h1>
        <p className="mt-3 text-sm leading-7 text-muted-foreground">
          페이지를 다시 불러와 주세요. 문제가 계속되면 잠시 후 다시 시도해
          주세요.
        </p>
        <button
          type="button"
          className="mt-6 rounded-md border bg-background px-4 py-2 text-sm font-medium focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-ring"
          onClick={() => window.location.reload()}
        >
          다시 불러오기
        </button>
      </section>
    </main>
  );
}
