import { useState } from 'react';
import { Project } from '../editor/model/EditorState';

export type Screen = 'projects' | 'editor' | 'deploy' | 'execution';

interface RouterState {
  screen: Screen;
  currentProject: Project | null;
}

export const useRouter = () => {
  const [state, setState] = useState<RouterState>({
    screen: 'projects',
    currentProject: null,
  });

  const navigateTo = (screen: Screen, project?: Project) => {
    setState({
      screen,
      currentProject: project ?? null,
    });
  };

  return { state, navigateTo };
};