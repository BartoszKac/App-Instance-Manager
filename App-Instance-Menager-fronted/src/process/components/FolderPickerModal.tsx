import { Icon } from '@/shared/ui/Icon';
import { Modal } from '@/shared/ui/Modal';
import { Notice, Spinner } from '@/shared/ui/atoms';
import type { InspectedFile } from '@/shared/inspection/model';
import type { RemoteFolders } from '../viewmodel/useRemoteFolders';

const FolderRow = ({ folder, folders }: { folder: InspectedFile; folders: RemoteFolders }) => {
  const isOpen = folders.expanded.has(folder.path);
  return (
    <li>
      <div className="folder-picker__row">
        <button type="button" className="folder-picker__toggle" aria-label={`${isOpen ? 'Zwiń' : 'Rozwiń'} ${folder.name}`} aria-expanded={isOpen} onClick={() => folders.toggle(folder.path)}>
          <Icon name={isOpen ? 'chevronDown' : 'chevronRight'} size={14} />
        </button>
        <button type="button" className="folder-picker__name" aria-pressed={folders.selected === folder.path} disabled={folder.denied} title={folder.path} onClick={() => folders.setSelected(folder.path)}>
          <Icon name="folder" size={16} />{folder.name}
          {folder.denied && <span className="folder-picker__denied">brak dostępu</span>}
        </button>
      </div>
      {isOpen && <FolderList path={folder.path} folders={folders} />}
    </li>
  );
};

const FolderList = ({ path, folders }: { path: string; folders: RemoteFolders }) => {
  const node = folders.nodes[path];
  if (!node) return null;
  return (
    <ul className="folder-picker__list">
      {node.folders.map((folder) => <FolderRow key={folder.path} folder={folder} folders={folders} />)}
      {node.isLoading && <li className="folder-picker__hint"><Spinner />Wczytywanie…</li>}
      {!node.isLoading && node.folders.length === 0 && <li className="folder-picker__hint">Brak podkatalogów</li>}
      {!node.isLoading && node.hasMore && (
        <li><button type="button" className="btn btn--sm btn--ghost" onClick={() => folders.loadMore(path)}>Pokaż więcej</button></li>
      )}
    </ul>
  );
};

export const FolderPickerModal = ({ folders }: { folders: RemoteFolders }) => (
  <Modal
    title="Wybierz folder na serwerze"
    description="Lista pochodzi z ostatniego badania serwera, więc nowsze foldery może pominąć."
    onClose={folders.close}
    footer={<>
      <button className="btn btn--ghost" onClick={folders.close}>Anuluj</button>
      <button className="btn btn--primary" onClick={folders.confirm} disabled={!folders.selected}>Wybierz folder</button>
    </>}
  >
    {folders.error && <Notice tone="error">{folders.error}</Notice>}
    <div className="folder-picker"><FolderList path={folders.rootPath} folders={folders} /></div>
    {folders.selected && <p className="folder-picker__selected">{folders.selected}</p>}
  </Modal>
);
