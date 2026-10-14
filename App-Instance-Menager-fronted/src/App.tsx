import { useRouter } from '@/router';
import type { Screen } from '@/router';
import { ProjectsView, EditorView } from '@/editor/views';
import { DeployView } from '@/deploy/views';
import { ExecutionView } from '@/execution/views/ExecutionView';
import { UploadView } from '@/process/views/UploadView';
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
};

const SCREEN_OF: Record<StationId, Screen> = {
  code: 'projects',
  upload: 'upload',
  deploy: 'deploy',
  execution: 'execution',
};

const App = () => {
  const { state, navigateTo } = useRouter();
  const toProjects = () => navigateTo('projects');
  const screenKey = state.screen === 'editor' ? `editor-${state.currentProject?.id}` : state.screen;

  return (
    <AppShell
      station={STATION_OF[state.screen]}
      screenKey={screenKey}
      onNavigate={(id) => navigateTo(SCREEN_OF[id])}
      console={<PackageTerminal />}
    >
      {state.screen === 'projects' && <ProjectsView onOpenProject={(p) => navigateTo('editor', p)} />}
      {state.screen === 'editor' && state.currentProject && (
        <EditorView project={state.currentProject} onBack={toProjects} />
      )}
      {state.screen === 'deploy' && <DeployView />}
      {state.screen === 'execution' && <ExecutionView />}
      {state.screen === 'upload' && <UploadView />}
    </AppShell>
  );
};

export default App;
