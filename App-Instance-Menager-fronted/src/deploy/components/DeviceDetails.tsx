import { PORT_LABEL } from '@/shared/network/model';
import type { DeviceDetails as Details, DiscoveredDevice, DiscoveredService } from '@/shared/network/model';
import { Spinner } from '@/shared/ui/atoms';

const joined = (parts: (string | null | undefined)[]) => parts.filter(Boolean).join(' · ');

const serviceLabel = (service: DiscoveredService) =>
  joined([PORT_LABEL[service.port] ?? String(service.port), service.product, service.version, service.title]);

const PortChips = ({ ports }: { ports: number[] }) => (
  <span className="scan-row__ports">
    {ports.map((port) => <span key={port} className="scan-row__port">{PORT_LABEL[port] ?? port}</span>)}
  </span>
);

const ServiceLines = ({ services }: { services: DiscoveredService[] }) => (
  <>
    {services.map((service) => (
      <span key={service.port} className="scan-row__details scan-row__details--mono">{serviceLabel(service)}</span>
    ))}
  </>
);

interface DeviceDetailsProps {
  device: DiscoveredDevice;
  details?: Details;
  isLoading: boolean;
}

export const DeviceDetails = ({ device, details, isLoading }: DeviceDetailsProps) => {
  const summary = joined([details?.deviceType, details?.osName, device.macAddress, device.vendor]);
  const hasServices = (details?.services.length ?? 0) > 0;
  return (
    <span className="pick-row__server">
      <span className="pick-row__name pick-row__name--plain">{details?.hostname ?? device.hostname ?? device.ip}</span>
      <span className="pick-row__address">{device.ip}</span>
      {summary && <span className="scan-row__details">{summary}</span>}
      {isLoading && <span className="scan-row__details"><Spinner /> Doczytywanie szczegółów…</span>}
      {hasServices ? <ServiceLines services={details!.services} /> : device.openPorts.length > 0 && <PortChips ports={device.openPorts} />}
      {device.openPorts.length === 0 && <span className="scan-row__details">Odpowiada na ping, brak otwartych typowych portów</span>}
    </span>
  );
};
