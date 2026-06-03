// src/deploy/components/DeployAtoms.tsx
import React from 'react';
import { EXT_COLOR } from '../model/DeployState';

// ─── CHECKBOX ─────────────────────────────────────────────────────────────────

interface CheckboxProps {
  checked: boolean;
  indeterminate?: boolean;
  onChange: () => void;
}

export const Checkbox: React.FC<CheckboxProps> = ({ checked, indeterminate, onChange }) => (
  <div
    onClick={onChange}
    style={{
      width: 15, height: 15, borderRadius: 4, flexShrink: 0, cursor: 'pointer',
      border: `1.5px solid ${checked || indeterminate ? '#4f7ef7' : '#2a3347'}`,
      background: checked ? '#4f7ef7' : indeterminate ? 'rgba(79,126,247,.25)' : 'transparent',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      transition: 'all .12s',
    }}
  >
    {checked && (
      <svg width="9" height="9" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="3.5">
        <path d="M20 6 9 17l-5-5" />
      </svg>
    )}
    {!checked && indeterminate && (
      <div style={{ width: 7, height: 1.5, background: '#4f7ef7', borderRadius: 1 }} />
    )}
  </div>
);

// ─── STATUS DOT ───────────────────────────────────────────────────────────────

interface StatusDotProps {
  status: string;
}

export const StatusDot: React.FC<StatusDotProps> = ({ status }) => {
  const c = status === 'online' ? '#22c55e' : status === 'offline' ? '#ef4444' : '#f59e0b';
  return (
    <span style={{
      width: 7, height: 7, borderRadius: '50%', background: c,
      display: 'inline-block', boxShadow: `0 0 6px ${c}90`, flexShrink: 0,
    }} />
  );
};

// ─── FILE CHIP ────────────────────────────────────────────────────────────────

interface FileChipProps {
  ext: string;
}

export const FileChip: React.FC<FileChipProps> = ({ ext }) => {
  const c = EXT_COLOR[ext] ?? '#94a3b8';
  return (
    <span style={{
      fontFamily: 'var(--mono)', fontSize: 10, fontWeight: 600,
      padding: '1px 5px', borderRadius: 3,
      background: c + '20', color: c, flexShrink: 0,
    }}>.{ext}</span>
  );
};

// ─── LANG BADGE ───────────────────────────────────────────────────────────────

interface LangBadgeProps {
  lang: string;
}

export const LangBadge: React.FC<LangBadgeProps> = ({ lang }) => {
  const c = EXT_COLOR[lang] ?? '#94a3b8';
  return (
    <span style={{
      fontFamily: 'var(--mono)', fontSize: 9, fontWeight: 700, letterSpacing: '.05em',
      padding: '2px 6px', borderRadius: 3,
      background: c + '22', color: c,
    }}>{lang.toUpperCase()}</span>
  );
};
