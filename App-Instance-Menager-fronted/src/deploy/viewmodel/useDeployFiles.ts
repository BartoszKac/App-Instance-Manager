import { useCallback, useMemo, useState } from 'react';
import { defaultStrategy } from '../model/DeployState';
import type { DeployFile, MainClass, Process, UploadStrategyType } from '../model/DeployState';

const toDeployFiles = (
  mainClasses: MainClass[],
  processes: Process[],
  strategies: Record<string, UploadStrategyType>,
): DeployFile[] => [
  ...mainClasses.map((mc) => {
    const key = `source::${mc.idManClass}`;
    return { key, name: mc.name, language: mc.language, source: 'source' as const, id: mc.idManClass, strategy: strategies[key] ?? defaultStrategy(mc.language) };
  }),
  ...processes.map((p) => {
    const key = `process::${p.id}`;
    return { key, name: p.name, language: p.language, source: 'process' as const, id: p.id, strategy: strategies[key] ?? defaultStrategy(p.language) };
  }),
];

export const useDeployFiles = (onSelectionChange: () => void) => {
  const [mainClasses, setMainClasses] = useState<MainClass[]>([]);
  const [processes, setProcesses] = useState<Process[]>([]);
  const [selectedFiles, setSelectedFiles] = useState<Set<string>>(new Set());
  const [fileStrategies, setFileStrategies] = useState<Record<string, UploadStrategyType>>({});

  const files = useMemo(() => toDeployFiles(mainClasses, processes, fileStrategies), [mainClasses, processes, fileStrategies]);
  const selectedFilesList = useMemo(() => files.filter((f) => selectedFiles.has(f.key)), [files, selectedFiles]);

  const setFileStrategy = useCallback((key: string, strategy: UploadStrategyType) => {
    setFileStrategies((prev) => ({ ...prev, [key]: strategy }));
  }, []);

  const toggleFile = useCallback((key: string) => {
    setSelectedFiles((prev) => {
      const next = new Set(prev);
      if (next.has(key)) next.delete(key); else next.add(key);
      return next;
    });
    onSelectionChange();
  }, [onSelectionChange]);

  const toggleAllFiles = useCallback(() => {
    setSelectedFiles((prev) => (prev.size === files.length && files.length > 0 ? new Set() : new Set(files.map((f) => f.key))));
    onSelectionChange();
  }, [files, onSelectionChange]);

  return { files, selectedFiles, selectedFilesList, setMainClasses, setProcesses, setFileStrategy, toggleFile, toggleAllFiles };
};
