import { useCallback, useState } from 'react';
import type { EditorState, ProjectFile } from '../model/EditorState';

const initialState = (files: ProjectFile[]): EditorState => {
  const first = files[0] ?? null;
  return { activeFile: first, openTabs: first ? [first] : [] };
};

export const useEditor = (initialFiles: ProjectFile[] = []) => {
  const [state, setState] = useState<EditorState>(() => initialState(initialFiles));

  const openFile = useCallback((file: ProjectFile) => {
    setState((prev) => ({
      activeFile: file,
      openTabs: prev.openTabs.some((t) => t.name === file.name) ? prev.openTabs : [...prev.openTabs, file],
    }));
  }, []);

  const closeTab = useCallback((fileName: string) => {
    setState((prev) => {
      const openTabs = prev.openTabs.filter((t) => t.name !== fileName);
      const activeFile = prev.activeFile?.name === fileName ? openTabs[openTabs.length - 1] ?? null : prev.activeFile;
      return { openTabs, activeFile };
    });
  }, []);

  const reset = useCallback((files: ProjectFile[]) => setState(initialState(files)), []);

  return { state, openFile, closeTab, reset };
};
