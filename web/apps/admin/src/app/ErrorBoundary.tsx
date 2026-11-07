import { Component, type PropsWithChildren } from 'react';
import { Button, EmptyState } from '@etude/ui';

export class ErrorBoundary extends Component<
  PropsWithChildren,
  { failed: boolean }
> {
  state = { failed: false };

  static getDerivedStateFromError() {
    return { failed: true };
  }

  render() {
    if (this.state.failed) {
      return (
        <main>
          <EmptyState
            title="화면을 불러오지 못했습니다"
            description="잠시 후 다시 시도해 주세요. 문제가 계속되면 페이지를 새로 열어 주세요."
            action={
              <Button onClick={() => window.location.reload()}>
                다시 불러오기
              </Button>
            }
          />
        </main>
      );
    }
    return this.props.children;
  }
}
