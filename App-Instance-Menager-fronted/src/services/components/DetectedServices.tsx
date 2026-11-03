import { DeviceMore, DeviceSummary } from '@/shared/network/DeviceSummary';
import { ScanProgress } from '@/shared/network/ScanProgress';
import type { ScanJob } from '@/shared/network/model';
import type { DetectedServiceRow } from '../model/ManagedService';
import type { ServicesViewModel } from '../viewmodels/useServicesViewModel';
import { EmptyState } from '@/shared/ui/atoms';

type Props = { vm: ServicesViewModel };

const RowAction = ({ row, vm }: { row: DetectedServiceRow; vm: ServicesViewModel }) => {
  if (vm.savedKeys.has(row.key)) return <span className="services-table__saved">Zapisany</span>;
  return <button className="btn btn--quiet btn--sm" onClick={() => vm.saveDetected(row)}>Zapisz</button>;
};

const DetectedRow = ({ row, vm }: { row: DetectedServiceRow; vm: ServicesViewModel }) => (
  <tr>
    <td><span className="services-table__mono">{row.ip}</span>{row.hostname && <span className="services-table__sub">{row.hostname}</span>}</td>
    <td className="services-table__mono">{row.port}</td>
    <td>{row.service}</td>
    <td>{row.product}</td>
    <td>{row.title}</td>
    <td><RowAction row={row} vm={vm} /></td>
  </tr>
);

const Table = ({ vm }: Props) => (
  <div className="services-table__wrap">
    <table className="services-table">
      <thead><tr><th>Urządzenie</th><th>Port</th><th>Usługa</th><th>Produkt i wersja</th><th>Tytuł lub certyfikat</th><th><span className="sr-only">Akcja</span></th></tr></thead>
      <tbody>{vm.rows.map((row) => <DetectedRow key={row.key} row={row} vm={vm} />)}</tbody>
    </table>
  </div>
);

const Devices = ({ job }: { job: ScanJob }) => (
  <ul className="service-list">
    {job.devices.map((device) => {
      const details = job.details[device.ip];
      return (
        <li key={device.ip} className="service-card service-card--device">
          <DeviceSummary device={device} details={details} isPending={job.state === 'RUNNING' && (job.stage !== 'DETAILS' || device.openPorts.length > 0)} />
          <DeviceMore details={details} />
        </li>
      );
    })}
  </ul>
);

export const DetectedServices = ({ vm }: Props) => {
  const job = vm.lan.job;
  return (
    <section className="services__section" aria-label="Wyniki skanu sieci">
      <h2 className="services__title">Wykryte w sieci</h2>
      {!job && <EmptyState title="Nie ma jeszcze wyników" hint="Wybierz podsieć u góry i kliknij Skanuj." />}
      {job && <ScanProgress job={job} />}
      {job && <p className="scan-summary" aria-live="polite">Urządzenia: <strong>{job.deviceCount}</strong>, usługi: <strong>{vm.rows.length}</strong></p>}
      {job && <Devices job={job} />}
      {vm.rows.length > 0 && <Table vm={vm} />}
    </section>
  );
};
