// src/editor/components/NewProjectModal.tsx
import React, { useState } from 'react';
import { Language } from '@/editor/model';

interface NewProjectModalProps {
  isOpen: boolean;
  onClose: () => void;
  onCreate: (name: string, desc: string, lang: Language) => void;
}

export const NewProjectModal: React.FC<NewProjectModalProps> = ({ isOpen, onClose, onCreate }) => {
  const [name, setName] = useState('');
  const [desc, setDesc] = useState('');
  const [lang, setLang] = useState<Language>('java');

  if (!isOpen) return null;

  const handleCreate = () => {
    if (!name.trim()) return;
    onCreate(name.trim(), desc.trim(), lang);
    setName('');
    setDesc('');
    setLang('java');
    onClose();
  };

  return (
    <div className="modal-backdrop open">
      <div className="modal">
        <div className="modal-title">Nowy projekt</div>
        <div className="modal-sub">Utwórz nową przestrzeń roboczą</div>

        <div className="form-group">
          <label className="form-label">Nazwa projektu</label>
          <input
            className="form-input"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="mój-projekt"
            onKeyDown={(e) => e.key === 'Enter' && handleCreate()}
            autoFocus
          />
        </div>

        <div className="form-group">
          <label className="form-label">Opis (opcjonalnie)</label>
          <input
            className="form-input"
            value={desc}
            onChange={(e) => setDesc(e.target.value)}
            placeholder="Krótki opis projektu..."
          />
        </div>

        <div className="form-group">
          <label className="form-label">Język</label>
          <select
            className="form-select"
            value={lang}
            onChange={(e) => setLang(e.target.value as Language)}
          >
            <option value="java">Java</option>
            <option value="py">Python</option>
            <option value="cpp">C++</option>
            <option value="sh">Bash</option>
          </select>
        </div>

        <div className="modal-footer">
          <button className="btn-cancel" onClick={onClose}>Anuluj</button>
          <button className="btn-confirm" onClick={handleCreate}>Utwórz</button>
        </div>
      </div>
    </div>
  );
};