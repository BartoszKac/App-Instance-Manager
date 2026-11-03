import { useRef } from 'react';
import { Icon } from '@/shared/ui/Icon';
import { Spinner } from '@/shared/ui/atoms';
import type { DirectUploadViewModel } from '../viewmodels/useDirectUpload';

const Pickers = ({ vm }: { vm: DirectUploadViewModel }) => {
  const filesRef = useRef<HTMLInputElement>(null);
  const folderRef = useRef<HTMLInputElement>(null);
  const markAsFolderPicker = (el: HTMLInputElement | null) => {
    folderRef.current = el;
    el?.setAttribute('webkitdirectory', '');
  };
  return (
    <div className="direct-upload-form__row">
      <input ref={filesRef} className="sr-only" type="file" multiple tabIndex={-1} aria-hidden="true" onChange={(e) => vm.choose(e.target.files)} />
      <input ref={markAsFolderPicker} className="sr-only" type="file" tabIndex={-1} aria-hidden="true" onChange={(e) => vm.choose(e.target.files)} />
      <button type="button" className="btn btn--quiet" disabled={vm.isSending} onClick={() => filesRef.current?.click()}><Icon name="file" size={16} />Wybierz pliki</button>
      <button type="button" className="btn btn--quiet" disabled={vm.isSending} onClick={() => folderRef.current?.click()}><Icon name="folder" size={16} />Wybierz katalog</button>
    </div>
  );
};

const Actions = ({ vm }: { vm: DirectUploadViewModel }) => (
  <div className="direct-upload-form__row">
    <button type="button" className="btn btn--primary" disabled={!vm.canSend} onClick={() => void vm.send()}>
      {vm.isSending ? <Spinner /> : <Icon name="upload" size={16} />}{vm.isSending ? 'Wysyłanie…' : 'Wyślij'}
    </button>
    {vm.isSending && <button type="button" className="btn btn--ghost" onClick={vm.stop}>Zatrzymaj</button>}
  </div>
);

export const UploadForm = ({ vm }: { vm: DirectUploadViewModel }) => (
  <div className="direct-upload-form">
    <label className="field">
      <span className="field__label">Katalog docelowy na serwerze</span>
      <input className="input input--mono" placeholder="/opt/app" value={vm.destination} disabled={vm.isSending} onChange={(e) => vm.setDestination(e.target.value)} />
    </label>
    <label className="direct-upload-form__check">
      <input type="checkbox" className="checkbox" checked={vm.overwrite} disabled={vm.isSending} onChange={(e) => vm.setOverwrite(e.target.checked)} />
      Nadpisuj istniejące pliki
    </label>
    <Pickers vm={vm} />
    <Actions vm={vm} />
    <p className="direct-upload-form__hint">Pliki idą prosto na serwer, bez zapisu w aplikacji. Nie zamykaj tej karty ani nie zmieniaj serwera, dopóki wysyłka trwa.</p>
  </div>
);
