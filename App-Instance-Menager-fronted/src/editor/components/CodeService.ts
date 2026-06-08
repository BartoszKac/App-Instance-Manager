// src/editor/services/CodeService.ts
import { SourceCode, CompiledCode } from '../model/EditorState';

const API_BASE_URL = 'http://localhost:8888/api/code';

export const CodeService = {

  async getAllSourceCodes(): Promise<SourceCode[]> {
    const res = await fetch(`${API_BASE_URL}/source/main-class/all`);
    if (!res.ok) throw new Error('Nie udało się pobrać listy projektów');
    return res.json();
  },

  async getSourceCodes(idManClass: number): Promise<SourceCode[]> {
    const res = await fetch(`${API_BASE_URL}/source/main-class/${idManClass}`);
    if (!res.ok) throw new Error('Nie udało się pobrać plików projektu');
    return res.json();
  },

  async saveSourceCodes(sourceCodes: SourceCode[]): Promise<void> {
    const res = await fetch(`${API_BASE_URL}/source`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(sourceCodes),
    });
    if (!res.ok) throw new Error('Błąd podczas zapisywania kodu');
  },

  // Uruchamia kompilację wszystkich plików projektu
  async compileAll(idManClass: number): Promise<string> {
    const res = await fetch(`${API_BASE_URL}/compile/all/main-class/${idManClass}`, {
      method: 'POST',
    });
    if (!res.ok) throw new Error('Błąd podczas kompilacji');
    return res.text();
  },

  // Pobiera wyniki kompilacji
  async getCompiledCodes(idManClass: number): Promise<CompiledCode[]> {
    const res = await fetch(`${API_BASE_URL}/compiled/main-class/${idManClass}`);
    if (!res.ok) throw new Error('Nie udało się pobrać wyników kompilacji');
    return res.json();
  },

  async deleteEntireProject(idManClass: number): Promise<void> {
    const res = await fetch(`${API_BASE_URL}/project/${idManClass}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('Błąd podczas usuwania projektu');
  },
};