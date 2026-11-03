import { useState } from 'react';
import { ScreenHeader } from '@/shared/ui/ScreenHeader';
import { StoredUpload } from '../components/StoredUpload';
import { DirectUploadView } from './DirectUploadView';

type UploadMode = 'direct' | 'stored';

const MODES: { id: UploadMode; label: string }[] = [
  { id: 'direct', label: 'Prosto na serwer' },
  { id: 'stored', label: 'Do aplikacji' },
];

const CONTEXT: Record<UploadMode, string> = {
  direct: 'Wyślij pliki lub cały katalog od razu na serwer. Aplikacja zapamięta tylko informacje o plikach.',
  stored: 'Prześlij gotowy program lub skrypt, żeby wysłać go na serwery bez edytora.',
};

interface UploadViewProps {
  isActive: boolean;
  onBusyChange: (isBusy: boolean) => void;
}

export const UploadView = ({ isActive, onBusyChange }: UploadViewProps) => {
  const [mode, setMode] = useState<UploadMode>('direct');
  return (
    <>
      <ScreenHeader title="Plik" context={CONTEXT[mode]} />
      <div className="upload" data-enter="body">
        <div className="segmented" role="group" aria-label="Dokąd wysłać">
          {MODES.map((m) => <button key={m.id} type="button" className="segmented__item" aria-pressed={mode === m.id} onClick={() => setMode(m.id)}>{m.label}</button>)}
        </div>
        <div className="upload__mode" hidden={mode !== 'direct'}><DirectUploadView isActive={isActive} onBusyChange={onBusyChange} /></div>
        {mode === 'stored' && <StoredUpload />}
      </div>
    </>
  );
};
