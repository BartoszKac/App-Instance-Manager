import { StatusDot } from '@/shared/ui/atoms';
import type { DirectUploadPage, DirectUploadRecord, DirectUploadState } from '../model/DirectUpload';
import type { Tone } from '@/shared/ui/atoms';
import { formatBytes, formatIso } from '@/shared/format';

const TONE: Record<DirectUploadState, Tone> = { IN_PROGRESS: 'warn', DONE: 'ok', FAILED: 'error' };

const Row = ({ record }: { record: DirectUploadRecord }) => (
  <li className="direct-upload-row">
    <StatusDot tone={TONE[record.state]} />
    <span className="direct-upload-row__path" title={record.remotePath ?? record.relativePath}>{record.remotePath ?? record.relativePath}</span>
    <span className="direct-upload-row__size">{record.state === 'DONE' ? formatBytes(record.sizeBytes) : ''}</span>
    <span className="direct-upload-row__date">{formatIso(record.startedAt)}</span>
    {record.sha256 && <span className="direct-upload-row__hash">SHA-256 {record.sha256}</span>}
    {record.error && <span className="direct-upload-row__error">{record.error}</span>}
  </li>
);

export const UploadHistory = ({ history }: { history: DirectUploadPage | null }) => {
  if (!history || history.total === 0) return null;
  return (
    <section className="direct-upload-list">
      <h3 className="direct-upload-list__title">Ostatnie wysyłki ({history.total.toLocaleString('pl-PL')})</h3>
      <ul className="direct-upload-list__items">{history.entries.map((record) => <Row key={record.id} record={record} />)}</ul>
    </section>
  );
};
