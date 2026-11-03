import { PORT_LABEL } from './model';
import type { DeviceDetails, DiscoveredService, ScriptResult } from './model';

const joined = (parts: (string | null | undefined)[], separator = ' · ') => parts.filter(Boolean).join(separator);

const serviceTitle = (service: DiscoveredService) =>
  joined([`${service.port} ${PORT_LABEL[service.port] ?? service.name ?? ''}`.trim(), joined([service.product, service.version], ' ')]);

const ScriptOutputs = ({ scripts }: { scripts: ScriptResult[] }) => (
  <>
    {scripts.map((script) => (
      <div key={script.id} className="device-facts__script">
        <span className="device-facts__script-id">{script.id}</span>
        <pre className="device-facts__pre">{script.output}</pre>
      </div>
    ))}
  </>
);

const ServiceFacts = ({ service }: { service: DiscoveredService }) => (
  <li className="device-facts__service">
    <strong className="device-facts__service-title">{serviceTitle(service)}</strong>
    {service.title && <span>Strona: {service.title}</span>}
    {service.tlsSubject && <span>Certyfikat: {service.tlsSubject}</span>}
    {service.extraInfo && <span>{service.extraInfo}</span>}
    {service.cpe.length > 0 && <span className="device-facts__mono">{service.cpe.join(', ')}</span>}
    <ScriptOutputs scripts={service.scripts} />
  </li>
);

export const DeviceFacts = ({ details }: { details: DeviceDetails }) => (
  <div className="device-facts">
    <ul className="device-facts__services">
      {details.services.map((service) => <ServiceFacts key={service.port} service={service} />)}
    </ul>
    <ScriptOutputs scripts={details.hostScripts} />
  </div>
);
