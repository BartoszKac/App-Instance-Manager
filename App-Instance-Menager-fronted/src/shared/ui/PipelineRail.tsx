import { useLayoutEffect, useRef } from 'react';
import { gsap } from 'gsap';
import { Icon } from './Icon';
import type { IconName } from './Icon';
import { prefersReducedMotion } from '@/shared/motion';

export type StationId = 'code' | 'upload' | 'deploy' | 'execution';

interface Station {
  id: StationId;
  label: string;
  hint: string;
  icon: IconName;
}

const STATIONS: Station[] = [
  { id: 'code', label: 'Kod', hint: 'Projekty i edytor', icon: 'code' },
  { id: 'upload', label: 'Plik', hint: 'Prześlij gotowy plik', icon: 'upload' },
  { id: 'deploy', label: 'Wysyłka', hint: 'Wyślij na serwery', icon: 'ship' },
  { id: 'execution', label: 'Uruchom', hint: 'Komendy na serwerach', icon: 'run' },
];

interface PipelineRailProps {
  active: StationId;
  onSelect: (id: StationId) => void;
}

const useMarker = (wrapRef: React.RefObject<HTMLDivElement | null>, active: StationId) => {
  useLayoutEffect(() => {
    const wrap = wrapRef.current;
    const target = wrap?.querySelector<HTMLElement>(`[data-station="${active}"]`);
    if (!wrap || !target) return;
    const place = () => {
      const center = { x: target.offsetLeft + target.offsetWidth / 2, y: target.offsetTop + target.offsetHeight / 2 };
      const vars = { '--marker-x': `${center.x}px`, '--marker-y': `${center.y}px` };
      if (prefersReducedMotion()) gsap.set(wrap, vars);
      else gsap.to(wrap, { ...vars, duration: 0.55, ease: 'expo.out' });
    };
    place();
    window.addEventListener('resize', place);
    return () => window.removeEventListener('resize', place);
  }, [wrapRef, active]);
};

export const PipelineRail = ({ active, onSelect }: PipelineRailProps) => {
  const wrapRef = useRef<HTMLDivElement>(null);
  useMarker(wrapRef, active);

  return (
    <nav className="rail" aria-label="Etapy pracy">
      <div className="rail__brand" aria-label="BackRunner">br</div>
      <div className="rail__stations" ref={wrapRef}>
        <span className="rail__track" aria-hidden="true"><span className="rail__fill" /></span>
        <span className="rail__marker" aria-hidden="true" />
        <ol className="rail__list">
          {STATIONS.map((s) => (
            <li key={s.id}>
              <button
                type="button"
                data-station={s.id}
                className="rail__station"
                aria-current={s.id === active ? 'page' : undefined}
                title={s.hint}
                onClick={() => onSelect(s.id)}
              >
                <Icon name={s.icon} size={20} />
                <span className="rail__label">{s.label}</span>
              </button>
            </li>
          ))}
        </ol>
      </div>
    </nav>
  );
};
