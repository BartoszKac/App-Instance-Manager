import { LanguageType, RemoteExecutable } from './RemoteExecutable';

const API_BASE = 'http://localhost:8888/api/process';

export interface UploadResult {
  success: boolean;
  message: string;
  data?: RemoteExecutable;
}

export const remoteExecutableRepository = {
  async upload(
    file: File,
    language: LanguageType,
    onProgress?: (percent: number) => void
  ): Promise<UploadResult> {
    return new Promise((resolve, reject) => {
      const formData = new FormData();
      formData.append('file', file);

      const xhr = new XMLHttpRequest();

      xhr.upload.addEventListener('progress', (e) => {
        if (e.lengthComputable && onProgress) {
          onProgress(Math.round((e.loaded / e.total) * 100));
        }
      });

      xhr.addEventListener('load', () => {
        if (xhr.status === 201) {
          resolve({
            success: true,
            message: xhr.responseText,
            data: { name: file.name, language },
          });
        } else {
          resolve({
            success: false,
            message: xhr.responseText || `HTTP ${xhr.status}`,
          });
        }
      });

      xhr.addEventListener('error', () => {
        reject(new Error('Brak połączenia z serwerem.'));
      });

      xhr.open('POST', `${API_BASE}/upload?language=${language}`);
      xhr.send(formData);
    });
  },
};