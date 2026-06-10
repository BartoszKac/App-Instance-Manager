// src/App.tsx
import React from 'react';
import { useRouter } from '@/router';
import { ProjectsView, EditorView } from '@/editor/views';
import { Project } from '@/editor/model';
import { DeployView } from '@/deploy/views';
import { ExecutionView } from '@/execution/views/ExecutionView';
import { UploadView } from '@/process/views/UploadView';
import PackageTerminal from '@/terminal/PackageTerminal';

import '@/styles/global.css';

const App: React.FC = () => {
  const { state, navigateTo } = useRouter();

  const handleOpenProject = (project: Project) => navigateTo('editor', project);
  const handleBack        = ()                  => navigateTo('projects');
  const handleDeploy      = ()                  => navigateTo('deploy');
  const handleExecution   = ()                  => navigateTo('execution');
  const handleUpload      = ()                  => navigateTo('upload');

  return (
    <div className="app" style={{ display: 'flex', flexDirection: 'column', height: '100vh' }}>

      <div style={{ flex: 1, overflow: 'auto' }}>
        {state.screen === 'projects' && (
          <ProjectsView
            onOpenProject={handleOpenProject}
            onDeploy={handleDeploy}
            onExecution={handleExecution}
            onUpload={handleUpload}
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

        {state.screen === 'upload' && (
          <UploadView onBack={handleBack} />
        )}
      </div>

      <PackageTerminal />

    </div>
  );
};

export default App;