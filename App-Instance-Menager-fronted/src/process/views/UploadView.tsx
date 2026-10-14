import { LanguageType } from '../model/RemoteExecutable';
import type { UploadState } from '../model/RemoteExecutable';
import { useUploadViewModel } from '../viewmodel/useUploadViewModel';
import { ScreenHeader } from '@/shared/ui/ScreenHeader';
import { Icon } from '@/shared/ui/Icon';
import { Notice, Spinner } from '@/shared/ui/atoms';

const KINDS: { value: LanguageType; label: string }[] = [
  { value: LanguageType.JAVA, label: 'Java' },
  { value: LanguageType.PYTHON, label: 'Python' },
  { value: LanguageType.CPP, label: 'C++' },
  { value: LanguageType.BASH, label: 'Bash' },
  { value: LanguageType.UNKNOWN, label: 'Inny' },
];

const formatSize = (bytes: number) => (bytes < 1024 * 1024 ? `${(bytes / 1024).toFixed(1)} KB` : `${(bytes / 1024 / 1024).toFixed(1)} MB`);

const Result = ({ state }: { state: UploadState }) => {
  if (state.status === 'success') return <Notice tone="ok"><Icon name="check" />{state.message ?? 'Plik przesłany. Znajdziesz go w kroku Wysyłka.'}</Notice>;
  if (state.status === 'error') return <Notice tone="error"><Icon name="warning" />{state.message ?? 'Nie udało się przesłać pliku.'}</Notice>;
  return null;
};

export const UploadView = () => {
  const {
    selectedFile: file, selectedLanguage, setSelectedLanguage, uploadState, isDragging, fileInputRef,
    handleDrop, handleDragOver, handleDragLeave, handleFileInputChange, openFilePicker, upload, reset,
  } = useUploadViewModel();
  const uploading = uploadState.status === 'uploading';

  return (
    <>
      <ScreenHeader title="Plik" context="Prześlij gotowy program lub skrypt, żeby wysłać go na serwery bez edytora." />
      <div className="upload" data-enter="body">
        <div
          className="dropzone"
          data-dragging={isDragging}
          data-filled={Boolean(file)}
          onDrop={handleDrop}
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
        >
          <input ref={fileInputRef} type="file" className="visually-hidden" tabIndex={-1} onChange={handleFileInputChange} />
          {file ? (
            <div className="dropzone__file">
              <Icon name="file" size={28} />
              <span className="dropzone__name">{file.name}</span>
              <span className="dropzone__size">{formatSize(file.size)}</span>
              <button className="btn btn--ghost btn--sm" onClick={() => { reset(); openFilePicker(); }} disabled={uploading}>Zmień plik</button>
            </div>
          ) : (
            <button className="dropzone__prompt" onClick={openFilePicker}>
              <Icon name="upload" size={32} />
              <span className="dropzone__title">{isDragging ? 'Upuść, żeby dodać' : 'Przeciągnij plik tutaj'}</span>
              <span className="dropzone__hint">albo kliknij, żeby wybrać z dysku. Do 150 MB.</span>
            </button>
          )}
        </div>
        <div className="field">
          <span className="field__label" id="upload-kind">Rodzaj pliku</span>
          <div className="segmented" role="group" aria-labelledby="upload-kind">
            {KINDS.map((k) => (
              <button key={k.value} className="segmented__item" aria-pressed={selectedLanguage === k.value} disabled={uploading} onClick={() => setSelectedLanguage(k.value)}>{k.label}</button>
            ))}
          </div>
        </div>
        {uploading && (
          <div className="progress" role="progressbar" aria-valuenow={uploadState.progress} aria-valuemin={0} aria-valuemax={100} aria-label="Postęp przesyłania">
            <span className="progress__fill" style={{ width: `${uploadState.progress}%` }} />
          </div>
        )}
        <Result state={uploadState} />
        <div className="upload__actions">
          {uploadState.status === 'success'
            ? <button className="btn btn--quiet" onClick={reset}>Prześlij kolejny plik</button>
            : <button className="btn btn--primary" onClick={upload} disabled={!file || uploading}>{uploading ? <Spinner /> : <Icon name="upload" />}{uploading ? 'Przesyłanie…' : 'Prześlij plik'}</button>}
        </div>
      </div>
    </>
  );
};
