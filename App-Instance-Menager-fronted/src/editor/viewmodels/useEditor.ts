import { useState } from 'react';
import { EditorState, ProjectFile } from '../model/EditorState';

export const useEditor = (initialFiles: ProjectFile[] = []) => {
  const firstFile = initialFiles[0] ?? null;
  const [state, setState] = useState<EditorState>({
    activeFile: firstFile,
    openTabs: firstFile ? [firstFile] : [],
  });

  const openFile = (file: ProjectFile) => {
    setState((prev) => {
      const alreadyOpen = prev.openTabs.find((t) => t.name === file.name);
      return {
        activeFile: file,
        openTabs: alreadyOpen ? prev.openTabs : [...prev.openTabs, file],
      };
    });
  };

  const closeTab = (fileName: string) => {
    setState((prev) => {
      const newTabs = prev.openTabs.filter((t) => t.name !== fileName);
      const isActive = prev.activeFile?.name === fileName;
      const newActive = isActive
        ? newTabs[newTabs.length - 1] ?? null
        : prev.activeFile;
      return { openTabs: newTabs, activeFile: newActive };
    });
  };

  const reset = (files: ProjectFile[]) => {
    const first = files[0] ?? null;
    setState({
      activeFile: first,
      openTabs: first ? [first] : [],
    });
  };

  return { state, openFile, closeTab, reset };
};
