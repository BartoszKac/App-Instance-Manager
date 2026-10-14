import { UPLOAD_STRATEGIES, STRATEGY_LABEL } from '../model/DeployState';
import type { DeployFile, UploadStrategyType } from '../model/DeployState';
import type { DeployViewModel } from '../viewmodel/useDeployViewModel';
import { Checkbox, Notice, Spinner } from '@/shared/ui/atoms';
import { langClass } from '@/shared/lang';

interface FileRowProps {
  file: DeployFile;
  checked: boolean;
  onToggle: () => void;
  onStrategy: (s: UploadStrategyType) => void;
}

const FileRow = ({ file, checked, onToggle, onStrategy }: FileRowProps) => (
  <li className="pick-row" data-checked={checked}>
    <label className={`pick-row__label ${langClass(file.language)}`}>
      <Checkbox checked={checked} onChange={onToggle} label={`Wybierz ${file.name}`} />
      <span className="pick-row__swatch" aria-hidden="true" />
      <span className="pick-row__name">{file.name}</span>
    </label>
    <select className="pick-row__select" value={file.strategy} aria-label={`Co wysłać z ${file.name}`} onChange={(e) => onStrategy(e.target.value as UploadStrategyType)}>
      {UPLOAD_STRATEGIES.map((s) => <option key={s} value={s}>{STRATEGY_LABEL[s]}</option>)}
    </select>
  </li>
);

const Group = ({ title, files, vm }: { title: string; files: DeployFile[]; vm: DeployViewModel }) =>
  files.length === 0 ? null : (
    <section className="pick-group">
      <h3 className="pick-group__title">{title}</h3>
      <ul className="pick-group__list">
        {files.map((f) => (
          <FileRow key={f.key} file={f} checked={vm.selectedFiles.has(f.key)} onToggle={() => vm.toggleFile(f.key)} onStrategy={(s) => vm.setFileStrategy(f.key, s)} />
        ))}
      </ul>
    </section>
  );

export const FilesPanel = ({ vm }: { vm: DeployViewModel }) => {
  const all = vm.files.length > 0 && vm.selectedFiles.size === vm.files.length;
  return (
    <aside className="deploy-panel" aria-label="Pliki do wysłania">
      <div className="deploy-panel__head">
        <label className="deploy-panel__all">
          <Checkbox checked={all} indeterminate={vm.selectedFiles.size > 0} onChange={vm.toggleAllFiles} label="Zaznacz wszystkie pliki" />
          Pliki
        </label>
        <span className="deploy-panel__count">{vm.selectedFiles.size} z {vm.files.length}</span>
      </div>
      <div className="deploy-panel__body">
        {vm.loading && <p className="deploy-panel__loading"><Spinner />Wczytywanie plików…</p>}
        {vm.loadError && <Notice tone="error">{vm.loadError}</Notice>}
        {!vm.loading && !vm.loadError && vm.files.length === 0 && <p className="deploy-panel__empty">Brak plików. Zapisz projekt albo prześlij plik.</p>}
        <Group title="Projekty" files={vm.files.filter((f) => f.source === 'source')} vm={vm} />
        <Group title="Przesłane pliki" files={vm.files.filter((f) => f.source === 'process')} vm={vm} />
      </div>
    </aside>
  );
};
