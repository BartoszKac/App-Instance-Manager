import type { ProjectFile } from '@/editor/model';
import { Icon } from '@/shared/ui/Icon';
import { langClass } from '@/shared/lang';

interface TabBarProps {
  tabs: ProjectFile[];
  activeFile: ProjectFile | null;
  onTabClick: (file: ProjectFile) => void;
  onTabClose: (fileName: string) => void;
}

export const TabBar = ({ tabs, activeFile, onTabClick, onTabClose }: TabBarProps) => (
  <div className="tabs" role="tablist" aria-label="Otwarte pliki">
    {tabs.map((file) => {
      const active = activeFile?.name === file.name;
      return (
        <div key={file.name} className={`tab ${langClass(file.ext)}`} data-active={active}>
          <button className="tab__label" role="tab" aria-selected={active} onClick={() => onTabClick(file)}>
            <span className="tab__swatch" aria-hidden="true" />
            {file.name}
          </button>
          <button className="tab__close" onClick={() => onTabClose(file.name)} aria-label={`Zamknij ${file.name}`}>
            <Icon name="close" size={14} />
          </button>
        </div>
      );
    })}
  </div>
);
