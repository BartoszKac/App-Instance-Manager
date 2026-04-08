import { useState, useEffect } from 'react';
import { useIde } from './hooks/useIde';
import { CodeEditor } from './components/CodeEditor';
import { FileExplorer } from './components/FileExplorer';
import { Terminal } from './components/Terminal';
import './App.css';

function App() {
  const [openFiles, setOpenFiles] = useState([
    { name: 'Main', code: 'public class Main {\n    public static void main(String[] args) {\n        System.out.println("No i działa!");\n    }\n}' }
  ]);
  const [activeFileIndex, setActiveFileIndex] = useState(0);

  const activeFile = openFiles[activeFileIndex];
  const wsUrl = 'http://localhost:8081/ws-console';
  
  const {
    isSending, isCompiling, serverFiles, compileResult,
    loadFiles, saveFiles, compileFile, deleteFile, removeLocalFile, clearLogs, addLog
  } = useIde(wsUrl);

  useEffect(() => {
    loadFiles();
  }, []);

  const handleCodeChange = (newCode) => {
    const updated = [...openFiles];
    updated[activeFileIndex].code = newCode;
    setOpenFiles(updated);
  };

  const handleNameChange = (newName) => {
    const updated = [...openFiles];
    updated[activeFileIndex].name = newName;
    setOpenFiles(updated);
  };

  const handleNewFile = () => {
    const newFile = { name: `Klasa${openFiles.length + 1}`, code: 'public class Nazwa {\n    \n}' };
    setOpenFiles([...openFiles, newFile]);
    setActiveFileIndex(openFiles.length);
    addLog("> Dodano nową zakładkę.");
  };

  return (
    <div style={{ display: 'flex', maxWidth: '1400px', margin: '0 auto', padding: '20px', gap: '20px', color: '#fff', background: '#121212', minHeight: '95vh', fontFamily: 'Segoe UI' }}>
      
      <div style={{ flex: 1.5, display: 'flex', flexDirection: 'column' }}>
        {/* Pasek Zakładek */}
        <div style={{ display: 'flex', gap: '5px', marginBottom: '0px', overflowX: 'auto' }}>
          {openFiles.map((file, idx) => (
            <div key={idx} style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
              <button 
                onClick={() => setActiveFileIndex(idx)}
                style={{ 
                  background: activeFileIndex === idx ? '#1e1e1e' : '#2a2a2a', 
                  color: activeFileIndex === idx ? '#646cff' : '#999', 
                  border: '1px solid #333', 
                  borderBottom: activeFileIndex === idx ? 'none' : '1px solid #333',
                  padding: '8px 35px 8px 15px', 
                  cursor: 'pointer',
                  borderRadius: '6px 6px 0 0',
                  fontSize: '13px',
                  fontWeight: activeFileIndex === idx ? 'bold' : 'normal',
                  transition: '0.2s'
                }}
              >
                {file.name || 'bez_nazwy'}.java
                {idx === 0 && <span style={{ marginLeft: '5px', color: '#10b981', fontSize: '10px' }}>★</span>}
              </button>
              
              <span 
                onClick={(e) => {
                  e.stopPropagation();
                  removeLocalFile(idx, openFiles, setOpenFiles, setActiveFileIndex);
                }}
                style={{
                  position: 'absolute',
                  right: '10px',
                  color: '#666',
                  cursor: 'pointer',
                  fontSize: '16px',
                  lineHeight: '1'
                }}
                onMouseOver={(e) => e.target.style.color = '#ff4d4d'}
                onMouseOut={(e) => e.target.style.color = '#666'}
              >
                ×
              </span>
            </div>
          ))}
          <button onClick={handleNewFile} style={{ background: 'none', border: '1px dashed #444', color: '#666', padding: '5px 15px', cursor: 'pointer', borderRadius: '4px', marginLeft: '5px', marginBottom: '5px' }}>
            +
          </button>
        </div>

        <CodeEditor 
          fileName={activeFile.name} 
          setFileName={handleNameChange}
          javaCode={activeFile.code} 
          setJavaCode={handleCodeChange}
          isSending={isSending}
          onSave={() => saveFiles(openFiles)}
        />
      </div>

      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '20px' }}>
        <FileExplorer 
          files={serverFiles}
          isCompiling={isCompiling}
          onRefresh={loadFiles}
          onDelete={deleteFile}
          onCompile={compileFile}
        />
        <Terminal logs={compileResult} onClear={clearLogs} />
      </div>

    </div>
  );
}

export default App;