import { Link } from 'react-router-dom';
import { MapPinOff } from 'lucide-react';
import { buttonVariants, EmptyState } from '@etude/ui';

export function NotFound() {
  return (
    <EmptyState
      icon={<MapPinOff className="size-6" />}
      title="페이지를 찾을 수 없습니다"
      description="주소가 변경되었거나 존재하지 않는 페이지입니다."
      action={
        <Link className={buttonVariants()} to="/">
          워크스페이스로 돌아가기
        </Link>
      }
    />
  );
}
