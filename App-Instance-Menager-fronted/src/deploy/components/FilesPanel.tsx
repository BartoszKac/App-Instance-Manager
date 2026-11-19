import { COMPILE_FIRST, STRATEGY_LABEL } from '../viewmodel/deployRules';
import type { DeployFile, UploadStrategyType } from '../model/DeployState';
import type { DeployViewModel } from '../viewmodel/useDeployViewModel';
import { Checkbox, Notice, Spinner } from '@/shared/ui/atoms';
import { formatBytes, formatIso } from '@/shared/format';
import { langClass } from '@/shared/lang';

interface FileRowProps {
  file: DeployFile;
  checked: boolean;
  onToggle: () => void;
  onStrategy: (s: UploadStrategyType) => void;
}

const optionLabel = (strategy: UploadStrategyType, disabledReason: string | null) =>
  disabledReason ? `${STRATEGY_LABEL[strategy]} — ${disabledReason.toLowerCase()}` : STRATEGY_LABEL[strategy];

const StrategyChoice = ({ file, onStrategy }: Pick<FileRowProps, 'file' | 'onStrategy'>) => {
  if (file.options.length === 1) return <span className="pick-row__fixed">{STRATEGY_LABEL[file.strategy]}</span>;
  return (
    <select className="pick-row__select" value={file.strategy} aria-label={`Co wysłać z ${file.name}`} onChange={(e) => onStrategy(e.target.value as UploadStrategyType)}>
      {file.options.map((o) => <option key={o.strategy} value={o.strategy} disabled={o.disabledReason !== null}>{optionLabel(o.strategy, o.disabledReason)}</option>)}
    </select>
  );
};

const metaOf = (file: DeployFile): string[] => [
  file.description,
  file.sizeBytes !== null ? formatBytes(file.sizeBytes) : null,
  file.uploadedAt ? formatIso(file.uploadedAt) : null,
].filter((part): part is string => Boolean(part));

const FileDetails = ({ file }: { file: DeployFile }) => {
  const meta = metaOf(file);
  const mustCompile = file.options.some((o) => o.disabledReason === COMPILE_FIRST);
  return (
    <span className="pick-row__text">
      <span className="pick-row__name">{file.name}</span>
      {meta.length > 0 && <span className="pick-row__meta">{meta.join(' · ')}</span>}
      {mustCompile && <span className="pick-row__note">Program: {COMPILE_FIRST.toLowerCase()} w kroku Kod</span>}
      <code className="pick-row__hint" title="Jak uruchomić na serwerze">$ {file.runHint}</code>
    </span>
  );
};

const FileRow = ({ file, checked, onToggle, onStrategy }: FileRowProps) => (
  <li className="pick-row pick-row--file" data-checked={checked}>
    <label className={`pick-row__label ${langClass(file.language)}`}>
      <Checkbox checked={checked} onChange={onToggle} label={`Wybierz ${file.name}`} />
      <span className="pick-row__swatch" aria-hidden="true" />
      <FileDetails file={file} />
    </label>
    <StrategyChoice file={file} onStrategy={onStrategy} />
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

const PanelBody = ({ vm }: { vm: DeployViewModel }) => (
  <div className="deploy-panel__body">
    {vm.loading && <p className="deploy-panel__loading"><Spinner />Wczytywanie plików…</p>}
    {vm.loadError && <Notice tone="error">{vm.loadError}</Notice>}
    {!vm.loading && !vm.loadError && vm.files.length === 0 && <p className="deploy-panel__empty">Brak plików. Zapisz projekt albo prześlij plik.</p>}
    <Group title="Projekty" files={vm.files.filter((f) => f.source === 'source')} vm={vm} />
    <Group title="Przesłane pliki" files={vm.files.filter((f) => f.source === 'process')} vm={vm} />
  </div>
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
      <PanelBody vm={vm} />
    </aside>
  );
};
