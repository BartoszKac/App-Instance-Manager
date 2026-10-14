import type { Project } from '@/editor/model';
import { Sidebar, TabBar, CodeView, StatusBar, BuildStrip } from '@/editor/components';
import type { BuildStepView } from '@/editor/components';
import { useProjectFiles } from '@/editor/viewmodels/useProjectFiles';
import { useBuildActions } from '@/editor/viewmodels/useBuildActions';
import type { Activity } from '@/editor/viewmodels/useBuildActions';
import { ScreenHeader } from '@/shared/ui/ScreenHeader';
import { Icon } from '@/shared/ui/Icon';
import { LangChip } from '@/shared/ui/atoms';

interface EditorViewProps {
  project: Project;
  onBack: () => void;
}

type Session = ReturnType<typeof useProjectFiles>;
type Build = ReturnType<typeof useBuildActions>;

const buildSteps = (session: Session, build: Build): BuildStepView[] => {
  const runReady = !session.dirty && (build.needsCompile ? build.compiled : session.persisted);
  const steps: BuildStepView[] = [
    { id: 'save', label: 'Zapisz', icon: 'save', done: session.persisted && !session.dirty, enabled: session.files.length > 0, hint: 'Dodaj plik, żeby zapisać', onClick: () => build.save(session.files) },
    { id: 'compile', label: 'Kompiluj', icon: 'build', done: build.compiled && !session.dirty, enabled: session.persisted && !session.dirty, hint: 'Najpierw zapisz zmiany', onClick: build.compile },
    { id: 'run', label: 'Uruchom', icon: 'play', done: false, enabled: runReady, hint: build.needsCompile ? 'Zapisz i skompiluj, żeby uruchomić' : 'Zapisz zmiany, żeby uruchomić', onClick: build.run },
  ];
  return build.needsCompile ? steps : steps.filter((s) => s.id !== 'compile');
};

const describeStatus = (session: Session, activity: Activity): { tone: string; text: string } => {
  if (activity.tone === 'busy' || activity.tone === 'error') return activity;
  if (session.dirty) return { tone: 'dirty', text: 'Niezapisane zmiany' };
  if (activity.tone === 'ok') return activity;
  return { tone: 'idle', text: session.persisted ? 'Wszystko zapisane' : 'Nowy projekt, jeszcze nie zapisany' };
};

export const EditorView = ({ project, onBack }: EditorViewProps) => {
  const session = useProjectFiles(project);
  const build = useBuildActions(project.id, project.lang, session.markSaved);
  const status = describeStatus(session, build.activity);

  return (
    <>
      <ScreenHeader
        title={project.name}
        leading={<button className="icon-btn icon-btn--lg" onClick={onBack} aria-label="Wróć do projektów"><Icon name="back" size={20} /></button>}
        context={<span className="editor-status"><LangChip lang={project.lang} /><span className={`editor-status__text editor-status__text--${status.tone}`} role="status">{status.text}</span></span>}
        actions={<BuildStrip steps={buildSteps(session, build)} activity={build.activity} />}
      />
      <div className="editor" data-enter="body">
        <Sidebar files={session.files} activeFile={session.activeFile} onFileClick={session.openFile} onFileRemove={session.removeFile} onFileAdd={session.addFile} />
        <div className="editor__main">
          <TabBar tabs={session.tabs} activeFile={session.activeFile} onTabClick={session.openFile} onTabClose={session.closeTab} />
          <CodeView file={session.activeFile} onContentChange={session.changeContent} onClear={() => session.changeContent('')} />
          <StatusBar file={session.activeFile} dirty={session.dirty} />
        </div>
      </div>
    </>
  );
};
