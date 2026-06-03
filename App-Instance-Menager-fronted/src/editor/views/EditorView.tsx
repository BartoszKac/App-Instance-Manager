// src/editor/views/EditorView.tsx
import React, { useMemo } from 'react';
import { Project, ProjectFile, SAMPLE_CODE } from '@/editor/model';
import { useEditor } from '@/editor/viewmodels';
import { Topbar, Sidebar, TabBar, CodeView, StatusBar } from '@/editor/components';

interface EditorViewProps {
  project: Project;
  onBack: () => void;
  onDeploy: () => void;
  onExecution: () => void;
}

export const EditorView: React.FC<EditorViewProps> = ({ project, onBack, onDeploy, onExecution }) => {
  const { state, openFile, closeTab, reset } = useEditor(project.files);

  const [files, setFiles] = React.useState(project.files);

  const lineCount = useMemo(() => {
    if (!state.activeFile) return 0;
    const code = state.activeFile.content || SAMPLE_CODE[state.activeFile.ext] || '';
    return code.split('\n').length;
  }, [state.activeFile]);

  const handleFileAdd = (file: ProjectFile) => {
    setFiles((prev) => [...prev, file]);
    openFile(file);
  };

  const handleFileRemove = (fileName: string) => {
    setFiles((prev) => prev.filter((f) => f.name !== fileName));
    closeTab(fileName);
  };

  const projectWithFiles = { ...project, files };

  return (
    <div className="screen-editor">
      <Topbar
        left={
          <div className="nav-header" style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
            <div className="nav-tabs">
              <button className="nav-tab" onClick={onBack}>Projekty</button>
              <button className="nav-tab" onClick={onDeploy}>Deploy</button>
              <button className="nav-tab" onClick={onExecution}>Instancje</button>
            </div>
            <div className="breadcrumb">
              <span className="breadcrumb-sep">/</span>
              <span className="breadcrumb-cur">{project.name}</span>
            </div>
          </div>
        }
        right={
          <>
            <button className="nav-btn secondary">Uruchom</button>
            <button className="nav-btn primary">Zapisz</button>
          </>
        }
      />

      <div className="editor-layout">
        <Sidebar
          project={projectWithFiles}
          activeFile={state.activeFile}
          onFileClick={openFile}
          onFileRemove={handleFileRemove}
          onFileAdd={handleFileAdd}
        />

        <div className="editor-main">
          <TabBar
            tabs={state.openTabs}
            activeFile={state.activeFile}
            onTabClick={openFile}
            onTabClose={closeTab}
          />
          <CodeView file={state.activeFile} />
          <StatusBar file={state.activeFile} lineCount={lineCount} />
        </div>
      </div>
    </div>
  );
};
