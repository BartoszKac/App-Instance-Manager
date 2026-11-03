import type { DiscoveredDevice } from '../model/NetworkScan';
import type { NetworkScanViewModel } from '../viewmodel/useNetworkScan';
import { Modal } from '@/shared/ui/Modal';
import { Icon } from '@/shared/ui/Icon';
import { Checkbox, EmptyState, Notice, Spinner } from '@/shared/ui/atoms';

type ScanProps = { scan: NetworkScanViewModel };

const HELPER_HINT = 'Lista zawiera urządzenia, które odpowiedziały na ping lub ARP. Uśpione urządzenie może się nie pojawić.';
const DOCKER_HINT = 'Lista zawiera tylko urządzenia z otwartym typowym portem. Urządzenie uśpione lub za firewallem może się nie pojawić, a kolejny skan może dać inny wynik.';

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
      {scan.isScanning ? (scan.usesHelper ? 'Skanowanie…' : 'Skanowanie (do 4 min)…') : 'Skanuj'}
    </button>
  </div>
);

const deviceDetails = (device: DiscoveredDevice) =>
  [device.hostname, device.vendor].filter(Boolean).join(' · ') || 'Bez nazwy';

const DeviceRow = ({ device, scan }: { device: DiscoveredDevice; scan: NetworkScanViewModel }) => {
  const known = scan.knownIps.has(device.ip);
  return (
    <li className="pick-row" data-checked={scan.picked.has(device.ip)}>
      <label className="pick-row__label">
        {!known && <Checkbox checked={scan.picked.has(device.ip)} onChange={() => scan.togglePicked(device.ip)} label={`Wybierz urządzenie ${device.ip}`} />}
        <span className="pick-row__server">
          <span className="pick-row__address">{device.ip}</span>
          <span className="scan-row__details">{deviceDetails(device)}</span>
        </span>
      </label>
      {known && <span className="pick-row__os">Już dodany</span>}
      {!known && device.sshOpen && <span className="scan-row__ssh">SSH</span>}
    </li>
  );
};

const ScanResult = ({ scan }: ScanProps) => {
  if (!scan.result) return null;
  return (
    <>
      <p className="scan-summary" aria-live="polite">Znalezione urządzenia: <strong>{scan.result.deviceCount}</strong> w sieci {scan.result.cidr}</p>
      <ul className="pick-group__list scan-list">
        {scan.result.devices.map((device) => <DeviceRow key={device.ip} device={device} scan={scan} />)}
      </ul>
      <p className="field__hint">{scan.usesHelper ? HELPER_HINT : DOCKER_HINT}</p>
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
  <EmptyState title="Brak podsieci do skanowania" hint="Skan obsługuje prywatne podsieci /24 lub mniejsze na kartach sieciowych komputera z backendem." />
);

const ScanBody = ({ scan }: ScanProps) => {
  if (scan.isLoadingSubnets) return <p className="deploy-panel__loading"><Spinner />Wykrywanie podsieci…</p>;
  if (scan.subnets.length === 0) return scan.error ? null : <NoSubnets />;
  return (
    <>
      {!scan.usesHelper && <Notice tone="warn">Szybki skan jest wyłączony. Uruchom Start.bat. Bez niego skan w Dockerze trwa kilka minut i widzi mniej urządzeń.</Notice>}
      <SubnetPicker scan={scan} />
      <ScanResult scan={scan} />
      {scan.picked.size > 0 && scan.result && <SharedCredentials scan={scan} />}
    </>
  );
};

export const NetworkScanModal = ({ scan }: ScanProps) => (
  <Modal
    title="Skanuj sieć"
    description="Skanuj tylko sieć, którą zarządzasz lub na której skan masz zgodę."
    onClose={scan.close}
    footer={<>
      <button className="btn btn--ghost" onClick={scan.close} disabled={scan.isAdding}>Anuluj</button>
      <button className="btn btn--primary" onClick={scan.addPicked} disabled={!scan.canAdd}>
        {scan.isAdding && <Spinner />}{scan.isAdding ? 'Dodawanie…' : `Dodaj zaznaczone (${scan.picked.size})`}
      </button>
    </>}
  >
    <ScanBody scan={scan} />
    {scan.error && <Notice tone="error">{scan.error}</Notice>}
  </Modal>
);
