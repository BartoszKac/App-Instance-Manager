import { useState } from 'react';

export const SshUpload = () => {
  const [file, setFile] = useState(null);
  const [status, setStatus] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleSend = async () => {
    if (!file) {
      setStatus('⚠️ Wybierz najpierw plik!');
      return;
    }

    setIsLoading(true);
    setStatus('Wysyłanie...');

    const formData = new FormData();
    formData.append('file', file);

    try {
      const response = await fetch('http://localhost:8888/api/v1/files/upload-ssh', {
        method: 'POST',
        body: formData,
      });

      const text = await response.text();

      if (response.ok) {
        setStatus(`✅ ${text}`);
      } else {
        setStatus(`❌ Błąd ${response.status}: ${text}`);
      }
    } catch (err) {
      setStatus(`❌ Błąd połączenia: ${err.message}`);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div style={{
      background: '#1e1e1e',
      border: '1px solid #333',
      borderRadius: '8px',
      padding: '15px',
    }}>
      <h3 style={{ color: '#646cff', marginTop: 0, fontSize: '14px', textAlign: 'center' }}>
        WYŚLIJ PLIK PRZEZ SSH
      </h3>

      <input
        type="file"
        onChange={(e) => {
          setFile(e.target.files[0]);
          setStatus('');
        }}
        style={{
          display: 'block',
          width: '100%',
          marginBottom: '10px',
          background: '#000',
          color: '#ccc',
          border: '1px solid #444',
          borderRadius: '4px',
          padding: '8px',
          fontFamily: 'monospace',
          fontSize: '12px',
          boxSizing: 'border-box',
          cursor: 'pointer',
        }}
      />

      <button
        onClick={handleSend}
        disabled={isLoading}
        style={{
          width: '100%',
          padding: '10px',
          background: isLoading ? '#444' : '#646cff',
          color: '#fff',
          border: 'none',
          borderRadius: '4px',
          cursor: isLoading ? 'not-allowed' : 'pointer',
          fontWeight: 'bold',
          fontSize: '13px',
        }}
      >
        {isLoading ? '⏳ Wysyłanie...' : '🚀 Wyślij plik przez SSH'}
      </button>

      {status && (
        <div style={{
          marginTop: '10px',
          padding: '8px',
          background: '#000',
          border: '1px solid #333',
          borderRadius: '4px',
          fontFamily: 'monospace',
          fontSize: '12px',
          color: status.startsWith('✅') ? '#10b981' : status.startsWith('❌') ? '#f87171' : '#facc15',
          whiteSpace: 'pre-wrap',
        }}>
          {status}
        </div>
      )}
    </div>
  );
};