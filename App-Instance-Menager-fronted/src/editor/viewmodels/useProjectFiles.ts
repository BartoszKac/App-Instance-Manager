import { useEffect, useMemo, useState } from 'react';
import type { Project, ProjectFile, SourceCode } from '../model/EditorState';
import { LANGUAGE_TO_EXT } from '../model/EditorState';
import { CodeService } from '../api/CodeService';
import { useEditor } from './useEditor';

const toProjectFile = (sc: SourceCode): ProjectFile => ({
  id: sc.id,
  name: sc.name,
  ext: LANGUAGE_TO_EXT[sc.language] ?? 'txt',
  content: sc.code,
  language: sc.language,
});

export const useProjectFiles = (project: Project) => {
  const [files, setFiles] = useState<ProjectFile[]>(project.files);
  const [dirty, setDirty] = useState(false);
  const [persisted, setPersisted] = useState(false);
  const editor = useEditor(project.files);
  const { reset } = editor;

  useEffect(() => {
    let cancelled = false;
    CodeService.getSourceCodes(project.id)
      .then((sources) => {
        if (cancelled || sources.length === 0) return;
        const loaded = sources.map(toProjectFile);
        setFiles(loaded);
        reset(loaded);
        setPersisted(true);
      })
      .catch(() => undefined);
    return () => { cancelled = true; };
  }, [project.id, reset]);

  const activeFile = useMemo(
    () => files.find((f) => f.name === editor.state.activeFile?.name) ?? null,
    [files, editor.state.activeFile],
  );

  const update = (next: (prev: ProjectFile[]) => ProjectFile[]) => {
    setFiles(next);
    setDirty(true);
  };

  const addFile = (file: ProjectFile) => {
    update((prev) => [...prev, file]);
    editor.openFile(file);
  };

  const removeFile = (name: string) => {
    update((prev) => prev.filter((f) => f.name !== name));
    editor.closeTab(name);
  };

  const changeContent = (content: string) => {
    if (!activeFile) return;
    update((prev) => prev.map((f) => (f.name === activeFile.name ? { ...f, content } : f)));
  };

  const markSaved = () => { setDirty(false); setPersisted(true); };

  return { files, activeFile, dirty, persisted, tabs: editor.state.openTabs, openFile: editor.openFile, closeTab: editor.closeTab, addFile, removeFile, changeContent, markSaved };
};
