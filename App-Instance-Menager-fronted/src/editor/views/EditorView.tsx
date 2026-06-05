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
  const { state, openFile, closeTab } = useEditor(project.files);

  const [files, setFiles] = React.useState(project.files);

  const activeFileWithContent = useMemo(() => {
    if (!state.activeFile) return null;
    // Pobieramy plik z lokalnego stanu 'files', aby mieć aktualną treść podczas pisania
    return files.find(f => f.name === state.activeFile?.name) || state.activeFile;
  }, [state.activeFile, files]);

  const lineCount = useMemo(() => {
    if (!activeFileWithContent) return 0;
    const code = activeFileWithContent.content || SAMPLE_CODE[activeFileWithContent.ext] || '';
    return code.split('\n').length;
  }, [activeFileWithContent]);

  const handleFileAdd = (file: ProjectFile) => {
    setFiles((prev) => [...prev, file]);
    openFile(file);
  };

  const handleFileRemove = (fileName: string) => {
    setFiles((prev) => prev.filter((f) => f.name !== fileName));
    closeTab(fileName);
  };

  const handleContentChange = (newContent: string) => {
    console.log('[EditorView] handleContentChange wywołane dla pliku:', state.activeFile?.name);
    if (!state.activeFile) return;
    setFiles((prev) => {
      const newFiles = prev.map((f) => (f.name === state.activeFile?.name ? { ...f, content: newContent } : f));
      console.log('[EditorView] Nowy stan tablicy plików:', newFiles);
      return newFiles;
    });
  };

  const handleClearFile = () => {
    handleContentChange('');
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
            <button className="nav-btn secondary" onClick={handleClearFile}>Wyczyść</button>
            <button className="nav-btn secondary" onClick={onDeploy}>Uruchom</button>
            <button className="nav-btn primary" onClick={() => console.log('Zapisywanie plików...', files)}>Zapisz</button>
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
          <CodeView file={activeFileWithContent} onContentChange={handleContentChange} />
          <StatusBar file={activeFileWithContent} lineCount={lineCount} />
        </div>
      </div>
    </div>
  );
};
