import type { ManagedServiceForm } from '../model/ManagedService';
import type { ServicesViewModel } from '../viewmodels/useServicesViewModel';
import { Modal } from '@/shared/ui/Modal';
import { Notice, Spinner } from '@/shared/ui/atoms';

const FIELDS: { key: keyof ManagedServiceForm; label: string; placeholder: string; mono?: boolean }[] = [
  { key: 'name', label: 'Nazwa', placeholder: 'np. Wiki firmowe' },
  { key: 'host', label: 'Adres IP lub host', placeholder: '192.168.1.10', mono: true },
  { key: 'port', label: 'Port', placeholder: '8080', mono: true },
  { key: 'description', label: 'Opis (opcjonalnie)', placeholder: 'Do czego służy' },
];

export const AddServiceModal = ({ vm }: { vm: ServicesViewModel }) => {
  const { form, setForm, isSaving, saveForm, setIsFormOpen, error } = vm.managed;
  const close = () => setIsFormOpen(false);
  const canSave = !isSaving && form.name.trim() !== '' && form.host.trim() !== '' && form.port.trim() !== '';

  return (
    <Modal
      title="Nowy serwis"
      description="Serwis zostanie zapisany na liście. Aplikacja nie łączy się z nim przy dodawaniu."
      onClose={close}
      footer={<>
        <button className="btn btn--ghost" onClick={close} disabled={isSaving}>Anuluj</button>
        <button className="btn btn--primary" onClick={saveForm} disabled={!canSave}>
          {isSaving && <Spinner />}{isSaving ? 'Dodawanie…' : 'Dodaj serwis'}
        </button>
      </>}
    >
      {FIELDS.map((f, i) => (
        <label key={f.key} className="field">
          <span className="field__label">{f.label}</span>
          <input
            className={`input ${f.mono ? 'input--mono' : ''}`}
            autoFocus={i === 0}
            inputMode={f.key === 'port' ? 'numeric' : undefined}
            value={form[f.key]}
            placeholder={f.placeholder}
            onChange={(e) => setForm({ ...form, [f.key]: e.target.value })}
          />
        </label>
      ))}
      {error && <Notice tone="error">{error}</Notice>}
    </Modal>
  );
};
