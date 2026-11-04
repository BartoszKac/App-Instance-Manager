import { EmptyState, Spinner } from '@/shared/ui/atoms';
import type { FilePage } from '../model/Inspection';
import { DeniedBadge, FileMeta } from './FileRow';

interface FileSearchResultsProps {
  results: FilePage | null;
  isSearching: boolean;
}

export const FileSearchResults = ({ results, isSearching }: FileSearchResultsProps) => {
  if (!results) return isSearching ? <p className="file-tree__hint"><Spinner />Szukanie…</p> : null;
  if (results.total === 0) return <EmptyState title="Nic nie znaleziono" hint="Spróbuj innego fragmentu nazwy lub ścieżki." />;
  return (
    <div className="file-tree">
      <p className="file-tree__hint">
        Znaleziono {results.total.toLocaleString('pl-PL')}
        {results.total > results.entries.length && `, pokazano ${results.entries.length}`}
      </p>
      <ul className="file-tree__group">
        {results.entries.map((file) => (
          <li key={file.path} className="file-row">
            <span className="file-row__main"><span className="file-row__name" title={file.path}>{file.path}</span><DeniedBadge file={file} /></span>
            <FileMeta file={file} />
          </li>
        ))}
      </ul>
    </div>
  );
};
