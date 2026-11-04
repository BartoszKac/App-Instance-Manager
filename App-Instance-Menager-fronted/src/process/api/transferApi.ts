import { API_BASE_URL } from '@/shared/apiBase';
import { apiFetch } from '@/shared/http';
import type { DirectUploadPage, DirectUploadRecord, UploadTarget } from '../model/DirectUpload';

const BASE = '/api/transfers/servers';
const NETWORK_ERROR = 'Połączenie z backendem zostało przerwane.';

export class UploadError extends Error {
  readonly status: number;

  constructor(status: number, message: string) {
    super(message);
    this.status = status;
  }
}

interface UploadRequest {
  target: UploadTarget;
  relativePath: string;
  file: File;
  onProgress: (percent: number) => void;
}

const readDetail = (xhr: XMLHttpRequest): string => {
  try {
    return (JSON.parse(xhr.responseText) as { detail?: string }).detail ?? `Błąd ${xhr.status}`;
  } catch {
    return `Błąd ${xhr.status}`;
  }
};

const uploadUrl = ({ target, relativePath }: UploadRequest) => {
  const query = new URLSearchParams({
    destination: target.destination,
    relativePath,
    overwrite: String(target.overwrite),
  });
  return `${API_BASE_URL}${BASE}/${target.serverId}/files?${query}`;
};

export const uploadFile = (request: UploadRequest) => {
  const xhr = new XMLHttpRequest();
  const promise = new Promise<DirectUploadRecord>((resolve, reject) => {
    xhr.upload.onprogress = (e) => { if (e.lengthComputable) request.onProgress(Math.round((e.loaded / e.total) * 100)); };
    xhr.onload = () => {
      if (xhr.status >= 200 && xhr.status < 300) resolve(JSON.parse(xhr.responseText) as DirectUploadRecord);
      else reject(new UploadError(xhr.status, readDetail(xhr)));
    };
    xhr.onerror = () => reject(new UploadError(0, NETWORK_ERROR));
    xhr.onabort = () => reject(new UploadError(0, 'Wysyłka zatrzymana.'));
    xhr.open('POST', uploadUrl(request));
    xhr.setRequestHeader('Content-Type', 'application/octet-stream');
    xhr.send(request.file);
  });
  return { promise, abort: () => xhr.abort() };
};

export const fetchUploadHistory = (serverId: number) => apiFetch<DirectUploadPage>(`${BASE}/${serverId}/files`);
