// src/editor/views/EditorView.tsx
import React, { useMemo, useState, useCallback, useEffect } from 'react';
import { Project, ProjectFile, SourceCode, CompiledCode, EXT_TO_LANGUAGE, LANGUAGE_TO_EXT } from '@/editor/model';
import { useEditor } from '@/editor/viewmodels';
import { CodeService } from '@/editor/components/CodeService';
import { Topbar, Sidebar, TabBar, CodeView, StatusBar } from '@/editor/components';

interface EditorViewProps {
  project: Project;
  onBack: () => void;
  onDeploy: () => void;
  onExecution: () => void;
}

type CompileStatus = 'idle' | 'compiling' | 'success' | 'error';

export const EditorView: React.FC<EditorViewProps> = ({ project, onBack, onDeploy, onExecution }) => {
  const [files, setFiles]             = useState<ProjectFile[]>(project.files);
  const [saving, setSaving]           = useState(false);
  const [saveError, setSaveError]     = useState<string | null>(null);
  const [compileStatus, setCompileStatus] = useState<CompileStatus>('idle');
  const [compileMsg, setCompileMsg]   = useState<string | null>(null);
  const [compiledCodes, setCompiledCodes] = useState<CompiledCode[]>([]);

  const { state, openFile, closeTab, reset } = useEditor(project.files);
  const idManClass = project.id;

  useEffect(() => {
    const load = async () => {
      try {
        const sourceCodes = await CodeService.getSourceCodes(idManClass);
        if (sourceCodes.length > 0) {
          const loaded: ProjectFile[] = sourceCodes.map(sc => ({
            id: sc.id,
            name: sc.name,
            ext: LANGUAGE_TO_EXT[sc.language] ?? 'java',
            content: sc.code,
            language: sc.language,
          }));
          setFiles(loaded);
          reset(loaded);
        } else {
          setFiles(project.files);
          reset(project.files);
        }
      } catch {
        setFiles(project.files);
        reset(project.files);
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

  // ── ZAPIS ────────────────────────────────────────────────────────
  const handleSave = useCallback(async () => {
    setSaving(true);
    setSaveError(null);
    try {
      const payload = files.map(toSourceCode);
      await CodeService.saveSourceCodes(payload);
      console.log('✅ Zapisano | idManClass:', idManClass);
    } catch (err: any) {
      setSaveError(err.message || 'Błąd zapisu');
    } finally {
      setSaving(false);
    }
  }, [files, idManClass]);

  // ── KOMPILACJA ───────────────────────────────────────────────────
  const handleCompile = useCallback(async () => {
    setCompileStatus('compiling');
    setCompileMsg(null);
    setCompiledCodes([]);

    try {
      // 1. Uruchom kompilację
      const msg = await CodeService.compileAll(idManClass);
      console.log('Kompilacja odpowiedź:', msg);

      // 2. Pobierz wyniki
      const results = await CodeService.getCompiledCodes(idManClass);
      setCompiledCodes(results);

      // Sukces jeśli żaden plik nie ma statusu error
      const hasError = results.some(r => r.status === 'error');
      if (hasError) {
        setCompileStatus('error');
        setCompileMsg('Kompilacja zakończona błędami');
      } else {
        setCompileStatus('success');
        setCompileMsg('Kompilacja zakończona sukcesem');
      }
    } catch (err: any) {
      setCompileStatus('error');
      setCompileMsg(err.message || 'Błąd kompilacji');
    }
  }, [idManClass]);

  // ── RUN (na razie puste) ─────────────────────────────────────────
  const handleRun = useCallback(() => {
    console.log('Run | idManClass:', idManClass, '| compiled:', compiledCodes);
    // TODO: podłączyć endpoint uruchamiania
  }, [idManClass, compiledCodes]);

  const projectWithFiles = { ...project, files };

  const compileColor =
    compileStatus === 'success' ? '#4ade80' :
    compileStatus === 'error'   ? '#f87171' :
    'var(--text3)';

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
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            {/* Status kompilacji */}
            {compileMsg && (
              <span style={{ fontSize: 11, color: compileColor }}>{compileMsg}</span>
            )}
            {saveError && (
              <span style={{ fontSize: 11, color: '#f87171' }}>{saveError}</span>
            )}

            <button className="nav-btn secondary" onClick={handleClearFile}>Wyczyść</button>

            <button
              className="nav-btn primary"
              onClick={handleSave}
              disabled={saving}
            >
              {saving ? 'Zapisywanie...' : 'Zapisz'}
            </button>

            <button
              className="nav-btn secondary"
              onClick={handleCompile}
              disabled={compileStatus === 'compiling'}
              style={{ minWidth: 90 }}
            >
              {compileStatus === 'compiling' ? 'Kompilowanie...' : '⚙ Kompiluj'}
            </button>

            {/* Run pojawia się tylko po udanej kompilacji */}
            {compileStatus === 'success' && (
              <button
                className="nav-btn primary"
                onClick={handleRun}
                style={{ background: '#4ade80', color: '#0f1117' }}
              >
                ▶ Run
              </button>
            )}
          </div>
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