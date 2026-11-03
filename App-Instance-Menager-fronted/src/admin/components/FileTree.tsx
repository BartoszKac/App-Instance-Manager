import { Icon } from '@/shared/ui/Icon';
import type { IconName } from '@/shared/ui/Icon';
import { Spinner } from '@/shared/ui/atoms';
import type { InspectedFile } from '../model/Inspection';
import type { useFileTree } from '../viewmodels/useFileTree';
import { DeniedBadge, FileMeta } from './FileRow';

type Tree = ReturnType<typeof useFileTree>;

const ICON_BY_TYPE: Record<string, IconName> = { d: 'folder', l: 'transfer' };

interface NodeProps {
  file: InspectedFile;
  depth: number;
  tree: Tree;
}

const Name = ({ file, isOpen }: { file: InspectedFile; isOpen: boolean }) => (
  <>
    {file.type === 'd' && <Icon name={isOpen ? 'chevronDown' : 'chevronRight'} size={14} />}
    <Icon name={ICON_BY_TYPE[file.type] ?? 'file'} size={16} />
    <span className="file-row__name" title={file.path}>{file.name}</span>
    <DeniedBadge file={file} />
  </>
);

const Node = ({ file, depth, tree }: NodeProps) => {
  const isDir = file.type === 'd';
  const isOpen = tree.expanded.has(file.path);
  return (
    <li role="treeitem" aria-expanded={isDir ? isOpen : undefined} aria-selected={false}>
      <div className="file-row" style={{ '--depth': depth } as React.CSSProperties}>
        {isDir
          ? <button type="button" className="file-row__main" onClick={() => tree.toggle(file.path)}><Name file={file} isOpen={isOpen} /></button>
          : <span className="file-row__main"><Name file={file} isOpen={false} /></span>}
        <FileMeta file={file} />
      </div>
      {isDir && isOpen && <Folder path={file.path} depth={depth + 1} tree={tree} />}
    </li>
  );
};

const Folder = ({ path, depth, tree }: { path: string; depth: number; tree: Tree }) => {
  const folder = tree.folders[path];
  if (!folder) return null;
  const remaining = folder.total - folder.entries.length;
  return (
    <ul role="group" className="file-tree__group">
      {folder.entries.map((file) => <Node key={file.path} file={file} depth={depth} tree={tree} />)}
      {folder.isLoading && <li className="file-tree__hint" role="none"><Spinner />Wczytywanie…</li>}
      {!folder.isLoading && folder.total === 0 && <li className="file-tree__hint" role="none">Pusty katalog</li>}
      {!folder.isLoading && remaining > 0 && (
        <li role="none">
          <button type="button" className="btn btn--sm btn--ghost file-tree__more" onClick={() => tree.loadMore(path)}>
            Pokaż więcej ({remaining.toLocaleString('pl-PL')})
          </button>
        </li>
      )}
    </ul>
  );
};

export const FileTree = ({ tree }: { tree: Tree }) => (
  <div className="file-tree" role="tree" aria-label="Katalogi serwera">
    <Folder path={tree.rootPath} depth={0} tree={tree} />
  </div>
);
