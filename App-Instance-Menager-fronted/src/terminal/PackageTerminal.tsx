import { useEffect, useRef, useState } from 'react';
import { useConsoleConnection } from './useConsoleConnection';
import type { ConnectionState } from './useConsoleConnection';
import { Icon } from '@/shared/ui/Icon';
import { StatusDot } from '@/shared/ui/atoms';
import type { Tone } from '@/shared/ui/atoms';

const MIN_HEIGHT = 120;
const MAX_HEIGHT = 560;
const STEP = 40;

const STATE_VIEW: Record<ConnectionState, { tone: Tone; label: string }> = {
  connected: { tone: 'ok', label: 'Połączono' },
  connecting: { tone: 'warn', label: 'Łączenie…' },
  disconnected: { tone: 'error', label: 'Rozłączono' },
};

const clamp = (h: number) => Math.min(MAX_HEIGHT, Math.max(MIN_HEIGHT, h));

const useResize = (initial: number) => {
  const [height, setHeight] = useState(initial);
  const startDrag = (e: React.PointerEvent<HTMLDivElement>) => {
    const startY = e.clientY;
    const startH = height;
    const move = (ev: PointerEvent) => setHeight(clamp(startH + startY - ev.clientY));
    const up = () => { window.removeEventListener('pointermove', move); window.removeEventListener('pointerup', up); };
    window.addEventListener('pointermove', move);
    window.addEventListener('pointerup', up);
  };
  const onKey = (e: React.KeyboardEvent) => {
    if (e.key === 'ArrowUp') setHeight((h) => clamp(h + STEP));
    if (e.key === 'ArrowDown') setHeight((h) => clamp(h - STEP));
  };
  return { height, startDrag, onKey };
};

export default function PackageTerminal() {
  const { lines, state, connect, disconnect, clear } = useConsoleConnection();
  const { height, startDrag, onKey } = useResize(170);
  const [open, setOpen] = useState(() => !window.matchMedia('(max-width: 760px)').matches);
  const endRef = useRef<HTMLLIElement>(null);
  const view = STATE_VIEW[state];

  useEffect(() => { endRef.current?.scrollIntoView({ block: 'end' }); }, [lines, open]);

  return (
    <section className="console" data-open={open} style={open ? { height } : undefined} aria-label="Konsola programu">
      {open && (
        <div className="console__handle" role="separator" aria-orientation="horizontal" aria-label="Zmień wysokość konsoli" aria-valuenow={height} aria-valuemin={MIN_HEIGHT} aria-valuemax={MAX_HEIGHT} tabIndex={0} onPointerDown={startDrag} onKeyDown={onKey} />
      )}
      <header className="console__bar">
        <button className="console__toggle" onClick={() => setOpen((v) => !v)} aria-expanded={open}>
          <Icon name={open ? 'chevronDown' : 'chevronRight'} size={16} />Konsola
        </button>
        <span className="console__state"><StatusDot tone={view.tone} />{view.label}</span>
        <span className="console__spacer" />
        {state === 'connected'
          ? <button className="btn btn--ghost btn--sm" onClick={disconnect}>Rozłącz</button>
          : <button className="btn btn--ghost btn--sm" onClick={connect} disabled={state === 'connecting'}>Połącz</button>}
        <button className="btn btn--ghost btn--sm" onClick={clear} disabled={lines.length === 0}><Icon name="erase" size={15} />Wyczyść</button>
      </header>
      {open && (
        <ol className="console__lines" aria-live="polite">
          {lines.length === 0 && <li className="console__line console__line--info">Tu pojawi się wynik kompilacji i uruchomień.</li>}
          {lines.map((l) => <li key={l.id} className={`console__line console__line--${l.type}`}>{l.text}</li>)}
          <li ref={endRef} aria-hidden="true" />
        </ol>
      )}
    </section>
  );
}
