export const FileExplorer = ({ files, onRefresh, onDelete, onCompile, isCompiling }) => {
  return (
    <div>
      <h3 style={{ color: '#646cff' }}>Twoje Klasy</h3>
      <button onClick={onRefresh} style={{ width: '100%', padding: '10px', background: '#3b82f6', border: 'none', color: '#fff', cursor: 'pointer', borderRadius: '4px', marginBottom: '10px' }}>
        Odśwież listę
      </button>
      
      <div style={{ background: '#1e1e1e', padding: '5px', height: '200px', overflowY: 'auto', border: '1px solid #333', borderRadius: '4px' }}>
        {files.length === 0 && <div style={{ color: '#666', padding: '10px', fontSize: '13px' }}>Brak plików na serwerze.</div>}
        {files.map((f, i) => (
          <div key={i} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '8px 12px', borderBottom: '1px solid #282828' }}>
            <span style={{ fontSize: '14px', fontFamily: 'monospace', color: '#e0e0e0' }}>{f}.java</span>
            <div style={{ display: 'flex', gap: '8px' }}>
              <button onClick={() => onDelete(f)} style={{ padding: '4px 8px', background: '#dc2626', border: 'none', color: '#fff', cursor: 'pointer', fontSize: '11px', borderRadius: '3px' }}>Usuń</button>
              <button onClick={() => onCompile(f)} disabled={isCompiling} style={{ padding: '4px 12px', background: '#10b981', border: 'none', color: '#fff', cursor: 'pointer', fontSize: '11px', borderRadius: '3px', fontWeight: 'bold' }}>RUN</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};