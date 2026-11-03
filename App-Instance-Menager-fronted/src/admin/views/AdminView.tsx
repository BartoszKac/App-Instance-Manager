import { ScreenHeader } from '@/shared/ui/ScreenHeader';
import { EmptyState, Notice } from '@/shared/ui/atoms';
import { EditCredentialsModal } from '../components/EditCredentialsModal';
import { FactsPanel } from '../components/FactsPanel';
import { FileSearchResults } from '../components/FileSearchResults';
import { FileTree } from '../components/FileTree';
import { InspectionSummary } from '../components/InspectionSummary';
import { ServerStatusList } from '../components/ServerStatusList';
import { DirectUploadView } from './DirectUploadView';
import { FILES_TAB, UPLOAD_TAB, useAdminViewModel } from '../viewmodels/useAdminViewModel';
import type { AdminViewModel } from '../viewmodels/useAdminViewModel';

const Tabs = ({ vm }: { vm: AdminViewModel }) => (
  <div className="segmented" role="group" aria-label="Wyniki badania">
    {vm.tabs.map((tab) => (
      <button key={tab} type="button" className="segmented__item" aria-pressed={tab === vm.activeTab} onClick={() => vm.setTab(tab)}>{tab}</button>
    ))}
  </div>
);

const FilesPanel = ({ vm }: { vm: AdminViewModel }) => (
  <>
    <input
      className="input input--mono"
      type="search"
      placeholder="Szukaj w ścieżkach, np. nginx.conf"
      aria-label="Szukaj plików"
      value={vm.search.query}
      onChange={(e) => vm.search.setQuery(e.target.value)}
    />
    {vm.search.isActive
      ? <FileSearchResults results={vm.search.results} isSearching={vm.search.isSearching} />
      : <FileTree tree={vm.tree} />}
  </>
);

const DataPanel = ({ vm, hasData }: { vm: AdminViewModel; hasData: boolean }) => {
  if (!hasData) return <EmptyState title="Brak wyników badania" hint="Pliki pojawią się po udanym badaniu serwera." />;
  return vm.activeTab === FILES_TAB ? <FilesPanel vm={vm} /> : <FactsPanel sections={vm.visibleSections} />;
};

const Results = ({ vm }: { vm: AdminViewModel }) => {
  const server = vm.servers.selected;
  if (!server) return null;
  return (
    <div className="admin-results">
      <InspectionSummary
        server={server}
        fileCount={vm.fileCount}
        onInspect={() => void vm.servers.inspect(server.serverId)}
        onTrustNewKey={() => void vm.servers.trustNewKey(server.serverId)}
        onEditCredentials={vm.credentials.open}
      />
      <Tabs vm={vm} />
      <div hidden={vm.activeTab !== UPLOAD_TAB}><DirectUploadView key={server.serverId} serverId={server.serverId} /></div>
      {vm.activeTab !== UPLOAD_TAB && <DataPanel vm={vm} hasData={server.dataFrom !== null} />}
    </div>
  );
};

export const AdminView = () => {
  const vm = useAdminViewModel();
  const isEmpty = !vm.servers.isLoading && vm.servers.servers.length === 0;

  return (
    <>
      <ScreenHeader title="Panel admina" context="Co jest na serwerach: system, usługi, użytkownicy i pliki." />
      <div className="admin" data-enter="body">
        {vm.error && <Notice tone="error">{vm.error}</Notice>}
        {isEmpty && <EmptyState title="Brak serwerów" hint="Dodaj serwer na ekranie Wysyłka. Badanie ruszy samo po zapisaniu hasła." />}
        {!isEmpty && (
          <div className="admin__columns">
            <ServerStatusList servers={vm.servers.servers} selectedId={vm.servers.selected?.serverId ?? null} onSelect={vm.servers.setSelectedId} />
            <Results vm={vm} />
          </div>
        )}
      </div>
      {vm.credentials.isOpen && vm.servers.selected && <EditCredentialsModal form={vm.credentials} serverName={vm.servers.selected.name} />}
    </>
  );
};
