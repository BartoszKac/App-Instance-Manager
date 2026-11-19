import { API_BASE_URL } from '@/shared/apiBase';
import type { LanguageType } from '../model/RemoteExecutable';

export interface StoredUploadRequest {
  file: File;
  language: LanguageType;
  description: string;
}

const problemDetail = (xhr: XMLHttpRequest): string => {
  try {
    const body = JSON.parse(xhr.responseText) as { detail?: string };
    return body.detail ?? `Serwer odpowiedział ${xhr.status}.`;
  } catch {
    return xhr.responseText || `Serwer odpowiedział ${xhr.status}.`;
  }
};

const uploadUrl = ({ language, description }: StoredUploadRequest) => {
  const params = new URLSearchParams({ language });
  if (description.trim()) params.set('description', description.trim());
  return `${API_BASE_URL}/api/process/upload?${params.toString()}`;
};

export const uploadStoredFile = (request: StoredUploadRequest, onProgress: (percent: number) => void): Promise<string> =>
  new Promise((resolve, reject) => {
    const formData = new FormData();
    formData.append('file', request.file);
    const xhr = new XMLHttpRequest();
    xhr.upload.addEventListener('progress', (e) => { if (e.lengthComputable) onProgress(Math.round((e.loaded / e.total) * 100)); });
    xhr.addEventListener('load', () => (xhr.status === 201 ? resolve(xhr.responseText) : reject(new Error(problemDetail(xhr)))));
    xhr.addEventListener('error', () => reject(new Error('Brak połączenia z serwerem aplikacji.')));
    xhr.open('POST', uploadUrl(request));
    xhr.send(formData);
  });
