import { useState, useCallback, useRef } from 'react';
import { LanguageType, UploadState } from '../model/RemoteExecutable';
import { remoteExecutableRepository } from '../model/remoteExecutableRepository';

const INITIAL_STATE: UploadState = {
  status: 'idle',
  progress: 0,
  message: null,
  uploadedFile: null,
};

export const useUploadViewModel = () => {
  const [selectedFile, setSelectedFile]         = useState<File | null>(null);
  const [selectedLanguage, setSelectedLanguage] = useState<LanguageType>(LanguageType.UNKNOWN);
  const [uploadState, setUploadState]           = useState<UploadState>(INITIAL_STATE);
  const [isDragging, setIsDragging]             = useState(false);
  const fileInputRef                            = useRef<HTMLInputElement>(null);

  const selectFile = useCallback((file: File) => {
    setSelectedFile(file);
    setUploadState(INITIAL_STATE);
  }, []);

  const handleDrop = useCallback(
    (e: React.DragEvent<HTMLDivElement>) => {
      e.preventDefault();
      setIsDragging(false);
      const file = e.dataTransfer.files[0];
      if (file) selectFile(file);
    },
    [selectFile]
  );

  const handleDragOver = useCallback((e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(true);
  }, []);

  const handleDragLeave = useCallback(() => setIsDragging(false), []);

  const handleFileInputChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      const file = e.target.files?.[0];
      if (file) selectFile(file);
    },
    [selectFile]
  );

  const openFilePicker = useCallback(() => {
    fileInputRef.current?.click();
  }, []);

  const upload = useCallback(async () => {
    if (!selectedFile) return;

    setUploadState({ status: 'uploading', progress: 0, message: null, uploadedFile: null });

    try {
      const result = await remoteExecutableRepository.upload(
        selectedFile,
        selectedLanguage,
        (progress) => setUploadState((prev) => ({ ...prev, progress }))
      );

      if (result.success) {
        setUploadState({
          status: 'success',
          progress: 100,
          message: result.message,
          uploadedFile: result.data ?? null,
        });
      } else {
        setUploadState({
          status: 'error',
          progress: 0,
          message: result.message,
          uploadedFile: null,
        });
      }
    } catch (err) {
      setUploadState({
        status: 'error',
        progress: 0,
        message: err instanceof Error ? err.message : 'Nieznany błąd.',
        uploadedFile: null,
      });
    }
  }, [selectedFile, selectedLanguage]);

  const reset = useCallback(() => {
    setSelectedFile(null);
    setUploadState(INITIAL_STATE);
    if (fileInputRef.current) fileInputRef.current.value = '';
  }, []);

  return {
    selectedFile,
    selectedLanguage,
    setSelectedLanguage,
    uploadState,
    isDragging,
    fileInputRef,
    handleDrop,
    handleDragOver,
    handleDragLeave,
    handleFileInputChange,
    openFilePicker,
    upload,
    reset,
  };
};