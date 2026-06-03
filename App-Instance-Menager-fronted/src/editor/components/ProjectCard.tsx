// src/editor/components/ProjectCard.tsx
import React from 'react';
import { Project } from '@/editor/model';

interface ProjectCardProps {
  project: Project;
  onClick: (project: Project) => void;
}

export const ProjectCard: React.FC<ProjectCardProps> = ({ project, onClick }) => {
  const fileCount = project.files.length;
  const fileLabel =
    fileCount === 1 ? '1 plik' : fileCount < 5 ? `${fileCount} pliki` : `${fileCount} plików`;

  return (
    <div className="project-card" onClick={() => onClick(project)}>
      <div className={`project-card-lang lang-${project.lang}`}>
        {project.lang.toUpperCase()}
      </div>
      <div className="project-name">{project.name}</div>
      <div className="project-desc">{project.desc}</div>
      <div className="project-meta">
        <div className="project-files">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
            <polyline points="14 2 14 8 20 8" />
          </svg>
          {fileLabel}
        </div>
        <div className="project-date">{project.created}</div>
      </div>
    </div>
  );
};
