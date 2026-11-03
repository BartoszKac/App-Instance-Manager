import { getServerId } from '../model/DeployState';
import type { DeployServer } from '../model/DeployState';
import type { DeployViewModel } from '../viewmodel/useDeployViewModel';
import { AddServerModal } from './AddServerModal';
import { NetworkScanModal } from './NetworkScanModal';
import { ServerPreviewModal } from './ServerPreviewModal';
import { Checkbox, Spinner } from '@/shared/ui/atoms';
import { Icon } from '@/shared/ui/Icon';

interface ServerRowProps {
  server: DeployServer;
  checked: boolean;
  onToggle: () => void;
  onDelete: () => void;
  onPreview: () => void;
}

const ServerRow = ({ server, checked, onToggle, onDelete, onPreview }: ServerRowProps) => (
  <li className="pick-row pick-row--server" data-checked={checked}>
    <Checkbox checked={checked} onChange={onToggle} label={`Wybierz serwer ${server.name}`} />
    <button className="server-row__open" onClick={onPreview} title="Pokaż podgląd serwera">
      <span className="pick-row__name pick-row__name--plain">{server.name || 'Bez nazwy'}</span>
      <span className="pick-row__address">{server.user}@{server.ip}</span>
    </button>
    {server.operationSystem && <span className="pick-row__os">{server.operationSystem}</span>}
    <button className="icon-btn" onClick={onPreview} aria-label={`Pokaż podgląd serwera ${server.name}`}>
      <Icon name="cpu" size={16} />
    </button>
    <button className="icon-btn icon-btn--danger" onClick={onDelete} aria-label={`Usuń serwer ${server.name}`}>
      <Icon name="trash" size={16} />
    </button>
  </li>
);

const ServerList = ({ vm }: { vm: DeployViewModel }) => (
  <ul className="pick-group__list">
    {vm.allServers.map((srv) => {
      const id = getServerId(srv);
      return <ServerRow key={id} server={srv} checked={vm.selectedServers.has(id)} onToggle={() => vm.toggleServer(id)} onDelete={() => vm.deleteServer(id)} onPreview={() => vm.serverPreview.open(srv)} />;
    })}
  </ul>
);

export const ServersPanel = ({ vm }: { vm: DeployViewModel }) => (
  <aside className="deploy-panel deploy-panel--right" aria-label="Serwery docelowe">
    <div className="deploy-panel__head">
      <span className="deploy-panel__title">Serwery</span>
      <div className="deploy-panel__actions">
        <button className="btn btn--quiet btn--sm" onClick={vm.networkScan.open}><Icon name="search" size={16} />Skanuj sieć</button>
        <button className="btn btn--quiet btn--sm" onClick={() => vm.setShowAddServer(true)}><Icon name="add" size={16} />Dodaj</button>
      </div>
    </div>
    <div className="deploy-panel__body">
      {vm.loading && <p className="deploy-panel__loading"><Spinner />Wczytywanie serwerów…</p>}
      {!vm.loading && vm.allServers.length === 0 && <p className="deploy-panel__empty">Nie ma jeszcze serwerów. Dodaj pierwszy, podając adres i login SSH.</p>}
      <ServerList vm={vm} />
    </div>
    {vm.showAddServer && <AddServerModal vm={vm} />}
    {vm.networkScan.isOpen && <NetworkScanModal scan={vm.networkScan} />}
    <ServerPreviewModal preview={vm.serverPreview} />
  </aside>
);
