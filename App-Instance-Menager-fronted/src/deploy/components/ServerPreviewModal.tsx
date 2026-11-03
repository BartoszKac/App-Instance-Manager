import { DeviceFacts } from '@/shared/network/DeviceFacts';
import type { ServerPreviewViewModel } from '../viewmodel/useServerPreview';
import { Modal } from '@/shared/ui/Modal';
import { EmptyState, Notice, Spinner } from '@/shared/ui/atoms';

type Props = { preview: ServerPreviewViewModel };

const Inventory = ({ preview }: Props) => {
  const { inventory, isLoadingInventory, inventoryError } = preview;
  return (
    <section className="server-preview__section">
      <h3 className="server-preview__title">System przez SSH</h3>
      {isLoadingInventory && <p className="deploy-panel__loading"><Spinner />Łączenie z serwerem…</p>}
      {inventoryError && <Notice tone="error">{inventoryError}</Notice>}
      {inventory?.error && <Notice tone="warn">{inventory.error}</Notice>}
      {inventory && inventory.sections.length > 0 && (
        <dl className="server-preview__facts">
          {inventory.sections.map((section) => (
            <div key={section.key} className="server-preview__fact">
              <dt className="server-preview__label">{section.label}</dt>
              <dd className="server-preview__value">{section.value}</dd>
            </div>
          ))}
        </dl>
      )}
    </section>
  );
};

const NetworkInfo = ({ preview }: Props) => {
  const { network, isLoadingNetwork, networkError } = preview;
  const isEmpty = network && network.services.length === 0;
  return (
    <section className="server-preview__section">
      <h3 className="server-preview__title">Usługi widoczne z sieci</h3>
      {isLoadingNetwork && <p className="deploy-panel__loading"><Spinner />Skanowanie portów i usług, do około 2 minut…</p>}
      {networkError && <Notice tone="warn">{networkError}</Notice>}
      {network?.osName && <p className="server-preview__hint">System wg usług: {network.osName}</p>}
      {isEmpty && <EmptyState title="Nie znaleziono otwartych portów" hint="Sprawdzono 1000 najczęstszych portów." />}
      {network && !isEmpty && <DeviceFacts details={network} />}
    </section>
  );
};

export const ServerPreviewModal = ({ preview }: Props) => {
  if (!preview.server) return null;
  return (
    <Modal
      title={preview.server.name || 'Bez nazwy'}
      description={`${preview.server.user}@${preview.server.ip}`}
      onClose={preview.close}
      footer={<button className="btn btn--ghost" onClick={preview.close}>Zamknij</button>}
    >
      <div className="server-preview">
        <Inventory preview={preview} />
        <NetworkInfo preview={preview} />
      </div>
    </Modal>
  );
};
