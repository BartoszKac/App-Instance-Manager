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
            console.error(err);
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

  const loadFiles = async () => {
    try {
      const filesArray = await fetchServerFiles();
      setServerFiles(filesArray);
    } catch (error) {
      setCompileResult(prev => prev + "!!! Nie udało się pobrać listy plików !!!\n");
    }
  };

  const saveFiles = async (filesArray) => {
    if (!filesArray || filesArray.length === 0) return;
    setIsSending(true);
    try {
      await saveJavaFiles(filesArray);
      setCompileResult(prev => prev + `> Zapisano projekt na serwerze (${filesArray.length} plików).\n> Główny plik ustawiony na: ${filesArray[0].name}.java\n`);
      loadFiles(); 
    } catch (error) {
      setCompileResult(prev => prev + "!!! Błąd zapisu projektu na serwerze !!!\n");
    } finally {
      setIsSending(false);
    }
  };

  // --- NOWA FUNKCJA: Usuwanie lokalne (zakładki) ---
  const removeLocalFile = (index, openFiles, setOpenFiles, setActiveFileIndex) => {
    if (openFiles.length <= 1) {
      setCompileResult(prev => prev + "> Info: Projekt musi mieć przynajmniej jeden plik.\n");
      return;
    }

    const updatedFiles = openFiles.filter((_, i) => i !== index);
    setOpenFiles(updatedFiles);

    // Korekta aktywnego indeksu, żeby nie patrzył w "próżnię"
    setActiveFileIndex((prevIndex) => {
      if (prevIndex >= updatedFiles.length) return updatedFiles.length - 1;
      return prevIndex;
    });
    
    setCompileResult(prev => prev + `> Zamknięto plik lokalnie.\n`);
  };

  const compileFile = async (nameOnly) => {
    if (isCompiling) return;
    setIsCompiling(true);
    setCompileResult(prev => prev + `--- Uruchamianie: ${nameOnly} ---\n`);
    try {
      await compileJavaFile(nameOnly);
    } catch (error) {
      setCompileResult(prev => prev + '!!! Błąd endpointa kompilacji !!!\n');
    } finally {
      setIsCompiling(false);
    }
  };

  const deleteFile = async (nameOnly) => {
    if (!window.confirm(`Usunąć ${nameOnly}.java z serwera?`)) return;
    try {
      await deleteJavaFile(nameOnly);
      setCompileResult(prev => prev + `> Usunięto: ${nameOnly}.java\n`);
      loadFiles();
    } catch (error) {
      setCompileResult(prev => prev + `!!! Błąd podczas usuwania !!!\n`);
    }
  };

  const clearLogs = () => setCompileResult('');
  const addLog = (log) => setCompileResult(prev => prev + log + '\n');

  return {
    isSending,
    isCompiling,
    serverFiles,
    compileResult,
    loadFiles,
    saveFiles,
    removeLocalFile, // Dodano do eksportu
    compileFile,
    deleteFile,
    clearLogs,
    addLog
  };
};