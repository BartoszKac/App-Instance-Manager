import type { Activity } from '@/editor/viewmodels/useBuildActions';
import { Icon } from '@/shared/ui/Icon';
import type { IconName } from '@/shared/ui/Icon';
import { Spinner } from '@/shared/ui/atoms';

export interface BuildStepView {
  id: 'save' | 'compile' | 'run';
  label: string;
  icon: IconName;
  done: boolean;
  enabled: boolean;
  hint: string;
  onClick: () => void;
}

interface BuildStripProps {
  steps: BuildStepView[];
  activity: Activity;
}

export const BuildStrip = ({ steps, activity }: BuildStripProps) => (
  <ol className="build-strip" aria-label="Zapis, kompilacja i uruchomienie">
    {steps.map((s) => {
      const busy = activity.tone === 'busy' && activity.step === s.id;
      const failed = activity.tone === 'error' && activity.step === s.id;
      const state = busy ? 'busy' : failed ? 'failed' : s.done ? 'done' : 'todo';
      return (
        <li key={s.id} className="build-strip__item" data-state={state}>
          <button
            className={`build-strip__step ${s.id === 'run' ? 'build-strip__step--run' : ''}`}
            onClick={s.onClick}
            disabled={!s.enabled || activity.tone === 'busy'}
            title={s.enabled ? undefined : s.hint}
          >
            {busy ? <Spinner /> : <Icon name={s.done && s.id !== 'run' ? 'check' : s.icon} size={17} />}
            {s.label}
          </button>
        </li>
      );
    })}
  </ol>
);
