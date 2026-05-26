import { useState, useEffect } from 'react';
import { deployToRemote, fetchDeployConfig, saveDeployConfig, fetchServerFiles } from '../api/api';

export const DeployPanel = ({ activeFile, addLog }) => {
  const [configs, setConfigs] = useState([]);
  const [selectedConfigName, setSelectedConfigName] = useState('');
  const [isSavingConfig, setIsSavingConfig] = useState(false);
  const [isDeploying, setIsDeploying] = useState(false);

  // NOWE: lista plików z serwera do wyboru
  const [serverFiles, setServerFiles] = useState([]);
  const [selectedFiles, setSelectedFiles] = useState([]);
  const [isLoadingFiles, setIsLoadingFiles] = useState(false);

  // NOWE: port forwarding
  const [portForwardEnabled, setPortForwardEnabled] = useState(false);
  const [localPort, setLocalPort] = useState('');
  const [remotePort, setRemotePort] = useState('');

  // Synchronizuj aktywny plik jako domyślny wybór
  useEffect(() => {
    if (activeFile) {
      const fullName = activeFile.name + (activeFile.extension || '.java');
      setSelectedFiles(prev =>
        prev.includes(fullName) ? prev : [fullName]
      );
    }
  }, [activeFile?.name, activeFile?.extension]);

  // Załaduj konfiguracje SSH
  useEffect(() => {
    const loadConfig = async () => {
      try {
        const data = await fetchDeployConfig();
        setConfigs(data);
        if (data && data.length > 0) {
          setSelectedConfigName(data[0].name || '');
        }
      } catch (err) {
        addLog("!!! BŁĄD: Nie można pobrać konfiguracji z serwera.");
      }
    };
    loadConfig();
  }, []);

  // Załaduj pliki z serwera
  const loadServerFiles = async () => {
    setIsLoadingFiles(true);
    try {
      const files = await fetchServerFiles();
      // Filtruj: tylko pliki źródłowe, bez .class
      setServerFiles(files.filter(f => !f.endsWith('.class')));
    } catch (err) {
      addLog("!!! Nie można pobrać listy plików serwera.");
    } finally {
      setIsLoadingFiles(false);
    }
  };

  useEffect(() => {
    loadServerFiles();
  }, []);

  const currentConfig = configs.find(c => c.name === selectedConfigName) || { name: '', ip: '', user: '', pass: '' };

  const handleConfigChange = (e) => {
    const updatedConfigs = configs.map(c =>
      c.name === selectedConfigName ? { ...c, [e.target.name]: e.target.value } : c
    );
    setConfigs(updatedConfigs);
  };

  const handleAddConfig = () => {
    const newName = prompt("Podaj unikalną nazwę dla nowej konfiguracji (np. 'Prod', 'Test'):");
    if (newName && !configs.find(c => c.name === newName)) {
      const newConfig = { name: newName, ip: '', user: '', pass: '' };
      setConfigs([...configs, newConfig]);
      setSelectedConfigName(newName);
    }
  };

  const handleSaveConfig = async () => {
    if (!selectedConfigName) return;
    setIsSavingConfig(true);
    try {
      await saveDeployConfig(currentConfig);
      addLog(">>> CONFIG: Zapisano konfigurację '" + selectedConfigName + "'.");
    } catch (err) {
      addLog(`!!! CONFIG BŁĄD: ${err.message}`);
    } finally {
      setIsSavingConfig(false);
    }
  };

  // Obsługa multi-select plików
  const toggleFileSelection = (fileName) => {
    setSelectedFiles(prev =>
      prev.includes(fileName)
        ? prev.filter(f => f !== fileName)
        : [...prev, fileName]
    );
  };

  const selectAll = () => setSelectedFiles([...serverFiles]);
  const clearSelection = () => setSelectedFiles([]);

  const handleDeploy = async () => {
    if (selectedFiles.length === 0) {
      addLog("!!! BŁĄD: Nie wybrano żadnych plików do deploymentu.");
      return;
    }
    if (!selectedConfigName) {
      addLog("!!! BŁĄD: Nie wybrano konfiguracji serwera.");
      return;
    }

    const lp = portForwardEnabled && localPort ? parseInt(localPort) : null;
    const rp = portForwardEnabled && remotePort ? parseInt(remotePort) : null;

    if (portForwardEnabled && (!lp || !rp)) {
      addLog("!!! BŁĄD: Port forwarding wymaga podania obu portów.");
      return;
    }

    // Deployment plików jeden po drugim — każdy jako osobna sesja SSH
    // Główny plik (pierwszy na liście) dostaje port forwarding jeśli włączony
    setIsDeploying(true);
    addLog(`>>> DEPLOY: Wysyłanie ${selectedFiles.length} pliku/plików → ${currentConfig.ip || '?'}`);
    if (portForwardEnabled && lp && rp) {
      addLog(`>>> DEPLOY: Port forwarding: localhost:${lp} → zdalny:${rp}`);
    }

    try {
      // Pierwszy plik = główny (z port forwardingiem jeśli włączony)
      const mainFile = selectedFiles[0];
      await deployToRemote(mainFile, selectedConfigName, lp, rp);
      addLog(`>>> DEPLOY: Uruchomiono ${mainFile}. Obserwuj terminal poniżej.`);

      // Pozostałe pliki — wysyłamy bez uruchamiania (tylko transfer)
      // Jeśli chcesz uruchomić każdy osobno, możesz to zmienić
      if (selectedFiles.length > 1) {
        addLog(`>>> DEPLOY: Powiązane pliki (${selectedFiles.length - 1}) są wysyłane razem przez backend (ProcessMenager).`);
      }
    } catch (err) {
      addLog(`!!! DEPLOY BŁĄD: ${err.message}`);
    } finally {
      setIsDeploying(false);
    }
  };

  const inputStyle = {
    background: '#000',
    border: '1px solid #444',
    color: '#00ff00',
    padding: '8px',
    borderRadius: '4px',
    fontFamily: 'monospace',
    width: '100%',
    boxSizing: 'border-box'
  };

  const labelStyle = {
    fontSize: '11px',
    color: '#888',
    marginBottom: '4px',
    display: 'block'
  };

  return (
    <div style={{ background: '#1e1e1e', padding: '15px', borderRadius: '8px', border: '1px solid #333' }}>
      <h3 style={{ color: '#646cff', marginTop: 0, fontSize: '14px', textAlign: 'center' }}>
        ZDALNY DEPLOYMENT
      </h3>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>

        {/* KONFIGURACJA SSH */}
        <div style={{ padding: '10px', background: '#2a2a2a', borderRadius: '6px', border: '1px solid #444' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px', gap: '5px' }}>
            <select
              value={selectedConfigName}
              onChange={(e) => setSelectedConfigName(e.target.value)}
              style={{ flex: 1, background: '#1a1a1a', color: '#646cff', border: '1px solid #444', padding: '4px', borderRadius: '4px', fontSize: '12px', cursor: 'pointer' }}
            >
              {configs.length === 0 && <option value="">Brak konfiguracji</option>}
              {configs.map(c => (
                <option key={c.name} value={c.name}>{c.name}</option>
              ))}
            </select>
            <button onClick={handleAddConfig} style={{ background: '#3b82f6', color: '#fff', border: 'none', padding: '3px 8px', borderRadius: '4px', fontSize: '12px', cursor: 'pointer' }}>+</button>
            <button onClick={handleSaveConfig} disabled={isSavingConfig} style={{ background: '#10b981', color: '#fff', border: 'none', padding: '3px 10px', borderRadius: '4px', fontSize: '10px', cursor: 'pointer' }}>
              {isSavingConfig ? 'ZAPIS...' : 'ZAPISZ'}
            </button>
          </div>

          <label style={labelStyle}>IP serwera</label>
          <input type="text" name="ip" value={currentConfig.ip} onChange={handleConfigChange} placeholder="192.168.1.100" style={{ ...inputStyle, marginBottom: '6px' }} />

          <div style={{ display: 'flex', gap: '6px' }}>
            <div style={{ flex: 1 }}>
              <label style={labelStyle}>Użytkownik</label>
              <input type="text" name="user" value={currentConfig.user} onChange={handleConfigChange} placeholder="user" style={inputStyle} />
            </div>
            <div style={{ flex: 1 }}>
              <label style={labelStyle}>Hasło</label>
              <input type="password" name="pass" value={currentConfig.pass} onChange={handleConfigChange} placeholder="••••••" style={inputStyle} />
            </div>
          </div>
        </div>

        {/* NOWE: WYBÓR PLIKÓW Z SERWERA */}
        <div style={{ padding: '10px', background: '#2a2a2a', borderRadius: '6px', border: '1px solid #444' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
            <span style={{ fontSize: '11px', color: '#888' }}>PLIKI DO WYSŁANIA:</span>
            <div style={{ display: 'flex', gap: '4px' }}>
              <button onClick={selectAll} style={{ background: 'none', border: '1px solid #444', color: '#888', padding: '2px 6px', borderRadius: '3px', fontSize: '10px', cursor: 'pointer' }}>
                Wszystkie
              </button>
              <button onClick={clearSelection} style={{ background: 'none', border: '1px solid #444', color: '#888', padding: '2px 6px', borderRadius: '3px', fontSize: '10px', cursor: 'pointer' }}>
                Wyczyść
              </button>
              <button onClick={loadServerFiles} disabled={isLoadingFiles} style={{ background: 'none', border: '1px solid #444', color: '#3b82f6', padding: '2px 6px', borderRadius: '3px', fontSize: '10px', cursor: 'pointer' }}>
                {isLoadingFiles ? '...' : '↻'}
              </button>
            </div>
          </div>

          <div style={{ maxHeight: '150px', overflowY: 'auto', border: '1px solid #333', borderRadius: '4px' }}>
            {serverFiles.length === 0 && (
              <div style={{ color: '#555', padding: '10px', fontSize: '12px', fontFamily: 'monospace' }}>
                {isLoadingFiles ? 'Ładowanie...' : 'Brak plików na serwerze. Najpierw zapisz projekt.'}
              </div>
            )}
            {serverFiles.map((file, i) => (
              <label key={i} style={{
                display: 'flex', alignItems: 'center', gap: '8px',
                padding: '6px 10px',
                borderBottom: i < serverFiles.length - 1 ? '1px solid #222' : 'none',
                cursor: 'pointer',
                background: selectedFiles.includes(file) ? '#1a2a1a' : 'transparent',
                transition: 'background 0.15s'
              }}>
                <input
                  type="checkbox"
                  checked={selectedFiles.includes(file)}
                  onChange={() => toggleFileSelection(file)}
                  style={{ accentColor: '#10b981', cursor: 'pointer' }}
                />
                <span style={{
                  fontFamily: 'monospace',
                  fontSize: '12px',
                  color: selectedFiles.includes(file) ? '#10b981' : '#ccc',
                  flex: 1
                }}>
                  {file}
                </span>
                {/* Oznacz pierwszy wybrany jako "główny" */}
                {selectedFiles[0] === file && (
                  <span style={{ fontSize: '10px', color: '#646cff', background: '#2a2a4a', padding: '1px 5px', borderRadius: '3px' }}>
                    główny
                  </span>
                )}
              </label>
            ))}
          </div>

          {selectedFiles.length > 0 && (
            <div style={{ marginTop: '6px', fontSize: '11px', color: '#555', fontFamily: 'monospace' }}>
              Wybrano: <span style={{ color: '#10b981' }}>{selectedFiles.length}</span> plik/plików
              {' | '}Główny: <span style={{ color: '#646cff' }}>{selectedFiles[0]}</span>
            </div>
          )}
        </div>

        {/* NOWE: PORT FORWARDING */}
        <div style={{ padding: '10px', background: '#2a2a2a', borderRadius: '6px', border: '1px solid #444' }}>
          <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer', marginBottom: portForwardEnabled ? '10px' : '0' }}>
            <input
              type="checkbox"
              checked={portForwardEnabled}
              onChange={(e) => setPortForwardEnabled(e.target.checked)}
              style={{ accentColor: '#646cff', cursor: 'pointer' }}
            />
            <span style={{ fontSize: '12px', color: portForwardEnabled ? '#646cff' : '#888' }}>
              Port forwarding (przekieruj port zdalny na lokalny)
            </span>
          </label>

          {portForwardEnabled && (
            <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
              <div style={{ flex: 1 }}>
                <label style={labelStyle}>Port lokalny</label>
                <input
                  type="number"
                  value={localPort}
                  onChange={(e) => setLocalPort(e.target.value)}
                  placeholder="8080"
                  style={{ ...inputStyle }}
                />
              </div>
              <span style={{ color: '#555', marginTop: '14px', fontSize: '18px' }}>→</span>
              <div style={{ flex: 1 }}>
                <label style={labelStyle}>Port zdalny</label>
                <input
                  type="number"
                  value={remotePort}
                  onChange={(e) => setRemotePort(e.target.value)}
                  placeholder="8080"
                  style={{ ...inputStyle }}
                />
              </div>
            </div>
          )}

          {portForwardEnabled && localPort && remotePort && (
            <div style={{ marginTop: '6px', fontSize: '11px', color: '#555', fontFamily: 'monospace' }}>
              localhost:<span style={{ color: '#646cff' }}>{localPort}</span>
              {' ←→ '}
              <span style={{ color: '#888' }}>{currentConfig.ip || 'serwer'}:{remotePort}</span>
            </div>
          )}
        </div>

        <button
          onClick={handleDeploy}
          disabled={isDeploying || selectedFiles.length === 0}
          style={{
            background: isDeploying ? '#444' : selectedFiles.length === 0 ? '#333' : '#646cff',
            color: selectedFiles.length === 0 ? '#555' : '#fff',
            border: 'none',
            padding: '10px',
            borderRadius: '4px',
            cursor: isDeploying || selectedFiles.length === 0 ? 'not-allowed' : 'pointer',
            fontWeight: 'bold',
            fontSize: '13px',
            transition: '0.2s'
          }}
        >
          {isDeploying
            ? '⏳ WDRAŻANIE...'
            : selectedFiles.length === 0
              ? 'Wybierz pliki do deploymentu'
              : `🚀 URUCHOM DEPLOYMENT (${selectedFiles.length} plik/pliki)`
          }
        </button>

      </div>
    </div>
  );
};
