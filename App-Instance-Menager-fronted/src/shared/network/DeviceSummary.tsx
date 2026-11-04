import { DeviceFacts } from './DeviceFacts';
import { PORT_LABEL } from './model';
import type { DeviceDetails, DiscoveredDevice } from './model';

const joined = (parts: (string | null | undefined)[]) => parts.filter(Boolean).join(' · ');

const PortChips = ({ ports }: { ports: number[] }) => (
  <span className="scan-row__ports">
    {ports.map((port) => <span key={port} className="scan-row__port">{PORT_LABEL[port] ?? port}</span>)}
  </span>
);

interface DeviceSummaryProps {
  device: DiscoveredDevice;
  details?: DeviceDetails;
  isPending: boolean;
}

export const DeviceSummary = ({ device, details, isPending }: DeviceSummaryProps) => {
  const facts = joined([device.deviceType ?? details?.deviceType, details?.osName, device.macAddress ?? details?.macAddress, device.vendor]);
  return (
    <span className="pick-row__server">
      <span className="pick-row__name pick-row__name--plain">{device.hostname ?? details?.hostname ?? device.ip}</span>
      <span className="pick-row__address">{device.ip}</span>
      {facts && <span className="scan-row__details">{facts}</span>}
      {isPending && <span className="scan-row__details">Doczytywanie szczegółów…</span>}
      {device.openPorts.length > 0 && <PortChips ports={device.openPorts} />}
      {device.openPorts.length === 0 && !isPending && <span className="scan-row__details">Odpowiada na ping, brak otwartych portów z 1000 najczęstszych</span>}
    </span>
  );
};

export const DeviceMore = ({ details }: { details?: DeviceDetails }) => {
  if (!details || (details.services.length === 0 && details.hostScripts.length === 0)) return null;
  return (
    <details className="device-more">
      <summary className="device-more__summary">Wszystkie dane</summary>
      <DeviceFacts details={details} />
    </details>
  );
};
