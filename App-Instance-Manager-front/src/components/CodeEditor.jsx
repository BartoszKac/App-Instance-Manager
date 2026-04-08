import Editor from '@monaco-editor/react';

const editorOptions = {
  fontSize: 14,
  minimap: { enabled: false },
  automaticLayout: true,
  tabSize: 4,
  scrollBeyondLastLine: false,
  formatOnPaste: true,
  formatOnType: true,
  suggestOnTriggerCharacters: true,
  acceptSuggestionOnEnter: "on",
  snippetSuggestions: "top",
  wordBasedSuggestions: true,
  quickSuggestions: { other: true, comments: true, strings: true },
  parameterHints: { enabled: true }
};

export const CodeEditor = ({ fileName, setFileName, javaCode, setJavaCode, onSave, isSending }) => {
  return (
    <div style={{ flex: 1, display: 'flex', flexDirection: 'column', background: '#1e1e1e', padding: '15px', border: '1px solid #333', borderRadius: '0 0 4px 4px' }}>
      
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <span style={{ color: '#888', fontSize: '12px' }}>NAZWA PLIKU:</span>
          <input 
            value={fileName} 
            onChange={e => setFileName(e.target.value)} 
            placeholder="Np. Silnik"
            style={{ 
              padding: '6px 12px', 
              background: '#2a2a2a', 
              border: '1px solid #444', 
              color: '#fff', 
              borderRadius: '4px',
              fontFamily: 'monospace' 
            }} 
          />
          <span style={{ color: '#646cff', fontWeight: 'bold' }}>.java</span>
        </div>
      </div>
      
      <div style={{ border: '1px solid #333', borderRadius: '4px', overflow: 'hidden', flexGrow: 1 }}>
        <Editor
          height="60vh"
          defaultLanguage="java"
          theme="vs-dark"
          value={javaCode}
          onChange={(val) => setJavaCode(val || '')}
          options={editorOptions}
        />
      </div>

      <button 
        onClick={onSave} 
        disabled={isSending} 
        style={{ 
          width: '100%', 
          padding: '15px', 
          marginTop: '15px', 
          background: '#646cff', 
          border: 'none', 
          color: '#fff', 
          cursor: isSending ? 'not-allowed' : 'pointer', 
          fontWeight: 'bold', 
          borderRadius: '4px',
          opacity: isSending ? 0.7 : 1
        }}
      >
        {isSending ? 'Trwa zapisywanie projektu...' : 'Zapisz wszystkie pliki na serwerze'}
      </button>
    </div>
  );
};