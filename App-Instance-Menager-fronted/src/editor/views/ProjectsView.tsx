// src/editor/views/ProjectsView.tsx
import React, { useState } from 'react';
import { Project, Language } from '@/editor/model';
import { useProjects } from '@/editor/viewmodels';
import { ProjectCard, NewProjectModal, Topbar } from '@/editor/components';

interface ProjectsViewProps {
  onOpenProject: (project: Project) => void;
  onDeploy: () => void;
  onExecution: () => void;
}

export const ProjectsView: React.FC<ProjectsViewProps> = ({ onOpenProject, onDeploy, onExecution }) => {
  const { filteredProjects, state, setFilter, setSearchQuery, createProject } = useProjects();
  const [isModalOpen, setIsModalOpen] = useState(false);

  const handleCreate = (name: string, desc: string, lang: Language) => {
    const project = createProject(name, desc, lang);
    onOpenProject(project);
  };

  const filters: { label: string; value: Language | 'all' }[] = [
    { label: 'Wszystkie', value: 'all' },
    { label: 'TypeScript', value: 'ts' },
    { label: 'Python', value: 'py' },
    { label: 'JavaScript', value: 'js' },
  ];

  return (
    <div className="screen-projects">
      <Topbar
        left={
          <div className="nav-header" style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
            <div className="nav-tabs">
              <button className="nav-tab active">Projekty</button>
              <button className="nav-tab" onClick={onDeploy}>Deploy</button>
              <button className="nav-tab" onClick={onExecution}>Instancje</button>
            </div>
            <div className="breadcrumb">
              <span className="breadcrumb-sep">/</span>
              <span className="breadcrumb-cur">wszystkie-projekty</span>
            </div>
          </div>
        }
        right={
          <>
            <button className="nav-btn primary" onClick={() => setIsModalOpen(true)}>
              + Nowy projekt
            </button>
          </>
        }
      />

      <div className="projects-header">
        <h1 className="projects-title">Projekty</h1>
        <p className="projects-sub">Twoje przestrzenie robocze</p>
      </div>

      <div className="projects-toolbar">
        <div className="search-box">
          <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="11" cy="11" r="8" />
            <path d="m21 21-4.35-4.35" />
          </svg>
          <input
            placeholder="Szukaj projektu..."
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        <div className="filter-tabs">
          {filters.map((f) => (
            <button
              key={f.value}
              className={`filter-tab ${state.filter === f.value ? 'active' : ''}`}
              onClick={() => setFilter(f.value)}
            >
              {f.label}
            </button>
          ))}
        </div>

        <div style={{ flex: 1 }} />

        <button className="btn-new" onClick={() => setIsModalOpen(true)}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" width="14" height="14">
            <path d="M12 5v14M5 12h14" />
          </svg>
          Nowy projekt
        </button>
      </div>

      <div className="projects-grid">
        {filteredProjects.length === 0 ? (
          <div className="empty-state">Brak projektów. Utwórz nowy projekt.</div>
        ) : (
          filteredProjects.map((project) => (
            <ProjectCard key={project.id} project={project} onClick={onOpenProject} />
          ))
        )}
      </div>

      <NewProjectModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        onCreate={handleCreate}
      />
    </div>
  );
};
