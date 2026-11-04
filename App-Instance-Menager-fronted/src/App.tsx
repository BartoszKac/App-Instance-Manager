import { useState } from 'react';
import { useRouter } from '@/router';
import type { Screen } from '@/router';
import { ProjectsView, EditorView } from '@/editor/views';
import { DeployView } from '@/deploy/views';
import { ExecutionView } from '@/execution/views/ExecutionView';
import { UploadView } from '@/process/views/UploadView';
import { ServicesView } from '@/services/views/ServicesView';
import { AdminView } from '@/admin/views/AdminView';
import PackageTerminal from '@/terminal/PackageTerminal';
import { AppShell } from '@/shared/ui/AppShell';
import type { StationId } from '@/shared/ui/PipelineRail';

import '@/styles/global.css';

const STATION_OF: Record<Screen, StationId> = {
  projects: 'code',
  editor: 'code',
  upload: 'upload',
  deploy: 'deploy',
  execution: 'execution',
  services: 'services',
  admin: 'admin',
};

const SCREEN_OF: Record<StationId, Screen> = {
  code: 'projects',
  upload: 'upload',
  deploy: 'deploy',
  execution: 'execution',
  services: 'services',
  admin: 'admin',
};

const App = () => {
  const { state, navigateTo } = useRouter();
  const toProjects = () => navigateTo('projects');
  const screenKey = state.screen === 'editor' ? `editor-${state.currentProject?.id}` : state.screen;
  const isUploadVisible = state.screen === 'upload';
  const [isUploadBusy, setIsUploadBusy] = useState(false);
  const keepAlive = {
    content: <UploadView isActive={isUploadVisible} onBusyChange={setIsUploadBusy} />,
    isVisible: isUploadVisible,
    isBusy: isUploadBusy,
    station: 'upload' as const,
  };

  return (
    <AppShell
      station={STATION_OF[state.screen]}
      screenKey={screenKey}
      onNavigate={(id) => navigateTo(SCREEN_OF[id])}
      console={<PackageTerminal />}
      keepAlive={keepAlive}
    >
      {state.screen === 'projects' && <ProjectsView onOpenProject={(p) => navigateTo('editor', p)} />}
      {state.screen === 'editor' && state.currentProject && (
        <EditorView project={state.currentProject} onBack={toProjects} />
      )}
      {state.screen === 'deploy' && <DeployView />}
      {state.screen === 'execution' && <ExecutionView />}
      {state.screen === 'services' && <ServicesView />}
      {state.screen === 'admin' && <AdminView />}
    </AppShell>
  );
};

export default App;
