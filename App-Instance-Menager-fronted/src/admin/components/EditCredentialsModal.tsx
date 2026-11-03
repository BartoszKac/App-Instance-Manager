import { Modal } from '@/shared/ui/Modal';
import { Notice, Spinner } from '@/shared/ui/atoms';
import type { CredentialsForm } from '../viewmodels/useCredentialsForm';

const Footer = ({ form }: { form: CredentialsForm }) => (
  <>
    <button className="btn btn--ghost" onClick={form.close} disabled={form.isSaving}>Anuluj</button>
    <button className="btn btn--primary" onClick={() => void form.save()} disabled={!form.canSave}>
      {form.isSaving && <Spinner />}{form.isSaving ? 'Zapisywanie…' : 'Zapisz i zbadaj'}
    </button>
  </>
);

export const EditCredentialsModal = ({ form, serverName }: { form: CredentialsForm; serverName: string }) => (
  <Modal
    title={`Dane logowania: ${serverName}`}
    description="Zmienia dane zapisane w aplikacji. Hasło na samym serwerze zostaje bez zmian."
    onClose={form.close}
    footer={<Footer form={form} />}
  >
    <label className="field">
      <span className="field__label">Użytkownik SSH</span>
      <input className="input input--mono" value={form.user} onChange={(e) => form.setUser(e.target.value)} />
    </label>
    <label className="field">
      <span className="field__label">Hasło SSH</span>
      <input
        className="input"
        type="password"
        autoFocus
        autoComplete="new-password"
        placeholder="Nowe hasło"
        value={form.password}
        onChange={(e) => form.setPassword(e.target.value)}
        onKeyDown={(e) => { if (e.key === 'Enter' && form.canSave) void form.save(); }}
      />
    </label>
    {form.error && <Notice tone="error">{form.error}</Notice>}
  </Modal>
);
