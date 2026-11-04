import { StatusDot } from '@/shared/ui/atoms';
import type { InspectionStatus } from '../model/Inspection';
import { STATE_LABEL, STATE_TONE } from './format';

interface ServerStatusListProps {
  servers: InspectionStatus[];
  selectedId: number | null;
  onSelect: (id: number) => void;
}

const ServerItem = ({ server, isSelected, onSelect }: { server: InspectionStatus; isSelected: boolean; onSelect: (id: number) => void }) => (
  <li>
    <button type="button" className="admin-server" aria-current={isSelected ? 'true' : undefined} onClick={() => onSelect(server.serverId)}>
      <StatusDot tone={server.state ? STATE_TONE[server.state] : 'idle'} />
      <span className="admin-server__body">
        <span className="admin-server__name">{server.name}</span>
        <span className="admin-server__ip">{server.user}@{server.ip}</span>
      </span>
      <span className="admin-server__state">{server.state ? STATE_LABEL[server.state] : 'Nie zbadano'}</span>
    </button>
  </li>
);

export const ServerStatusList = ({ servers, selectedId, onSelect }: ServerStatusListProps) => (
  <ul className="admin-servers" aria-label="Serwery">
    {servers.map((server) => (
      <ServerItem key={server.serverId} server={server} isSelected={server.serverId === selectedId} onSelect={onSelect} />
    ))}
  </ul>
);
