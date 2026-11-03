export type DirectUploadState = 'IN_PROGRESS' | 'DONE' | 'FAILED';

export interface DirectUploadRecord {
  id: number;
  relativePath: string;
  remotePath: string | null;
  sizeBytes: number;
  sha256: string | null;
  state: DirectUploadState;
  error: string | null;
  startedAt: string;
  finishedAt: string | null;
}

export interface DirectUploadPage {
  entries: DirectUploadRecord[];
  total: number;
}

export type UploadItemStatus = 'waiting' | 'sending' | 'done' | 'failed';

export interface UploadItem {
  relativePath: string;
  file: File;
  status: UploadItemStatus;
  percent: number;
  error: string | null;
}

export interface UploadTarget {
  serverId: number;
  destination: string;
  overwrite: boolean;
}
