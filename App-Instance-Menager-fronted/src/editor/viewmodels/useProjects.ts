import { useState, useMemo } from 'react';
import {
  Project,
  ProjectsState,
  INITIAL_PROJECTS_STATE,
  DEFAULT_FILES,
  Language,
} from '../model/EditorState';

const MOCK_PROJECTS: Project[] = [
  {
    id: 1,
    name: 'dashboard-app',
    desc: 'Panel administracyjny React',
    lang: 'ts',
    files: [...DEFAULT_FILES.ts],
    created: '2 dni temu',
  },
  {
    id: 2,
    name: 'api-server',
    desc: 'REST API w Pythonie',
    lang: 'py',
    files: [...DEFAULT_FILES.py],
    created: '5 dni temu',
  },
  {
    id: 3,
    name: 'state-manager',
    desc: 'Lekki menadżer stanu',
    lang: 'js',
    files: [...DEFAULT_FILES.js],
    created: '1 tydzień temu',
  },
  {
    id: 4,
    name: 'web-router',
    desc: 'Szybki router HTTP w Rust',
    lang: 'rs',
    files: [...DEFAULT_FILES.rs],
    created: '2 tygodnie temu',
  },
];

let nextId = 5;

export const useProjects = () => {
  const [state, setState] = useState<ProjectsState>({
    ...INITIAL_PROJECTS_STATE,
    projects: MOCK_PROJECTS,
  });

  const filteredProjects = useMemo(() => {
    let list = state.projects;
    if (state.filter !== 'all') {
      list = list.filter((p) => p.lang === state.filter);
    }
    if (state.searchQuery) {
      const q = state.searchQuery.toLowerCase();
      list = list.filter(
        (p) =>
          p.name.toLowerCase().includes(q) ||
          p.desc.toLowerCase().includes(q)
      );
    }
    return list;
  }, [state.projects, state.filter, state.searchQuery]);

  const setFilter = (filter: Language | 'all') => {
    setState((prev) => ({ ...prev, filter }));
  };

  const setSearchQuery = (searchQuery: string) => {
    setState((prev) => ({ ...prev, searchQuery }));
  };

  const createProject = (name: string, desc: string, lang: Language): Project => {
    const project: Project = {
      id: nextId++,
      name,
      desc: desc || 'Nowy projekt',
      lang,
      files: [...DEFAULT_FILES[lang]],
      created: 'Teraz',
    };
    setState((prev) => ({
      ...prev,
      projects: [project, ...prev.projects],
    }));
    return project;
  };

  return {
    state,
    filteredProjects,
    setFilter,
    setSearchQuery,
    createProject,
  };
};
