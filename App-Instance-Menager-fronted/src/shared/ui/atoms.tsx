import type { ReactNode } from 'react';
import { LANG_LABEL, langClass, toLangKey } from '@/shared/lang';

export type Tone = 'ok' | 'warn' | 'error' | 'idle';

export const LangChip = ({ lang, label }: { lang: string; label?: string }) => (
  <span className={`lang-chip ${langClass(lang)}`}>{label ?? LANG_LABEL[toLangKey(lang)]}</span>
);

export const StatusDot = ({ tone, label }: { tone: Tone; label?: string }) => (
  <span className={`status-dot status-dot--${tone}`} role={label ? 'img' : undefined} aria-label={label} />
);

export const Spinner = () => <span className="spinner" aria-hidden="true" />;

interface CheckboxProps {
  checked: boolean;
  indeterminate?: boolean;
  onChange: () => void;
  label: string;
}

export const Checkbox = ({ checked, indeterminate = false, onChange, label }: CheckboxProps) => (
  <input
    type="checkbox"
    className="checkbox"
    checked={checked}
    aria-label={label}
    ref={(el) => { if (el) el.indeterminate = indeterminate && !checked; }}
    onChange={onChange}
    onClick={(e) => e.stopPropagation()}
  />
);

export const Notice = ({ tone, children }: { tone: Exclude<Tone, 'idle'>; children: ReactNode }) => (
  <div className={`notice notice--${tone}`} role={tone === 'error' ? 'alert' : 'status'}>
    {children}
  </div>
);

export const EmptyState = ({ title, hint, action }: { title: string; hint?: string; action?: ReactNode }) => (
  <div className="empty-state">
    <p className="empty-state__title">{title}</p>
    {hint && <p className="empty-state__hint">{hint}</p>}
    {action}
  </div>
);
