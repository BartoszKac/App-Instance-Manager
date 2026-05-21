import { useState, useEffect } from 'react';
import { deployToRemote, fetchDeployConfig, saveDeployConfig } from '../api';

export const DeployPanel = ({ activeFile, addLog }) => {
  const [config, setConfig] = useState({ ip: '', user: '', pass: '' });
  const [isSavingConfig, setIsSavingConfig] = useState(false);
  const [isDeploying, setIsDeploying] = useState(false);
  const [mainAppName, setMainAppName] = useState('');

  // Synchronizuj z aktywnym plikiem - tylko nazwa BEZ rozszerzenia
  useEffect(() => {
    if (activeFile) {
      setMainAppName(activeFile.name);
    }
  }, [activeFile?.name]);

  useEffect(() => {
    const loadConfig = async () => {
      try {
        const data = await fetchDeployConfig();
        setConfig(data);
      } catch (err) {
        addLog("!!! BŁĄD: Nie można pobrać konfiguracji z serwera.");
      }
    };
    loadConfig();
  }, []);

  const handleConfigChange = (e) => {
    setConfig({ ...config, [e.target.name]: e.target.value });
  };

  const handleSaveConfig = async () => {
    setIsSavingConfig(true);
    try {
      await saveDeployConfig(config);
      addLog(">>> CONFIG: Zapisano konfigurację.");
    } catch (err) {
      addLog(`!!! CONFIG BŁĄD: ${err.message}`);
    } finally {
      setIsSavingConfig(false);
    }
  };

  // Czyścimy input z rozszerzeń jeśli ktoś wkleił np. "Main.java"
  const handleNameChange = (e) => {
    let value = e.target.value;
    const knownExts = ['.java', '.py', '.cpp', '.class', '.exe'];
    for (const ext of knownExts) {
      if (value.endsWith(ext)) {
        value = value.slice(0, -ext.length);
        break;
      }
    }
    setMainAppName(value);
  };

  const handleDeploy = async () => {
    const targetName = mainAppName.trim();
    if (!targetName) {
      addLog("!!! BŁĄD: Brak nazwy głównej klasy.");
      return;
    }

    const sourceExtension = activeFile?.extension || '.java';
    const fullFileName = targetName + sourceExtension;

    setIsDeploying(true);
    addLog(`>>> DEPLOY: ${fullFileName} → ${config.ip || '?'}`);

    try {
      await deployToRemote(fullFileName);
      addLog(`>>> DEPLOY: Uruchomiono. Obserwuj terminal poniżej.`);
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

  const sourceExtension = activeFile?.extension || '.java';
  const previewName = (mainAppName.trim() || '?') + sourceExtension;

  return (
    <div style={{ background: '#1e1e1e', padding: '15px', borderRadius: '8px', border: '1px solid #333' }}>
      <h3 style={{ color: '#646cff', marginTop: 0, fontSize: '14px', textAlign: 'center' }}>
        ZDALNY DEPLOYMENT
      </h3>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>

        {/* KONFIGURACJA SSH */}
        <div style={{ padding: '10px', background: '#2a2a2a', borderRadius: '6px', border: '1px solid #444' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
            <span style={{ fontSize: '11px', color: '#888' }}>KONFIGURACJA SSH:</span>
            <button
              onClick={handleSaveConfig}
              disabled={isSavingConfig}
              style={{ background: '#10b981', color: '#fff', border: 'none', padding: '3px 10px', borderRadius: '4px', fontSize: '10px', cursor: 'pointer' }}
            >
              {isSavingConfig ? 'ZAPIS...' : 'ZAPISZ'}
            </button>
          </div>

          <label style={labelStyle}>IP serwera</label>
          <input
            type="text" name="ip" value={config.ip}
            onChange={handleConfigChange} placeholder="192.168.1.100"
            style={{ ...inputStyle, marginBottom: '6px' }}
          />

          <div style={{ display: 'flex', gap: '6px' }}>
            <div style={{ flex: 1 }}>
              <label style={labelStyle}>Użytkownik</label>
              <input type="text" name="user" value={config.user} onChange={handleConfigChange} placeholder="user" style={inputStyle} />
            </div>
            <div style={{ flex: 1 }}>
              <label style={labelStyle}>Hasło</label>
              <input type="password" name="pass" value={config.pass} onChange={handleConfigChange} placeholder="••••••" style={inputStyle} />
            </div>
          </div>
        </div>

        {/* DEPLOY - tylko nazwa klasy */}
        <div style={{ padding: '10px', background: '#2a2a2a', borderRadius: '6px', border: '1px solid #444' }}>
          <label style={labelStyle}>GŁÓWNA KLASA (tylko nazwa, bez rozszerzenia):</label>

          {/* Input z widocznym suffixem rozszerzenia */}
          <div style={{ display: 'flex', alignItems: 'center', background: '#000', border: '1px solid #444', borderRadius: '4px', overflow: 'hidden' }}>
            <input
              type="text"
              value={mainAppName}
              onChange={handleNameChange}
              placeholder="Main"
              style={{ ...inputStyle, border: 'none', borderRadius: 0, flex: 1, margin: 0 }}
            />
            <span style={{
              padding: '8px 10px',
              background: '#1a1a1a',
              color: '#646cff',
              fontFamily: 'monospace',
              fontSize: '13px',
              borderLeft: '1px solid #333',
              whiteSpace: 'nowrap'
            }}>
              {sourceExtension}
            </span>
          </div>

          <div style={{ marginTop: '6px', fontSize: '11px', color: '#555', fontFamily: 'monospace' }}>
            Wyślę: <span style={{ color: '#10b981' }}>{previewName}</span>
            {' + '}
            <span style={{ color: '#888' }}>wszystkie powiązane pliki projektu</span>
          </div>
        </div>

        <button
          onClick={handleDeploy}
          disabled={isDeploying}
          style={{
            background: isDeploying ? '#444' : '#646cff',
            color: '#fff',
            border: 'none',
            padding: '10px',
            borderRadius: '4px',
            cursor: isDeploying ? 'not-allowed' : 'pointer',
            fontWeight: 'bold',
            fontSize: '13px',
            transition: '0.2s'
          }}
        >
          {isDeploying ? '⏳ WDRAŻANIE...' : '🚀 URUCHOM DEPLOYMENT'}
        </button>

      </div>
    </div>
  );
};