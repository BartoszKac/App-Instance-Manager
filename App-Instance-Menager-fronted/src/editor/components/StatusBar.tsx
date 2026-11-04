import type { ProjectFile } from '@/editor/model';
import { LANG_LABEL, toLangKey } from '@/shared/lang';

interface StatusBarProps {
  file: ProjectFile | null;
  dirty: boolean;
}

const linesLabel = (n: number) => (n === 1 ? '1 linia' : n % 10 >= 2 && n % 10 <= 4 && (n < 10 || n > 20) ? `${n} linie` : `${n} linii`);

export const StatusBar = ({ file, dirty }: StatusBarProps) => {
  const lines = file ? (file.content || '').split('\n').length : 0;
  return (
    <footer className="statusbar">
      <span>{file ? LANG_LABEL[toLangKey(file.ext)] : 'Brak pliku'}</span>
      {file && <span>{linesLabel(lines)}</span>}
      <span className="statusbar__spacer" />
      {dirty && <span className="statusbar__dirty">Niezapisane</span>}
      <span>UTF-8</span>
    </footer>
  );
};
