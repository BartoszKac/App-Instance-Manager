import { useState } from 'react';
import type { DeployedInstance } from '@/execution/model/Executionmodel';
import { PRESET_GROUPS } from '@/execution/model/presets';
import { Icon } from '@/shared/ui/Icon';
import { LangChip, StatusDot } from '@/shared/ui/atoms';

interface InstanceCardProps {
  instance: DeployedInstance;
  onRun: (idConfiguration: number, command: string) => void;
}

const Presets = ({ instance, onPick }: { instance: DeployedInstance; onPick: (cmd: string) => void }) => {
  const [group, setGroup] = useState(PRESET_GROUPS[0].group);
  const items = PRESET_GROUPS.find((g) => g.group === group)?.items ?? [];
  return (
    <div className="presets">
      <div className="segmented segmented--sm" role="group" aria-label="Grupa komend">
        {PRESET_GROUPS.map((g) => (
          <button key={g.group} className="segmented__item" aria-pressed={g.group === group} onClick={() => setGroup(g.group)}>{g.group}</button>
        ))}
      </div>
      <div className="presets__items">
        {items.map((p) => (
          <button key={p.label} className="preset" title={p.cmd(instance)} onClick={() => onPick(p.cmd(instance))}>{p.label}</button>
        ))}
      </div>
    </div>
  );
};

export const InstanceCard = ({ instance, onRun }: InstanceCardProps) => {
  const [command, setCommand] = useState('');
  const [showPresets, setShowPresets] = useState(false);
  const run = () => { if (command.trim()) onRun(instance.idConfiguration, command); };

  return (
    <li className="instance">
      <div className="instance__head">
        <StatusDot tone={instance.status === 'online' ? 'ok' : 'error'} label={instance.status === 'online' ? 'Dostępny' : 'Niedostępny'} />
        <span className="instance__server">{instance.serverName}</span>
        <LangChip lang={instance.lang} />
      </div>
      <p className="instance__path">{instance.path}</p>
      <div className="prompt">
        <span className="prompt__sigil" aria-hidden="true">$</span>
        <input
          className="prompt__input"
          value={command}
          placeholder="Wpisz komendę i naciśnij Enter"
          aria-label={`Komenda dla ${instance.serverName}`}
          onChange={(e) => setCommand(e.target.value)}
          onKeyDown={(e) => { if (e.key === 'Enter') run(); }}
        />
        <button className="btn btn--run btn--sm" onClick={run} disabled={!command.trim()}><Icon name="play" size={14} />Uruchom</button>
      </div>
      <button className="instance__toggle" aria-expanded={showPresets} onClick={() => setShowPresets((v) => !v)}>
        <Icon name={showPresets ? 'chevronDown' : 'chevronRight'} size={16} />Gotowe komendy
      </button>
      {showPresets && <Presets instance={instance} onPick={setCommand} />}
    </li>
  );
};
