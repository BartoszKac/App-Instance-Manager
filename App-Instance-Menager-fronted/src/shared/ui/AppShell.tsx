import { useRef } from 'react';
import type { ReactNode } from 'react';
import { PipelineRail } from './PipelineRail';
import type { StationId } from './PipelineRail';
import { useScreenEnter } from '@/shared/motion';

interface AppShellProps {
  station: StationId;
  screenKey: string;
  onNavigate: (id: StationId) => void;
  console: ReactNode;
  children: ReactNode;
}

export const AppShell = ({ station, screenKey, onNavigate, console, children }: AppShellProps) => {
  const mainRef = useRef<HTMLElement>(null);
  useScreenEnter(mainRef, screenKey);

  return (
    <div className="shell">
      <a className="skip-link" href="#main">Przejdź do treści</a>
      <PipelineRail active={station} onSelect={onNavigate} />
      <div className="shell__workspace">
        <main id="main" className="shell__main" ref={mainRef} key={screenKey}>
          {children}
        </main>
        {console}
      </div>
    </div>
  );
};
