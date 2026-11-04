import type { Project } from '@/editor/model';
import { LangChip } from '@/shared/ui/atoms';
import { Icon } from '@/shared/ui/Icon';

interface ProjectRowProps {
  project: Project;
  onOpen: (project: Project) => void;
}

const filesLabel = (n: number) => (n === 1 ? '1 plik' : n >= 2 && n <= 4 ? `${n} pliki` : `${n} plików`);

export const ProjectRow = ({ project, onOpen }: ProjectRowProps) => {
  const shown = project.files.slice(0, 3).map((f) => f.name);
  const rest = project.files.length - shown.length;

  return (
    <li>
      <button className="project-row" onClick={() => onOpen(project)}>
        <LangChip lang={project.lang} />
        <span className="project-row__main">
          <span className="project-row__name">{project.name}</span>
          {project.desc && <span className="project-row__desc">{project.desc}</span>}
        </span>
        <span className="project-row__files">
          {shown.join('  ')}
          {rest > 0 && <span className="project-row__more"> +{rest}</span>}
        </span>
        <span className="project-row__count">{filesLabel(project.files.length)}</span>
        <span className="project-row__open">Otwórz<Icon name="chevronRight" size={16} /></span>
      </button>
    </li>
  );
};
