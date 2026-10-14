import { useState } from 'react';
import type { Language, ProjectFile, SourceCode } from '../model/EditorState';
import { EXT_TO_LANGUAGE } from '../model/EditorState';
import { CodeService } from '../api/CodeService';
import { errorMessage } from '@/shared/errorMessage';

export type ActivityTone = 'idle' | 'busy' | 'ok' | 'error';
export type BuildStep = 'save' | 'compile' | 'run';

export interface Activity {
  tone: ActivityTone;
  text: string;
  step?: BuildStep;
}

const COMPILED: Language[] = ['java', 'cpp'];

const toSourceCode = (f: ProjectFile, idManClass: number): SourceCode => ({
  id: f.id,
  idManClass,
  name: f.name,
  code: f.content || '',
  language: f.language || EXT_TO_LANGUAGE[f.ext] || 'UNKNOWN',
});

const runTone = (message: string): ActivityTone => (/błęd/i.test(message) ? 'error' : 'ok');

export const useBuildActions = (projectId: number, lang: Language, onSaved: () => void) => {
  const [activity, setActivity] = useState<Activity>({ tone: 'idle', text: '' });
  const [compiled, setCompiled] = useState(false);
  const needsCompile = COMPILED.includes(lang);

  const perform = async (step: BuildStep, busyText: string, action: () => Promise<Activity>) => {
    setActivity({ tone: 'busy', text: busyText, step });
    try {
      setActivity({ ...(await action()), step });
    } catch (err: unknown) {
      setActivity({ tone: 'error', text: errorMessage(err), step });
    }
  };

  const save = (files: ProjectFile[]) => perform('save', 'Zapisywanie…', async () => {
    await CodeService.saveSourceCodes(files.map((f) => toSourceCode(f, projectId)));
    onSaved();
    setCompiled(false);
    return { tone: 'ok', text: 'Zapisano' };
  });

  const compile = () => perform('compile', 'Kompilowanie…', async () => {
    await CodeService.compileAll(projectId);
    const results = await CodeService.getCompiledCodes(projectId);
    const ok = results.length > 0 && !results.some((r) => r.status === 'error');
    setCompiled(ok);
    return ok ? { tone: 'ok', text: 'Skompilowano' } : { tone: 'error', text: 'Kompilacja nie powiodła się — szczegóły w konsoli' };
  });

  const run = () => perform('run', 'Uruchamianie…', async () => {
    const message = await CodeService.launchApp(projectId);
    return { tone: runTone(message), text: message || 'Uruchomiono — wynik w konsoli' };
  });

  const busy = activity.tone === 'busy';
  return { activity, busy, needsCompile, compiled, save, compile, run };
};
