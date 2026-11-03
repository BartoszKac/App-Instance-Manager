import { UploadError, uploadFile } from '../api/transferApi';
import type { UploadItem, UploadTarget } from '../model/DirectUpload';

const PARALLEL_UPLOADS = 3;
const BUSY_STATUS = 503;
const BUSY_RETRIES = 20;
const BUSY_DELAY_MS = 3000;

export interface UploadQueueHandle {
  stop: () => void;
  done: Promise<void>;
}

type ItemPatch = Partial<Pick<UploadItem, 'status' | 'percent' | 'error'>>;

interface QueueRequest {
  items: UploadItem[];
  target: UploadTarget;
  onChange: (index: number, patch: ItemPatch) => void;
}

const wait = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

const failureMessage = (err: unknown) => (err instanceof Error ? err.message : 'Wysyłka się nie udała.');

export const startUploadQueue = ({ items, target, onChange }: QueueRequest): UploadQueueHandle => {
  const active = new Set<() => void>();
  let stopped = false;
  let nextIndex = 0;

  const attempt = async (index: number, retriesLeft: number): Promise<void> => {
    const item = items[index];
    onChange(index, { status: 'sending', percent: 0, error: null });
    const call = uploadFile({ target, relativePath: item.relativePath, file: item.file, onProgress: (percent) => onChange(index, { percent }) });
    active.add(call.abort);
    try {
      await call.promise;
      onChange(index, { status: 'done', percent: 100 });
    } catch (err: unknown) {
      const isBusy = err instanceof UploadError && err.status === BUSY_STATUS;
      if (!isBusy || retriesLeft === 0 || stopped) {
        onChange(index, { status: 'failed', error: failureMessage(err) });
        return;
      }
      await wait(BUSY_DELAY_MS);
      await attempt(index, retriesLeft - 1);
    } finally {
      active.delete(call.abort);
    }
  };

  const worker = async () => {
    while (!stopped && nextIndex < items.length) {
      const index = nextIndex;
      nextIndex += 1;
      await attempt(index, BUSY_RETRIES);
    }
  };

  const done = Promise.all(Array.from({ length: PARALLEL_UPLOADS }, worker)).then(() => undefined);
  const stop = () => {
    stopped = true;
    active.forEach((abort) => abort());
  };
  return { stop, done };
};
