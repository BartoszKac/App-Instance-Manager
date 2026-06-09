// src/editor/viewmodels/useProjects.ts
import { useState, useMemo, useEffect } from 'react';
import {
  Project,
  ProjectsState,
  INITIAL_PROJECTS_STATE,
  Language,
  LanguageType,
  EXT_TO_LANGUAGE,
  LANGUAGE_TO_EXT,
  SourceCode,
} from '../model/EditorState';
import { CodeService } from '../api/CodeService';

// Lokalny licznik — używany tylko dla nowo tworzonych projektów (jeszcze nie zapisanych)
let nextIdManClass = 1000;

// Konwertuje listę SourceCode z backendu na Project
// Grupuje pliki po idManClass — każda unikalna wartość to osobny projekt
const sourceCodeToProjects = (sourceCodes: SourceCode[]): Project[] => {
  const grouped = new Map<number, SourceCode[]>();

  sourceCodes.forEach(sc => {
    const group = grouped.get(sc.idManClass) ?? [];
    group.push(sc);
    grouped.set(sc.idManClass, group);
  });

  const projects: Project[] = [];

  grouped.forEach((files, idManClass) => {
    // Nazwa projektu = nazwa pierwszego pliku bez rozszerzenia
    const mainFile = files.find(f => f.id === idManClass) ?? files[0];
    const projectName = mainFile.name.replace(/\.[^/.]+$/, '');
    const ext = LANGUAGE_TO_EXT[mainFile.language] ?? 'java';
    const lang = ext as Language;

    projects.push({
      id: idManClass,
      name: projectName,
      desc: '',
      lang,
      files: files.map(f => ({
        id: f.id,
        name: f.name,
        ext: LANGUAGE_TO_EXT[f.language] ?? 'java',
        content: f.code,
        language: f.language,
      })),
      created: '',
    });
  });

  return projects;
};

export const useProjects = () => {
  const [state, setState] = useState<ProjectsState>({
    ...INITIAL_PROJECTS_STATE,
    projects: [],
  });
  const [loading, setLoading] = useState(false);
  const [error, setError]     = useState<string | null>(null);

  // Ładuj projekty z backendu przy starcie
  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        const all = await CodeService.getAllSourceCodes();
        const projects = sourceCodeToProjects(all);
        // Ustaw licznik powyżej max id z backendu żeby uniknąć kolizji
        const maxId = projects.reduce((m, p) => Math.max(m, p.id), 0);
        nextIdManClass = maxId + 1;
        setState(prev => ({ ...prev, projects }));
      } catch (err: any) {
        setError(err.message || 'Błąd ładowania projektów');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const filteredProjects = useMemo(() => {
    let list = state.projects;
    if (state.filter !== 'all') {
      list = list.filter(p => p.lang === state.filter);
    }
    if (state.searchQuery) {
      const q = state.searchQuery.toLowerCase();
      list = list.filter(
        p => p.name.toLowerCase().includes(q) || p.desc.toLowerCase().includes(q)
      );
    }
    return list;
  }, [state.projects, state.filter, state.searchQuery]);

  const setFilter = (filter: Language | 'all') => {
    setState(prev => ({ ...prev, filter }));
  };

  const setSearchQuery = (searchQuery: string) => {
    setState(prev => ({ ...prev, searchQuery }));
  };

  const createProject = (name: string, desc: string, lang: Language): Project => {
    const idManClass = nextIdManClass++;
    const ext = lang;
    const language: LanguageType = EXT_TO_LANGUAGE[ext] ?? 'UNKNOWN';

    const project: Project = {
      id: idManClass,
      name,
      desc: desc || 'Nowy projekt',
      lang,
      files: [
        {
          id: idManClass,   // pierwszy plik ma id === idManClass
          name: `${name}.${ext}`,
          ext,
          content: '',
          language,
        },
      ],
      created: 'Teraz',
    };

    setState(prev => ({ ...prev, projects: [project, ...prev.projects] }));
    return project;
  };

  return {
    state,
    filteredProjects,
    setFilter,
    setSearchQuery,
    createProject,
    loading,
    error,
  };
};