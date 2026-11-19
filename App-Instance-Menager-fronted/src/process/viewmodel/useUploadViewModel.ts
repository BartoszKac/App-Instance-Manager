import { useCallback, useRef, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { LanguageType } from '../model/RemoteExecutable';
import type { UploadState } from '../model/RemoteExecutable';
import { uploadStoredFile } from '../api/remoteExecutableApi';

export const DESCRIPTION_MAX_LENGTH = 200;

const INITIAL_STATE: UploadState = { status: 'idle', progress: 0, message: null, uploadedFile: null };

const LANGUAGE_BY_EXTENSION: Record<string, LanguageType> = {
  java: LanguageType.JAVA,
  py: LanguageType.PYTHON,
  cpp: LanguageType.CPP,
  cc: LanguageType.CPP,
  cxx: LanguageType.CPP,
  sh: LanguageType.BASH,
};

const languageOf = (fileName: string): LanguageType => {
  const extension = fileName.includes('.') ? fileName.slice(fileName.lastIndexOf('.') + 1).toLowerCase() : '';
  return LANGUAGE_BY_EXTENSION[extension] ?? LanguageType.UNKNOWN;
};

const useFilePicking = (onPick: (file: File) => void) => {
  const [isDragging, setIsDragging] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleDrop = useCallback((e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    setIsDragging(false);
    const file = e.dataTransfer.files[0];
    if (file) onPick(file);
  }, [onPick]);

  const handleDragOver = useCallback((e: React.DragEvent<HTMLDivElement>) => { e.preventDefault(); setIsDragging(true); }, []);
  const handleDragLeave = useCallback(() => setIsDragging(false), []);
  const handleFileInputChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) onPick(file);
  }, [onPick]);
  const openFilePicker = useCallback(() => fileInputRef.current?.click(), []);
  const clearInput = useCallback(() => { if (fileInputRef.current) fileInputRef.current.value = ''; }, []);

  return { isDragging, fileInputRef, handleDrop, handleDragOver, handleDragLeave, handleFileInputChange, openFilePicker, clearInput };
};

const useStoredUploadForm = () => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [selectedLanguage, setSelectedLanguage] = useState<LanguageType>(LanguageType.UNKNOWN);
  const [description, setDescriptionValue] = useState('');
  const [uploadState, setUploadState] = useState<UploadState>(INITIAL_STATE);

  const selectFile = useCallback((file: File) => {
    setSelectedFile(file);
    setSelectedLanguage(languageOf(file.name));
    setUploadState(INITIAL_STATE);
  }, []);

  const setDescription = useCallback((value: string) => setDescriptionValue(value.slice(0, DESCRIPTION_MAX_LENGTH)), []);

  return { selectedFile, setSelectedFile, selectedLanguage, setSelectedLanguage, description, setDescription, uploadState, setUploadState, selectFile };
};

export const useUploadViewModel = () => {
  const form = useStoredUploadForm();
  const picking = useFilePicking(form.selectFile);
  const { selectedFile, selectedLanguage, description, setUploadState, setSelectedFile, setDescription } = form;
  const { clearInput } = picking;

  const upload = useCallback(async () => {
    if (!selectedFile) return;
    setUploadState({ status: 'uploading', progress: 0, message: null, uploadedFile: null });
    try {
      const message = await uploadStoredFile({ file: selectedFile, language: selectedLanguage, description },
        (progress) => setUploadState((prev) => ({ ...prev, progress })));
      setUploadState({ status: 'success', progress: 100, message, uploadedFile: { name: selectedFile.name, language: selectedLanguage } });
    } catch (err: unknown) {
      setUploadState({ status: 'error', progress: 0, message: errorMessage(err, 'Nie udało się przesłać pliku.'), uploadedFile: null });
    }
  }, [selectedFile, selectedLanguage, description, setUploadState]);

  const reset = useCallback(() => {
    setSelectedFile(null);
    setDescription('');
    setUploadState(INITIAL_STATE);
    clearInput();
  }, [setSelectedFile, setDescription, setUploadState, clearInput]);

  return {
    selectedFile, selectedLanguage, setSelectedLanguage: form.setSelectedLanguage, description, setDescription,
    uploadState: form.uploadState, ...picking, upload, reset,
  };
};
