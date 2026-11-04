import { STRATEGY_LABEL, getServerId } from '../model/DeployState';
import type { DeployFile, DeployServer } from '../model/DeployState';
import type { DeployViewModel } from '../viewmodel/useDeployViewModel';
import { langClass } from '@/shared/lang';
import { EmptyState } from '@/shared/ui/atoms';

const LOG_TONES: [string, string][] = [
  ['✅', 'ok'],
  ['✗', 'error'],
  ['⚡', 'warn'],
  ['→', 'signal'],
];

const toneOf = (line: string) => LOG_TONES.find(([marker]) => line.includes(marker))?.[1] ?? 'plain';

const Lane = ({ server, files }: { server: DeployServer; files: DeployFile[] }) => (
  <li className="lane">
    <div className="lane__server">
      <span className="lane__name">{server.name || 'Bez nazwy'}</span>
      <span className="lane__address">{server.user}@{server.ip}</span>
    </div>
    <span className="lane__wire" aria-hidden="true" />
    <ul className="lane__files">
      {files.map((f) => (
        <li key={f.key} className={`lane__file ${langClass(f.language)}`}>
          <span className="lane__file-name">{f.name}</span>
          <span className="lane__file-strategy">{STRATEGY_LABEL[f.strategy]}</span>
        </li>
      ))}
    </ul>
  </li>
);

const DeployLog = ({ lines, running }: { lines: string[]; running: boolean }) => (
  <section className="deploy-log" aria-label="Przebieg wysyłki">
    <h2 className="deploy-center__title">Przebieg</h2>
    <ol className="deploy-log__lines" aria-live="polite">
      {lines.map((line, i) => <li key={i} className={`deploy-log__line deploy-log__line--${toneOf(line)}`}>{line}</li>)}
      {running && <li className="deploy-log__cursor" aria-hidden="true" />}
    </ol>
  </section>
);

export const CenterPanel = ({ vm }: { vm: DeployViewModel }) => {
  const ready = vm.selectedFilesList.length > 0 && vm.selectedServersList.length > 0;
  if (vm.deployLog.length > 0) {
    return <div className="deploy-center"><DeployLog lines={vm.deployLog} running={vm.deploying} /></div>;
  }
  if (!ready) {
    return (
      <div className="deploy-center deploy-center--empty">
        <EmptyState title="Plan wysyłki pojawi się tutaj" hint="Zaznacz co najmniej jeden plik i jeden serwer. Zobaczysz, co trafi na który serwer, zanim cokolwiek wyślesz." />
      </div>
    );
  }
  return (
    <div className="deploy-center">
      <h2 className="deploy-center__title">Plan wysyłki</h2>
      <ul className="lanes">
        {vm.selectedServersList.map((srv) => <Lane key={getServerId(srv)} server={srv} files={vm.selectedFilesList} />)}
      </ul>
    </div>
  );
};
