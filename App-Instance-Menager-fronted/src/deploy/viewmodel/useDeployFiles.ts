import { useCallback, useMemo, useState } from 'react';
import type { DeployableFiles } from '@/shared/deployable/model';
import type { UploadStrategyType } from '../model/DeployState';
import { toDeployFiles } from './deployRules';

const toggled = (set: Set<string>, key: string) => {
  const next = new Set(set);
  if (next.has(key)) next.delete(key); else next.add(key);
  return next;
};

export const useDeployFiles = (onSelectionChange: () => void) => {
  const [deployable, setDeployable] = useState<DeployableFiles | null>(null);
  const [selectedFiles, setSelectedFiles] = useState<Set<string>>(new Set());
  const [fileStrategies, setFileStrategies] = useState<Record<string, UploadStrategyType>>({});

  const files = useMemo(() => toDeployFiles(deployable, fileStrategies), [deployable, fileStrategies]);
  const selectedFilesList = useMemo(() => files.filter((f) => selectedFiles.has(f.key)), [files, selectedFiles]);

  const setFileStrategy = useCallback((key: string, strategy: UploadStrategyType) => {
    setFileStrategies((prev) => ({ ...prev, [key]: strategy }));
    onSelectionChange();
  }, [onSelectionChange]);

  const toggleFile = useCallback((key: string) => {
    setSelectedFiles((prev) => toggled(prev, key));
    onSelectionChange();
  }, [onSelectionChange]);

  const toggleAllFiles = useCallback(() => {
    setSelectedFiles((prev) => (prev.size === files.length && files.length > 0 ? new Set() : new Set(files.map((f) => f.key))));
    onSelectionChange();
  }, [files, onSelectionChange]);

  return { files, selectedFiles, selectedFilesList, setDeployable, setFileStrategy, toggleFile, toggleAllFiles };
};
