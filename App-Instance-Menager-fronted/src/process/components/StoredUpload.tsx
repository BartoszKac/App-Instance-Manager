import { LanguageType } from '../model/RemoteExecutable';
import type { UploadState } from '../model/RemoteExecutable';
import { useUploadViewModel } from '../viewmodel/useUploadViewModel';
import { Icon } from '@/shared/ui/Icon';
import { Notice, Spinner } from '@/shared/ui/atoms';

type UploadViewModel = Omit<ReturnType<typeof useUploadViewModel>, 'fileInputRef'>;

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

const SelectedFile = ({ vm, file }: { vm: UploadViewModel; file: File }) => (
  <div className="dropzone__file">
    <Icon name="file" size={28} />
    <span className="dropzone__name">{file.name}</span>
    <span className="dropzone__size">{formatSize(file.size)}</span>
    <button className="btn btn--ghost btn--sm" onClick={() => { vm.reset(); vm.openFilePicker(); }} disabled={vm.uploadState.status === 'uploading'}>Zmień plik</button>
  </div>
);

const Dropzone = ({ vm, inputRef }: { vm: UploadViewModel; inputRef: React.RefObject<HTMLInputElement | null> }) => (
  <div className="dropzone" data-dragging={vm.isDragging} data-filled={Boolean(vm.selectedFile)} onDrop={vm.handleDrop} onDragOver={vm.handleDragOver} onDragLeave={vm.handleDragLeave}>
    <input ref={inputRef} type="file" className="visually-hidden" tabIndex={-1} onChange={vm.handleFileInputChange} />
    {vm.selectedFile ? <SelectedFile vm={vm} file={vm.selectedFile} /> : (
      <button className="dropzone__prompt" onClick={vm.openFilePicker}>
        <Icon name="upload" size={32} />
        <span className="dropzone__title">{vm.isDragging ? 'Upuść, żeby dodać' : 'Przeciągnij plik tutaj'}</span>
        <span className="dropzone__hint">albo kliknij, żeby wybrać z dysku. Do 150 MB.</span>
      </button>
    )}
  </div>
);

const KindPicker = ({ vm }: { vm: UploadViewModel }) => (
  <div className="field">
    <span className="field__label" id="upload-kind">Rodzaj pliku</span>
    <div className="segmented" role="group" aria-labelledby="upload-kind">
      {KINDS.map((k) => (
        <button key={k.value} className="segmented__item" aria-pressed={vm.selectedLanguage === k.value} disabled={vm.uploadState.status === 'uploading'} onClick={() => vm.setSelectedLanguage(k.value)}>{k.label}</button>
      ))}
    </div>
  </div>
);

const Actions = ({ vm }: { vm: UploadViewModel }) => {
  const uploading = vm.uploadState.status === 'uploading';
  return (
    <div className="upload__actions">
      {vm.uploadState.status === 'success'
        ? <button className="btn btn--quiet" onClick={vm.reset}>Prześlij kolejny plik</button>
        : <button className="btn btn--primary" onClick={vm.upload} disabled={!vm.selectedFile || uploading}>{uploading ? <Spinner /> : <Icon name="upload" />}{uploading ? 'Przesyłanie…' : 'Prześlij plik'}</button>}
    </div>
  );
};

export const StoredUpload = () => {
  const { fileInputRef, ...vm } = useUploadViewModel();
  return (
    <>
      <Dropzone vm={vm} inputRef={fileInputRef} />
      <KindPicker vm={vm} />
      {vm.uploadState.status === 'uploading' && (
        <div className="progress" role="progressbar" aria-valuenow={vm.uploadState.progress} aria-valuemin={0} aria-valuemax={100} aria-label="Postęp przesyłania">
          <span className="progress__fill" style={{ width: `${vm.uploadState.progress}%` }} />
        </div>
      )}
      <Result state={vm.uploadState} />
      <Actions vm={vm} />
    </>
  );
};
