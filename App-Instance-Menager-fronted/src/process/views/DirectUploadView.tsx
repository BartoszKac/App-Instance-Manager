import { EmptyState, Notice } from '@/shared/ui/atoms';
import type { InspectionStatus } from '@/shared/inspection/model';
import { FolderPickerModal } from '../components/FolderPickerModal';
import { UploadForm } from '../components/UploadForm';
import { UploadHistory } from '../components/UploadHistory';
import { UploadProgress } from '../components/UploadProgress';
import { useDirectUpload } from '../viewmodel/useDirectUpload';
import { useUploadTargets } from '../viewmodel/useUploadTargets';

const ServerPanel = ({ server }: { server: InspectionStatus }) => {
  const vm = useDirectUpload(server.serverId);
  return (
    <>
      {vm.error && <Notice tone="error">{vm.error}</Notice>}
      <UploadForm vm={vm} hasFolderList={server.dataFrom !== null} />
      <UploadProgress vm={vm} />
      <UploadHistory history={vm.history} />
      {vm.folders.isOpen && <FolderPickerModal folders={vm.folders} />}
    </>
  );
};

export const DirectUploadView = () => {
  const targets = useUploadTargets();
  if (!targets.isLoading && targets.servers.length === 0 && !targets.error) {
    return <EmptyState title="Brak serwerów" hint="Dodaj serwer na ekranie Wysyłka, a potem wróć tutaj." />;
  }
  return (
    <div className="direct-upload">
      {targets.error && <Notice tone="error">{targets.error}</Notice>}
      <label className="field">
        <span className="field__label">Serwer</span>
        <select className="select" value={targets.selected?.serverId ?? ''} onChange={(e) => targets.setSelectedId(Number(e.target.value))}>
          {targets.servers.map((server) => <option key={server.serverId} value={server.serverId}>{server.name} ({server.user}@{server.ip})</option>)}
        </select>
      </label>
      {targets.selected && <ServerPanel key={targets.selected.serverId} server={targets.selected} />}
    </div>
  );
};
