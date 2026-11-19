import { shellQuote } from '@/shared/shellQuote';
import type { DeployedInstance } from '../model/Executionmodel';

export interface Preset {
  label: string;
  cmd: (fileName: string) => string;
  needsFile?: boolean;
  uploadOnly?: boolean;
  unixOnly?: boolean;
}

export interface PresetGroup {
  group: string;
  items: Preset[];
}

const base = (name: string) => name.replace(/\.[^.]+$/, '');
const q = shellQuote;

export const PRESET_GROUPS: PresetGroup[] = [
  {
    group: 'Java',
    items: [
      { label: 'javac', cmd: (f) => `javac ${q(f)}`, needsFile: true },
      { label: 'java', cmd: (f) => `java ${q(base(f))}`, needsFile: true },
      { label: 'java -jar', cmd: (f) => `java -jar ${q(`${base(f)}.jar`)}`, needsFile: true },
      { label: 'mvn package', cmd: () => 'mvn clean package' },
      { label: 'gradle build', cmd: () => './gradlew build' },
      { label: 'usuń .class', cmd: (f) => `rm -f ${q(`${base(f)}.class`)}`, needsFile: true },
    ],
  },
  {
    group: 'Python',
    items: [
      { label: 'python3', cmd: (f) => `python3 ${q(f)}`, needsFile: true },
      { label: 'pip install', cmd: () => 'pip3 install -r requirements.txt' },
      { label: 'venv', cmd: () => 'python3 -m venv venv' },
      { label: 'pytest', cmd: () => 'pytest' },
    ],
  },
  {
    group: 'C++',
    items: [
      { label: 'g++', cmd: (f) => `g++ -o ${q(base(f))} ${q(f)}`, needsFile: true },
      { label: 'uruchom', cmd: (f) => `./${q(base(f))}`, needsFile: true },
      { label: 'make', cmd: () => 'make' },
      { label: 'make clean', cmd: () => 'make clean' },
    ],
  },
  {
    group: 'System',
    items: [
      { label: 'Nadaj prawo uruchamiania', cmd: (f) => `chmod +x -- ${q(f)}`, needsFile: true, unixOnly: true },
      { label: 'Uruchom plik', cmd: (f) => `./${q(f)}`, needsFile: true, uploadOnly: true, unixOnly: true },
      { label: 'ls', cmd: () => 'ls -la' },
      { label: 'pwd', cmd: () => 'pwd' },
      { label: 'df', cmd: () => 'df -h' },
      { label: 'ps', cmd: () => 'ps aux' },
      { label: 'free', cmd: () => 'free -h' },
      { label: 'top', cmd: () => 'top -bn1 | head -20' },
    ],
  },
];

const RULES: ((preset: Preset, instance: DeployedInstance) => boolean)[] = [
  (p, i) => !p.needsFile || i.fileName !== null,
  (p, i) => !p.uploadOnly || i.isUpload,
  (p, i) => !p.unixOnly || i.operationSystem !== 'WINDOWS',
];

export const isPresetAvailable = (preset: Preset, instance: DeployedInstance) =>
  RULES.every((rule) => rule(preset, instance));

export const presetCommand = (preset: Preset, instance: DeployedInstance) => preset.cmd(instance.fileName ?? '');
