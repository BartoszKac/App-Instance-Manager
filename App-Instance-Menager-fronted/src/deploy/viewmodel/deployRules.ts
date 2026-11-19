import type { DeployableFiles, LanguageType, ProjectFile, UploadedFile, UploadStrategyType } from '@/shared/deployable/model';
import type { DeployFile, DeployServer, NewServerForm, StrategyOption } from '../model/DeployState';
import { runHint } from './runHints';

export const STRATEGY_LABEL: Record<UploadStrategyType, string> = {
  SOURCE_CODE: 'Kod',
  COMPILED_CODE: 'Program',
  REMOTE_EXECUTABLE: 'Plik',
};

export const COMPILE_FIRST = 'Najpierw skompiluj';

export const EMPTY_NEW_SERVER: NewServerForm = { name: '', host: '', user: 'deploy', pass: '' };

const COMPILES_HERE: Partial<Record<LanguageType, boolean>> = { JAVA: true };

export const getServerId = (srv: DeployServer): number => (srv.idConfiguration ?? srv.IdConfiguration)!;

const projectOptions = (project: ProjectFile): StrategyOption[] => {
  const source: StrategyOption = { strategy: 'SOURCE_CODE', disabledReason: null };
  if (!COMPILES_HERE[project.language]) return [source];
  return [source, { strategy: 'COMPILED_CODE', disabledReason: project.compiledFileCount > 0 ? null : COMPILE_FIRST }];
};

const pickStrategy = (options: StrategyOption[], chosen: UploadStrategyType | undefined): UploadStrategyType => {
  const enabled = options.filter((o) => o.disabledReason === null).map((o) => o.strategy);
  if (chosen && enabled.includes(chosen)) return chosen;
  return enabled.includes('COMPILED_CODE') ? 'COMPILED_CODE' : 'SOURCE_CODE';
};

const toProjectFile = (project: ProjectFile, chosen: Record<string, UploadStrategyType>): DeployFile => {
  const key = `source::${project.mainClassId}`;
  const options = projectOptions(project);
  const strategy = pickStrategy(options, chosen[key]);
  return {
    key, name: project.name, language: project.language, source: 'source', id: project.mainClassId, strategy, options,
    runHint: runHint({ name: project.name, language: project.language, strategy }),
    description: null, sizeBytes: null, uploadedAt: null,
  };
};

const toUploadedFile = (upload: UploadedFile): DeployFile => ({
  key: `process::${upload.id}`, name: upload.name, language: upload.language, source: 'process', id: upload.id,
  strategy: 'REMOTE_EXECUTABLE', options: [{ strategy: 'REMOTE_EXECUTABLE', disabledReason: null }],
  runHint: runHint({ name: upload.name, language: upload.language, strategy: 'REMOTE_EXECUTABLE' }),
  description: upload.description, sizeBytes: upload.sizeBytes, uploadedAt: upload.uploadedAt,
});

export const toDeployFiles = (files: DeployableFiles | null, chosen: Record<string, UploadStrategyType>): DeployFile[] =>
  files ? [...files.projects.map((p) => toProjectFile(p, chosen)), ...files.uploads.map(toUploadedFile)] : [];
