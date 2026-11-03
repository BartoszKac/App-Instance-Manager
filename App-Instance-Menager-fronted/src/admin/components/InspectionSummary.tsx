import { Icon } from '@/shared/ui/Icon';
import { Notice, Spinner } from '@/shared/ui/atoms';
import type { InspectionStatus } from '../model/Inspection';
import { formatIso } from '@/shared/format';
import { STAGE_LABEL } from './format';

interface InspectionSummaryProps {
  server: InspectionStatus;
  fileCount: number;
  onInspect: () => void;
  onTrustNewKey: () => void;
  onEditCredentials: () => void;
}

const isActive = (server: InspectionStatus) => server.state === 'QUEUED' || server.state === 'RUNNING';

const Progress = ({ server }: { server: InspectionStatus }) => (
  <p className="admin-summary__progress" role="status">
    <Spinner />
    {server.state === 'QUEUED' ? 'Czeka na wolną instancję' : STAGE_LABEL[server.stage ?? ''] ?? 'Badanie'}
    {server.filesFound > 0 && `, znaleziono ${server.filesFound.toLocaleString('pl-PL')} wpisów`}
  </p>
);

const Failure = ({ server, onTrustNewKey }: Pick<InspectionSummaryProps, 'server' | 'onTrustNewKey'>) => (
  <Notice tone="error">
    {server.error}
    {server.failureKind === 'HOST_KEY_CHANGED' && (
      <button className="btn btn--sm btn--danger admin-summary__trust" onClick={onTrustNewKey}>Zaufaj nowemu kluczowi</button>
    )}
  </Notice>
);

export const InspectionSummary = ({ server, fileCount, onInspect, onTrustNewKey, onEditCredentials }: InspectionSummaryProps) => (
  <div className="admin-summary">
    <div className="admin-summary__head">
      <div>
        <h2 className="admin-summary__name">{server.name}</h2>
        <p className="admin-summary__meta">
          {server.dataFrom ? `Dane z ${formatIso(server.dataFrom)}, ${fileCount.toLocaleString('pl-PL')} wpisów` : 'Brak wyników badania'}
        </p>
      </div>
      <div className="admin-summary__actions">
        <button className="btn btn--quiet" onClick={onEditCredentials}>
          <Icon name="build" size={16} />Zmień hasło
        </button>
        <button className="btn btn--quiet" onClick={onInspect} disabled={isActive(server)}>
          <Icon name="refresh" size={16} />Zbadaj ponownie
        </button>
      </div>
    </div>
    {isActive(server) && <Progress server={server} />}
    {server.state === 'FAILED' && <Failure server={server} onTrustNewKey={onTrustNewKey} />}
    {server.truncated && <Notice tone="warn">Serwer ma więcej plików niż limit badania. Lista plików jest niepełna.</Notice>}
  </div>
);
