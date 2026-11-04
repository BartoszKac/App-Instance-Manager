import { useState } from 'react';
import type { Language } from '@/editor/model';
import { Modal } from '@/shared/ui/Modal';

interface NewProjectModalProps {
  onClose: () => void;
  onCreate: (name: string, desc: string, lang: Language) => void;
}

const LANGS: { value: Language; label: string }[] = [
  { value: 'java', label: 'Java' },
  { value: 'py', label: 'Python' },
  { value: 'cpp', label: 'C++' },
  { value: 'sh', label: 'Bash' },
];

export const NewProjectModal = ({ onClose, onCreate }: NewProjectModalProps) => {
  const [name, setName] = useState('');
  const [desc, setDesc] = useState('');
  const [lang, setLang] = useState<Language>('java');
  const canCreate = /^[A-Za-z0-9_-]+$/.test(name.trim());

  const create = () => {
    if (!canCreate) return;
    onCreate(name.trim(), desc.trim(), lang);
    onClose();
  };

  return (
    <Modal
      title="Nowy projekt"
      description="Projekt dostanie pierwszy plik o tej samej nazwie."
      onClose={onClose}
      footer={<>
        <button className="btn btn--ghost" onClick={onClose}>Anuluj</button>
        <button className="btn btn--primary" onClick={create} disabled={!canCreate}>Utwórz projekt</button>
      </>}
    >
      <label className="field">
        <span className="field__label">Nazwa</span>
        <input className="input" value={name} autoFocus placeholder="Main" onChange={(e) => setName(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && create()} />
        <span className="field__hint">Litery, cyfry, „_” i „-”. Bez spacji.</span>
      </label>
      <label className="field">
        <span className="field__label">Opis</span>
        <input className="input" value={desc} placeholder="Opcjonalnie" onChange={(e) => setDesc(e.target.value)} />
      </label>
      <div className="field">
        <span className="field__label" id="new-project-lang">Język</span>
        <div className="segmented" role="group" aria-labelledby="new-project-lang">
          {LANGS.map((l) => (
            <button key={l.value} type="button" className="segmented__item" aria-pressed={lang === l.value} onClick={() => setLang(l.value)}>{l.label}</button>
          ))}
        </div>
      </div>
    </Modal>
  );
};
