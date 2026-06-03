// src/editor/components/TabBar.tsx
import React from 'react';
import { ProjectFile } from '@/editor/model';

interface TabBarProps {
  tabs: ProjectFile[];
  activeFile: ProjectFile | null;
  onTabClick: (file: ProjectFile) => void;
  onTabClose: (fileName: string) => void;
}

export const TabBar: React.FC<TabBarProps> = ({
  tabs,
  activeFile,
  onTabClick,
  onTabClose,
}) => {
  return (
    <div className="editor-tabs">
      {tabs.map((file) => (
        <div
          key={file.name}
          className={`tab ${activeFile?.name === file.name ? 'active' : ''}`}
          onClick={() => onTabClick(file)}
        >
          <span style={{ fontSize: 10, fontFamily: 'var(--mono)', color: 'var(--text3)' }}>
            .{file.ext}
          </span>
          {file.name}
          <span
            className="tab-close"
            onClick={(e) => {
              e.stopPropagation();
              onTabClose(file.name);
            }}
          >
            <svg width="9" height="9" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
              <path d="M18 6 6 18M6 6l12 12" />
            </svg>
          </span>
        </div>
      ))}
    </div>
  );
};
