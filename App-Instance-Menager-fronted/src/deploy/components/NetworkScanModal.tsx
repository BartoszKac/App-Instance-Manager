import type { DiscoveredDevice } from '@/shared/network/model';
import { DeviceMore, DeviceSummary } from '@/shared/network/DeviceSummary';
import { ScanProgress } from '@/shared/network/ScanProgress';
import type { NetworkScanViewModel } from '../viewmodel/useNetworkScan';
import { Modal } from '@/shared/ui/Modal';
import { Icon } from '@/shared/ui/Icon';
import { Checkbox, EmptyState, Notice, Spinner } from '@/shared/ui/atoms';

type ScanProps = { scan: NetworkScanViewModel };

const SubnetPicker = ({ scan }: ScanProps) => (
  <div className="scan-picker">
    <label className="field scan-picker__field">
      <span className="field__label">Podsieć</span>
      <select className="select input--mono" value={scan.cidr} onChange={(e) => scan.setCidr(e.target.value)} disabled={scan.isScanning}>
        {scan.subnets.map((subnet) => <option key={subnet} value={subnet}>{subnet}</option>)}
      </select>
    </label>
    <button className="btn btn--primary" onClick={scan.scan} disabled={scan.isScanning || !scan.cidr}>
      {scan.isScanning ? <Spinner /> : <Icon name="search" size={16} />}
      {scan.isScanning ? 'Skanowanie…' : 'Skanuj'}
    </button>
  </div>
);

const DeviceRow = ({ device, scan }: { device: DiscoveredDevice; scan: NetworkScanViewModel }) => {
  const known = scan.knownIps.has(device.ip);
  const details = scan.job?.details[device.ip];
  return (
    <li className="scan-device">
      <div className="pick-row" data-checked={scan.isPicked(device)}>
        <label className="pick-row__label">
          {!known && <Checkbox checked={scan.isPicked(device)} onChange={() => scan.togglePicked(device)} label={`Wybierz urządzenie ${device.ip}`} />}
          <DeviceSummary device={device} details={details} isPending={scan.isPending(device)} />
        </label>
        {known && <span className="pick-row__os">Już dodany</span>}
      </div>
      <DeviceMore details={details} />
    </li>
  );
};

const ScanResult = ({ scan }: ScanProps) => {
  if (!scan.job) return null;
  return (
    <>
      <ScanProgress job={scan.job} />
      <p className="scan-summary" aria-live="polite">Znalezione urządzenia: <strong>{scan.job.deviceCount}</strong> w sieci {scan.job.cidr}</p>
      <ul className="pick-group__list scan-list">
        {scan.job.devices.map((device) => <DeviceRow key={device.ip} device={device} scan={scan} />)}
      </ul>
      <p className="field__hint">Lista zawiera urządzenia, które odpowiedziały na ping. Urządzenie uśpione lub blokujące ping może się nie pojawić.</p>
    </>
  );
};

const SharedCredentials = ({ scan }: ScanProps) => (
  <div className="scan-credentials">
    <label className="field">
      <span className="field__label">Wspólny użytkownik SSH</span>
      <input className="input input--mono" value={scan.credentials.user} onChange={(e) => scan.setCredentials({ ...scan.credentials, user: e.target.value })} />
    </label>
    <label className="field">
      <span className="field__label">Wspólne hasło SSH</span>
      <input className="input" type="password" autoComplete="off" value={scan.credentials.pass} onChange={(e) => scan.setCredentials({ ...scan.credentials, pass: e.target.value })} />
    </label>
  </div>
);

const NoSubnets = () => (
  <EmptyState title="Brak podsieci do skanowania" hint="Ustaw AIM_SCAN_SUBNETS w docker-compose. Skan obsługuje prywatne podsieci /24 lub mniejsze." />
);

const ScanBody = ({ scan }: ScanProps) => {
  if (scan.isLoadingSubnets) return <p className="deploy-panel__loading"><Spinner />Wykrywanie podsieci…</p>;
  if (scan.subnets.length === 0) return scan.error ? null : <NoSubnets />;
  return (
    <>
      <SubnetPicker scan={scan} />
      <ScanResult scan={scan} />
      {scan.pickedCount > 0 && <SharedCredentials scan={scan} />}
    </>
  );
};

export const NetworkScanModal = ({ scan }: ScanProps) => (
  <Modal
    title="Skanuj sieć"
    description="Skanuj tylko sieć, którą zarządzasz lub na której skan masz zgodę."
    onClose={scan.close}
    footer={<>
      <button className="btn btn--ghost" onClick={scan.close} disabled={scan.isAdding}>Zamknij</button>
      <button className="btn btn--primary" onClick={scan.addPicked} disabled={!scan.canAdd}>
        {scan.isAdding && <Spinner />}{scan.isAdding ? 'Dodawanie…' : `Dodaj zaznaczone (${scan.pickedCount})`}
      </button>
    </>}
  >
    <ScanBody scan={scan} />
    {scan.error && <Notice tone="error">{scan.error}</Notice>}
  </Modal>
);
