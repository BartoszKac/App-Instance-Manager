import { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { saveJavaFiles, fetchServerFiles, compileJavaFile, deleteJavaFile } from '../api'; 

export const useIde = (wsUrl) => {
  const [isSending, setIsSending] = useState(false);
  const [isCompiling, setIsCompiling] = useState(false);
  const [serverFiles, setServerFiles] = useState([]);
  const [compileResult, setCompileResult] = useState('');
  
  const stompClient = useRef(null);

  // Połączenie WebSocket (bez zmian, jest OK)
  useEffect(() => {
    const connectWS = () => {
      try {
        const socket = new SockJS(wsUrl);
        stompClient.current = Stomp.over(socket);
        stompClient.current.debug = null; 

        stompClient.current.connect({}, 
          () => {
            setCompileResult(prev => prev + ">>> SYSTEM: Połączono z serwerem logów.\n");
            stompClient.current.subscribe('/topic/output', (message) => {
              setCompileResult(prev => prev + message.body + '\n');
            });
          }, 
          (err) => {
            setCompileResult(prev => prev + "!!! BŁĄD: Brak połączenia z serwerem logów !!!\n");
          }
        );
      } catch (err) {
        setCompileResult(prev => prev + "!!! BŁĄD KRYTYCZNY JS: " + err.message + " !!!\n");
      }
    };

    connectWS();
    return () => {
      if (stompClient.current && stompClient.current.connected) {
        stompClient.current.disconnect();
      }
    };
  }, [wsUrl]);

  // 1. Ładowanie listy plików
  const loadFiles = async () => {
    try {
      const filesArray = await fetchServerFiles();
      setServerFiles(filesArray);
    } catch (error) {
      console.error(error);
      setCompileResult(prev => prev + "!!! Nie udało się pobrać listy plików (sprawdź port serwera) !!!\n");
    }
  };

  // 2. Zapisywanie plików (obsługa extension)
  const saveFiles = async (filesArray) => {
    if (!filesArray || filesArray.length === 0) return;
    setIsSending(true);
    try {
      await saveJavaFiles(filesArray);
      const mainFile = filesArray[0];
      // Wyświetlamy w logach faktyczne rozszerzenie, a nie tylko .java
      setCompileResult(prev => prev + `> Zapisano projekt: ${mainFile.name}${mainFile.extension}\n`);
      loadFiles(); 
    } catch (error) {
      setCompileResult(prev => prev + "!!! Błąd zapisu projektu !!!\n");
    } finally {
      setIsSending(false);
    }
  };

  // 3. Uruchamianie (Kompletna zmiana logiki rozszerzeń)
  const compileFile = async (fullFileName) => {
    if (isCompiling) return;
    setIsCompiling(true);

    // Wyciągamy kropkę i to co po niej (np. .py, .java, .cpp)
    const lastDotIndex = fullFileName.lastIndexOf('.');
    const nameOnly = fullFileName.substring(0, lastDotIndex);
    const extension = fullFileName.substring(lastDotIndex);

    setCompileResult(prev => prev + `--- Uruchamianie: ${fullFileName} ---\n`);
    
    try {
      // Przekazujemy nazwę i rozszerzenie osobno do API
      await compileJavaFile(nameOnly, extension);
    } catch (error) {
      setCompileResult(prev => prev + '!!! Błąd endpointa uruchamiania !!!\n');
    } finally {
      setIsCompiling(false);
    }
  };

  // 4. Usuwanie (z obsługą rozszerzenia)
  const deleteFile = async (fullFileName) => {
    if (!window.confirm(`Usunąć ${fullFileName} z serwera?`)) return;
    
    const lastDotIndex = fullFileName.lastIndexOf('.');
    const nameOnly = fullFileName.substring(0, lastDotIndex);
    const extension = fullFileName.substring(lastDotIndex);

    try {
      await deleteJavaFile(nameOnly, extension);
      setCompileResult(prev => prev + `> Usunięto: ${fullFileName}\n`);
      loadFiles();
    } catch (error) {
      setCompileResult(prev => prev + `!!! Błąd podczas usuwania !!!\n`);
    }
  };

  const removeLocalFile = (index, openFiles, setOpenFiles, setActiveFileIndex) => {
    if (openFiles.length <= 1) return;
    const updatedFiles = openFiles.filter((_, i) => i !== index);
    setOpenFiles(updatedFiles);
    setActiveFileIndex((prev) => (prev >= updatedFiles.length ? updatedFiles.length - 1 : prev));
  };

  const clearLogs = () => setCompileResult('');
  const addLog = (log) => setCompileResult(prev => prev + log + '\n');

  return {
    isSending, isCompiling, serverFiles, compileResult,
    loadFiles, saveFiles, removeLocalFile, compileFile, deleteFile, clearLogs, addLog
  };
};