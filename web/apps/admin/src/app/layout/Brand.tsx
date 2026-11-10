import { Link } from 'react-router-dom';
import { Layers2 } from 'lucide-react';
import { previewPaths } from '../routes';
import { usePreviewContent } from '@/pages/preview/preview-data';

export function Brand({ compact = false }: { compact?: boolean }) {
  const { href } = usePreviewContent();
  return (
    <Link
      to={href(previewPaths.home)}
      className="flex min-h-10 items-center gap-2.5 rounded-lg p-1"
      aria-label="Etude 홈"
    >
      <span className="grid size-8 shrink-0 place-items-center rounded-lg bg-violet-600 text-white">
        <Layers2 className="size-[18px]" strokeWidth={1.8} aria-hidden="true" />
      </span>
      {!compact && (
        <span className="text-lg font-semibold tracking-tight">etude</span>
      )}
    </Link>
  );
}
