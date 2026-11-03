import type { ManagedService } from '../model/ManagedService';
import type { ServicesViewModel } from '../viewmodels/useServicesViewModel';
import { EmptyState, Spinner } from '@/shared/ui/atoms';
import { Icon } from '@/shared/ui/Icon';

type Props = { vm: ServicesViewModel };

const ServiceCard = ({ service, onRemove }: { service: ManagedService; onRemove: () => void }) => (
  <li className="service-card">
    <span className="service-card__body">
      <span className="service-card__name">{service.name}</span>
      <span className="services-table__mono">{service.host}:{service.port}</span>
      {service.description && <span className="services-table__sub">{service.description}</span>}
    </span>
    <button className="icon-btn icon-btn--danger" onClick={onRemove} aria-label={`Usuń serwis ${service.name}`}>
      <Icon name="trash" size={16} />
    </button>
  </li>
);

export const ManagedServices = ({ vm }: Props) => (
  <section className="services__section" aria-label="Twoje serwisy">
    <h2 className="services__title">Twoje serwisy</h2>
    {vm.managed.isLoading && <p className="deploy-panel__loading"><Spinner />Wczytywanie serwisów…</p>}
    {!vm.managed.isLoading && vm.managed.services.length === 0 && (
      <EmptyState title="Nie ma jeszcze serwisów" hint="Dodaj własny albo zapisz usługę wykrytą w sieci." />
    )}
    <ul className="service-list">
      {vm.managed.services.map((service) => (
        <ServiceCard key={service.id} service={service} onRemove={() => vm.managed.remove(service.id)} />
      ))}
    </ul>
  </section>
);
