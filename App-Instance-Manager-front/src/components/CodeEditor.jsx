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

export const CodeEditor = ({ fileName, setFileName, javaCode, setJavaCode, onSave, isSending, extension, setExtension }) => {
  return (
    <div style={{ /* styl bez zmian */ }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <span style={{ color: '#888', fontSize: '12px' }}>NAZWA PLIKU:</span>
          <input 
            value={fileName} 
            onChange={e => setFileName(e.target.value)} 
            style={{ /* styl bez zmian */ }} 
          />
          {/* ZMIANA: Select zamiast statycznego .java */}
          <select 
            value={extension} 
            onChange={(e) => setExtension(e.target.value)}
            style={{ background: '#2a2a2a', color: '#646cff', border: '1px solid #444', borderRadius: '4px', padding: '5px', fontWeight: 'bold' }}
          >
            <option value=".java">.java</option>
            <option value=".py">.py</option>
            <option value=".cpp">.cpp</option>
          </select>
        </div>
      </div>
      
      <div style={{ border: '1px solid #333', borderRadius: '4px', overflow: 'hidden', flexGrow: 1 }}>
        <Editor
          height="60vh"
          language={extension === ".java" ? "java" : extension === ".py" ? "python" : "cpp"} // Dynamiczny język monaco
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