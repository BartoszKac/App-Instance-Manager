import { useState } from 'react';
import type { Project, Language } from '@/editor/model';
import { useProjects } from '@/editor/viewmodels';
import { ProjectRow, NewProjectModal } from '@/editor/components';
import { ScreenHeader } from '@/shared/ui/ScreenHeader';
import { Icon } from '@/shared/ui/Icon';
import { EmptyState, Notice, Spinner } from '@/shared/ui/atoms';

interface ProjectsViewProps {
  onOpenProject: (project: Project) => void;
}

const FILTERS: { label: string; value: Language | 'all' }[] = [
  { label: 'Wszystkie', value: 'all' },
  { label: 'Java', value: 'java' },
  { label: 'Python', value: 'py' },
  { label: 'C++', value: 'cpp' },
  { label: 'Bash', value: 'sh' },
];

const countLabel = (n: number) => (n === 1 ? '1 projekt' : n % 10 >= 2 && n % 10 <= 4 && (n < 10 || n > 20) ? `${n} projekty` : `${n} projektów`);

export const ProjectsView = ({ onOpenProject }: ProjectsViewProps) => {
  const { filteredProjects, state, setFilter, setSearchQuery, createProject, loading, error } = useProjects();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const openModal = () => setIsModalOpen(true);

  return (
    <>
      <ScreenHeader
        title="Projekty"
        context={loading ? 'Wczytywanie projektów z serwera…' : `${countLabel(state.projects.length)} na serwerze`}
        actions={<button className="btn btn--primary" onClick={openModal}><Icon name="add" />Nowy projekt</button>}
      />
      <div className="projects" data-enter="body">
        <div className="projects__toolbar">
          <label className="search">
            <Icon name="search" size={16} />
            <span className="visually-hidden">Szukaj projektu</span>
            <input type="search" placeholder="Szukaj po nazwie lub opisie" onChange={(e) => setSearchQuery(e.target.value)} />
          </label>
          <div className="segmented" role="group" aria-label="Filtruj po języku">
            {FILTERS.map((f) => (
              <button key={f.value} className="segmented__item" aria-pressed={state.filter === f.value} onClick={() => setFilter(f.value)}>
                {f.label}
              </button>
            ))}
          </div>
        </div>
        {error && <div className="projects__notice"><Notice tone="error">Nie udało się wczytać projektów: {error}. Sprawdź, czy backend działa na porcie 8888.</Notice></div>}
        {loading && <div className="projects__loading"><Spinner />Wczytywanie…</div>}
        {!loading && filteredProjects.length === 0 && (
          <EmptyState
            title={state.projects.length ? 'Nic nie pasuje do filtra' : 'Nie masz jeszcze projektów'}
            hint={state.projects.length ? 'Zmień wyszukiwanie albo język.' : 'Utwórz projekt, napisz kod i uruchom go jednym kliknięciem.'}
            action={!state.projects.length && <button className="btn btn--primary" onClick={openModal}>Utwórz pierwszy projekt</button>}
          />
        )}
        {filteredProjects.length > 0 && (
          <ul className="project-list">
            {filteredProjects.map((p) => <ProjectRow key={p.id} project={p} onOpen={onOpenProject} />)}
          </ul>
        )}
      </div>
      {isModalOpen && (
        <NewProjectModal onClose={() => setIsModalOpen(false)} onCreate={(name, desc, lang) => onOpenProject(createProject(name, desc, lang))} />
      )}
    </>
  );
};
