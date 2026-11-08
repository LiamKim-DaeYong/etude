import { useId, type ReactNode } from 'react';
import { Label } from './components/label';

type ControlProps = {
  id: string;
  'aria-describedby': string | undefined;
  'aria-invalid': true | undefined;
};

export function FormField({
  id,
  label,
  description,
  error,
  children,
}: {
  id?: string;
  label: string;
  description?: string;
  error?: string;
  children: (controlProps: ControlProps) => ReactNode;
}) {
  const generatedId = useId();
  const controlId = id ?? generatedId;
  const descriptionId = `${controlId}-description`;
  const errorId = `${controlId}-error`;
  const describedBy =
    [description && descriptionId, error && errorId]
      .filter(Boolean)
      .join(' ') || undefined;

  return (
    <div className="space-y-2">
      <Label htmlFor={controlId}>{label}</Label>
      {children({
        id: controlId,
        'aria-describedby': describedBy,
        'aria-invalid': error ? true : undefined,
      })}
      {description && (
        <p
          id={descriptionId}
          className="text-xs leading-6 text-muted-foreground"
        >
          {description}
        </p>
      )}
      {error && (
        <p id={errorId} className="text-sm leading-6 text-destructive">
          {error}
        </p>
      )}
    </div>
  );
}
