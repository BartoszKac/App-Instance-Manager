// src/editor/views/EditorView.tsx
import React, { useMemo, useState, useCallback, useEffect } from 'react';
import { Project, ProjectFile, SourceCode, EXT_TO_LANGUAGE, LANGUAGE_TO_EXT } from '@/editor/model';
import { useEditor } from '@/editor/viewmodels';
import { CodeService } from '@/editor/components/CodeService';
import { Topbar, Sidebar, TabBar, CodeView, StatusBar } from '@/editor/components';

interface EditorViewProps {
  project: Project;
  onBack: () => void;
  onDeploy: () => void;
  onExecution: () => void;
}

export const EditorView: React.FC<EditorViewProps> = ({ project, onBack, onDeploy, onExecution }) => {
  const [files, setFiles]         = useState<ProjectFile[]>(project.files);
  const [saving, setSaving]       = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  const { state, openFile, closeTab, reset } = useEditor(files);

  const idManClass = project.id;

  // Jeśli projekt pochodzi z backendu — załaduj aktualne pliki
  useEffect(() => {
    const load = async () => {
      try {
        const sourceCodes = await CodeService.getSourceCodes(idManClass);
        const loaded: ProjectFile[] = sourceCodes.map(sc => ({
          id: sc.id,
          name: sc.name,
          ext: LANGUAGE_TO_EXT[sc.language] ?? 'java',
          content: sc.code,
          language: sc.language,
        }));
        setFiles(loaded);
        reset(loaded);
      } catch {
        // Nowy projekt — zostają pliki z project.files
      }
    };
    load();
  }, [idManClass]);

  const activeFileWithContent = useMemo(() => {
    if (!state.activeFile) return null;
    return files.find(f => f.name === state.activeFile?.name) || state.activeFile;
  }, [state.activeFile, files]);

  const lineCount = useMemo(() => {
    if (!activeFileWithContent) return 0;
    return (activeFileWithContent.content || '').split('\n').length;
  }, [activeFileWithContent]);

  const handleFileAdd = (file: ProjectFile) => {
    setFiles(prev => [...prev, file]);
    openFile(file);
  };

  const handleFileRemove = (fileName: string) => {
    setFiles(prev => prev.filter(f => f.name !== fileName));
    closeTab(fileName);
  };

  const handleContentChange = (newContent: string) => {
    if (!state.activeFile) return;
    setFiles(prev =>
      prev.map(f => f.name === state.activeFile?.name ? { ...f, content: newContent } : f)
    );
  };

  const handleClearFile = () => handleContentChange('');

  const toSourceCode = (f: ProjectFile): SourceCode => ({
    id: f.id,
    idManClass,
    name: f.name,
    code: f.content || '',
    language: f.language || EXT_TO_LANGUAGE[f.ext] || 'UNKNOWN',
  });

  // ── ZAPIS NA BACKEND ─────────────────────────────────────────────
  const handleSave = useCallback(async () => {
    setSaving(true);
    setSaveError(null);

    try {
      const newFiles      = files.filter(f => f.id === 0);
      const existingFiles = files.filter(f => f.id > 0);

      // POST — nowe pliki
      if (newFiles.length > 0) {
        const payload = newFiles.map(toSourceCode);
        await CodeService.saveSourceCodes(payload);
        setFiles(prev => prev.map(f =>
          f.id === 0 ? { ...f, id: idManClass } : f
        ));
      }

      // PUT — istniejące pliki
      for (const f of existingFiles) {
        await CodeService.updateSourceCode(toSourceCode(f));
      }

      console.log('✅ Zapisano | idManClass:', idManClass);
    } catch (err: any) {
      setSaveError(err.message || 'Błąd zapisu');
      console.error('❌', err);
    } finally {
      setSaving(false);
    }
  }, [files, idManClass]);

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
            {saveError && (
              <span style={{ color: 'var(--error, #f87171)', fontSize: 12 }}>{saveError}</span>
            )}
            <button className="nav-btn secondary" onClick={handleClearFile}>Wyczyść</button>
            <button className="nav-btn secondary" onClick={onDeploy}>Uruchom</button>
            <button className="nav-btn primary" onClick={handleSave} disabled={saving}>
              {saving ? 'Zapisywanie...' : 'Zapisz'}
            </button>
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