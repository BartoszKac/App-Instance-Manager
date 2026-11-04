import type { Tone } from '@/shared/ui/atoms';
import { StatusDot } from '@/shared/ui/atoms';
import type { UploadItem, UploadItemStatus } from '../model/DirectUpload';
import type { DirectUploadViewModel } from '../viewmodel/useDirectUpload';
import { formatBytes } from '@/shared/format';

const MAX_VISIBLE = 100;

const TONE: Record<UploadItemStatus, Tone> = { waiting: 'idle', sending: 'warn', done: 'ok', failed: 'error' };

const ORDER: Record<UploadItemStatus, number> = { failed: 0, sending: 1, waiting: 2, done: 3 };

const Row = ({ item }: { item: UploadItem }) => (
  <li className="direct-upload-row">
    <StatusDot tone={TONE[item.status]} />
    <span className="direct-upload-row__path" title={item.relativePath}>{item.relativePath}</span>
    <span className="direct-upload-row__size">{formatBytes(item.file.size)}</span>
    <span className="direct-upload-row__percent">{item.status === 'sending' ? `${item.percent}%` : ''}</span>
    {item.error && <span className="direct-upload-row__error">{item.error}</span>}
  </li>
);

export const UploadProgress = ({ vm }: { vm: DirectUploadViewModel }) => {
  if (vm.items.length === 0) return null;
  const visible = [...vm.items].sort((a, b) => ORDER[a.status] - ORDER[b.status]).slice(0, MAX_VISIBLE);
  return (
    <section className="direct-upload-list">
      <p className="direct-upload-list__summary" role="status">
        Wybrano {vm.summary.total} ({formatBytes(vm.summary.bytes)}), wysłano {vm.summary.done}, błędy {vm.summary.failed}
        {vm.items.length > MAX_VISIBLE && `. Pokazano ${MAX_VISIBLE}`}
      </p>
      <ul className="direct-upload-list__items">{visible.map((item) => <Row key={item.relativePath} item={item} />)}</ul>
    </section>
  );
};
