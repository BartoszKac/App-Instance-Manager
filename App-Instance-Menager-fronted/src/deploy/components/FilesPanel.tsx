// src/deploy/components/FilesPanel.tsx
import React from 'react';
import { DeployProject, EXT_COLOR } from '@/deploy/model/DeployState';
import { DeployViewModel } from '@/deploy/viewmodel';
import { Checkbox, LangBadge } from '@/deploy/components';

interface FilesPanelProps {
  vm: DeployViewModel;
}

const fileKey = (projId: number, name: string) => `${projId}::${name}`;

export const FilesPanel: React.FC<FilesPanelProps> = ({ vm }) => {
  const {
    projects,
    selectedFiles,
    collapsedProjects,
    toggleFile,
    toggleAllProjectFiles,
    toggleCollapseProject,
  } = vm;

  return (
    <aside className="sidebar sidebar-deploy">
      <div className="sidebar-header">
        <span className="sidebar-project-name">Pliki źródłowe</span>
        <span className="sidebar-badge">{selectedFiles.size} wybrano</span>
      </div>

      <div className="sidebar-section">Projekty</div>

      {/* list */}
      <div className="file-list" style={{ flex: 1, overflowY: 'auto' }}>
        {projects.map((proj: DeployProject) => {
          const keys        = proj.files.map((f) => fileKey(proj.id, f.name));
          const checkedCount = keys.filter((k) => selectedFiles.has(k)).length;
          const allChecked  = checkedCount === keys.length && keys.length > 0;
          const someChecked = checkedCount > 0 && !allChecked;
          const collapsed   = collapsedProjects.has(proj.id);

          return (
            <div key={proj.id}>
              <div 
                className="file-item project-group" 
                style={{ 
                  borderLeft: `4px solid ${EXT_COLOR[proj.lang] || 'var(--accent)'}`,
                  background: 'var(--bg3)'
                }}
              >
                <Checkbox
                  checked={allChecked}
                  indeterminate={someChecked}
                  onChange={() => toggleAllProjectFiles(proj)}
                />
                <div
                  onClick={() => toggleCollapseProject(proj.id)}
                  className="project-group-title"
                >
                  <LangBadge lang={proj.lang} />
                  <span className="file-name">{proj.name}</span>
                  <svg
                    width="10" height="10" viewBox="0 0 24 24"
                    fill="none" stroke="var(--text3)" strokeWidth="2.5"
                    className={`collapse-icon ${collapsed ? 'collapsed' : ''}`}
                  >
                    <path d="M6 9l6 6 6-6" />
                  </svg>
                </div>
              </div>

              {/* file rows */}
              {!collapsed && proj.files.map((file) => {
                const k       = fileKey(proj.id, file.name);
                const checked = selectedFiles.has(k);
                return (
                  <div
                    key={file.name}
                    className={`file-item file-item-deploy ${checked ? 'active' : ''}`}
                    onClick={() => toggleFile(proj.id, file.name)}
                    style={{ 
                      background: checked ? `${EXT_COLOR[file.ext] || '#4f7ef7'}15` : 'transparent'
                    }}
                  >
                    <Checkbox checked={checked} onChange={() => toggleFile(proj.id, file.name)} />
                    <span 
                      className={`file-icon file-ext-${file.ext}`}
                      style={{ color: EXT_COLOR[file.ext], fontWeight: 600 }}
                    >
                      .{file.ext}
                    </span>
                    <span className="file-name">{file.name}</span>
                  </div>
                );
              })}
            </div>
          );
        })}
      </div>
    </aside>
  );
};
