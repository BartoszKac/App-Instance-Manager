// src/App.tsx
import React from 'react';
import { useRouter } from '@/router';
import { ProjectsView, EditorView } from '@/editor/views';
import { Project } from '@/editor/model';
import { DeployView } from '@/deploy/views';
// Importujemy z nowej lokalizacji
import { ExecutionView } from '@/execution/views/ExecutionView';

import '@/styles/global.css';

const App: React.FC = () => {
  const { state, navigateTo } = useRouter();

  const handleOpenProject = (project: Project) => navigateTo('editor', project);
  const handleBack        = ()                  => navigateTo('projects');
  const handleDeploy      = ()                  => navigateTo('deploy');
  const handleExecution   = ()                  => navigateTo('execution');

  return (
    <div className="app">
      {state.screen === 'projects' && (
        <ProjectsView
          onOpenProject={handleOpenProject}
          onDeploy={handleDeploy}
          onExecution={handleExecution}
        />
      )}

      {state.screen === 'editor' && state.currentProject && (
        <EditorView
          project={state.currentProject}
          onBack={handleBack}
          onDeploy={handleDeploy}
          onExecution={handleExecution}
        />
      )}

      {state.screen === 'deploy' && (
        <DeployView onBack={handleBack} onExecution={handleExecution} />
      )}

      {state.screen === 'execution' && (
        <ExecutionView
          onProjects={handleBack}
          onDeploy={handleDeploy}
        />
      )}
    </div>
  );
};

export default App;