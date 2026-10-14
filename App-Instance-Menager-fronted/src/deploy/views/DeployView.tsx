import { useDeployViewModel } from '@/deploy/viewmodel';
import { FilesPanel, CenterPanel, ServersPanel } from '@/deploy/components';
import { ScreenHeader } from '@/shared/ui/ScreenHeader';
import { Icon } from '@/shared/ui/Icon';
import { Spinner } from '@/shared/ui/atoms';

const plural = (n: number, one: string, few: string, many: string) =>
  n === 1 ? one : n % 10 >= 2 && n % 10 <= 4 && (n < 10 || n > 20) ? few : many;

export const DeployView = () => {
  const vm = useDeployViewModel();
  const files = vm.selectedFilesList.length;
  const servers = vm.selectedServersList.length;
  const route = `${files} ${plural(files, 'plik', 'pliki', 'plików')} na ${servers} ${plural(servers, 'serwer', 'serwery', 'serwerów')}`;

  return (
    <>
      <ScreenHeader
        title="Wysyłka"
        context={vm.loading ? 'Wczytywanie plików i serwerów…' : 'Zaznacz pliki po lewej i serwery po prawej, potem wyślij.'}
        actions={
          <>
            <span className="deploy-route" aria-live="polite">{route}</span>
            <button className="btn btn--primary" onClick={vm.deploy} disabled={!vm.canDeploy || vm.deploying}>
              {vm.deploying ? <Spinner /> : <Icon name="ship" />}
              {vm.deploying ? 'Wysyłanie…' : 'Wyślij'}
            </button>
          </>
        }
      />
      <div className="deploy" data-enter="body">
        <FilesPanel vm={vm} />
        <CenterPanel vm={vm} />
        <ServersPanel vm={vm} />
      </div>
    </>
  );
};
