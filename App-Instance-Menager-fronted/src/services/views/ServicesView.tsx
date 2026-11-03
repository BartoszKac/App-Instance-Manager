import { useServicesViewModel } from '../viewmodels/useServicesViewModel';
import type { ServicesViewModel } from '../viewmodels/useServicesViewModel';
import { AddServiceModal } from '../components/AddServiceModal';
import { DetectedServices } from '../components/DetectedServices';
import { ManagedServices } from '../components/ManagedServices';
import { ScreenHeader } from '@/shared/ui/ScreenHeader';
import { Icon } from '@/shared/ui/Icon';
import { Notice, Spinner } from '@/shared/ui/atoms';

const HeaderActions = ({ vm }: { vm: ServicesViewModel }) => (
  <>
    <select className="select input--mono services__subnet" aria-label="Podsieć" value={vm.lan.cidr} onChange={(e) => vm.lan.setCidr(e.target.value)} disabled={vm.lan.isScanning}>
      {vm.lan.subnets.map((subnet) => <option key={subnet} value={subnet}>{subnet}</option>)}
    </select>
    <button className="btn btn--quiet" onClick={vm.lan.scan} disabled={vm.lan.isScanning || !vm.lan.cidr}>
      {vm.lan.isScanning ? <Spinner /> : <Icon name="search" size={16} />}
      {vm.lan.isScanning ? 'Skanowanie…' : 'Skanuj'}
    </button>
    <button className="btn btn--primary" onClick={() => vm.managed.setIsFormOpen(true)}><Icon name="add" size={16} />Dodaj serwis</button>
  </>
);

export const ServicesView = () => {
  const vm = useServicesViewModel();
  const pageError = vm.lan.error ?? (vm.managed.isFormOpen ? null : vm.managed.error);

  return (
    <>
      <ScreenHeader title="Serwisy" context="Usługi działające w sieci i Twoja własna lista." actions={<HeaderActions vm={vm} />} />
      <div className="services" data-enter="body">
        {pageError && <Notice tone="error">{pageError}</Notice>}
        <ManagedServices vm={vm} />
        <DetectedServices vm={vm} />
      </div>
      {vm.managed.isFormOpen && <AddServiceModal vm={vm} />}
    </>
  );
};
