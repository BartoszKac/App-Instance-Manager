import type { DeployableFiles, LanguageType, UploadStrategyType } from '@/shared/deployable/model';
import type { DeployedInstance, ProgramConfig, ServerConfig } from '../model/Executionmodel';

interface SentFile {
  name: string;
  language: LanguageType;
}

type FileLookup = (files: DeployableFiles, idCode: number) => SentFile | null;

const asClassFile = (name: string) => name.replace(/\.java$/, '.class');

const projectFile = (files: DeployableFiles, idCode: number) =>
  files.projects.find((p) => p.mainClassId === idCode) ?? null;

const FILE_LOOKUP: Record<UploadStrategyType, FileLookup> = {
  SOURCE_CODE: (files, idCode) => projectFile(files, idCode),
  COMPILED_CODE: (files, idCode) => {
    const project = projectFile(files, idCode);
    return project ? { name: asClassFile(project.name), language: project.language } : null;
  },
  REMOTE_EXECUTABLE: (files, idCode) => files.uploads.find((u) => u.id === idCode) ?? null,
};

const findSentFile = (cfg: ProgramConfig, files: DeployableFiles): SentFile | null =>
  FILE_LOOKUP[cfg.uploadStrategyType]?.(files, cfg.idCode) ?? null;

export const mapConfigToInstance = (cfg: ProgramConfig, servers: ServerConfig[], files: DeployableFiles): DeployedInstance => {
  const server = servers.find((s) => s.idConfiguration === cfg.idSerwer);
  const file = findSentFile(cfg, files);
  return {
    id: String(cfg.idConfiguration),
    idConfiguration: cfg.idConfiguration,
    fileName: file?.name ?? null,
    isUpload: cfg.uploadStrategyType === 'REMOTE_EXECUTABLE',
    serverName: server?.name ?? `Serwer #${cfg.idSerwer}`,
    operationSystem: server?.operationSystem ?? null,
    path: cfg.pathInServer,
    lang: file?.language ?? 'UNKNOWN',
    status: 'online',
  };
};
