import { useEffect, useRef } from 'react';

export const Terminal = ({ logs, onClear }) => {
  const terminalRef = useRef(null);

  useEffect(() => {
    if (terminalRef.current) {
      terminalRef.current.scrollTop = terminalRef.current.scrollHeight;
    }
  }, [logs]);

  return (
    <div style={{ flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
      <h4 style={{ marginBottom: '8px', color: '#888' }}>Konsola Wyjściowa:</h4>
      <pre ref={terminalRef} style={{ 
        background: '#000', color: '#00ff00', padding: '15px', flexGrow: 1, minHeight: '300px', overflowY: 'auto', border: '1px solid #444', fontSize: '13px', whiteSpace: 'pre-wrap', margin: 0, borderRadius: '4px', fontFamily: 'Consolas, monospace'
      }}>
        {logs || "System gotowy na kod..."}
      </pre>
      <button onClick={onClear} style={{ fontSize: '11px', alignSelf: 'flex-end', marginTop: '5px', background: 'none', border: '1px solid #444', color: '#666', cursor: 'pointer', padding: '2px 8px' }}>
        Wyczyść logi
      </button>
    </div>
  );
};