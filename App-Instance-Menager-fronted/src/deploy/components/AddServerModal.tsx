import type { NewServerForm } from '../model/DeployState';
import type { DeployViewModel } from '../viewmodel/useDeployViewModel';
import { Modal } from '@/shared/ui/Modal';
import { Notice, Spinner } from '@/shared/ui/atoms';

const FIELDS: { key: keyof NewServerForm; label: string; placeholder: string; type: string; mono?: boolean }[] = [
  { key: 'name', label: 'Nazwa', placeholder: 'np. Serwer testowy', type: 'text' },
  { key: 'host', label: 'Adres IP lub host', placeholder: '192.168.1.10', type: 'text', mono: true },
  { key: 'user', label: 'Użytkownik SSH', placeholder: 'deploy', type: 'text', mono: true },
  { key: 'pass', label: 'Hasło SSH', placeholder: 'Hasło', type: 'password' },
];

export const AddServerModal = ({ vm }: { vm: DeployViewModel }) => {
  const close = () => vm.setShowAddServer(false);
  const canAdd = !vm.addingServer && vm.newServer.name.trim() !== '' && vm.newServer.host.trim() !== '';

  return (
    <Modal
      title="Nowy serwer"
      description="Aplikacja połączy się z nim przez SSH, żeby wysyłać pliki."
      onClose={close}
      footer={<>
        <button className="btn btn--ghost" onClick={close} disabled={vm.addingServer}>Anuluj</button>
        <button className="btn btn--primary" onClick={vm.addServer} disabled={!canAdd}>
          {vm.addingServer && <Spinner />}{vm.addingServer ? 'Dodawanie…' : 'Dodaj serwer'}
        </button>
      </>}
    >
      {FIELDS.map((f, i) => (
        <label key={f.key} className="field">
          <span className="field__label">{f.label}</span>
          <input
            className={`input ${f.mono ? 'input--mono' : ''}`}
            type={f.type}
            autoFocus={i === 0}
            value={vm.newServer[f.key]}
            placeholder={f.placeholder}
            onChange={(e) => vm.setNewServer((s) => ({ ...s, [f.key]: e.target.value }))}
            onKeyDown={(e) => { if (e.key === 'Enter' && canAdd) vm.addServer(); }}
          />
        </label>
      ))}
      {vm.addServerError && <Notice tone="error">{vm.addServerError}</Notice>}
    </Modal>
  );
};
