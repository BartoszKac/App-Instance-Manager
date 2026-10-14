import type { DeployedInstance } from './Executionmodel';

export interface Preset {
  label: string;
  cmd: (inst: DeployedInstance) => string;
}

export interface PresetGroup {
  group: string;
  items: Preset[];
}

const base = (name: string) => name.replace(/\.[^.]+$/, '');

export const PRESET_GROUPS: PresetGroup[] = [
  {
    group: 'Java',
    items: [
      { label: 'javac', cmd: (i) => `javac ${i.fileName}` },
      { label: 'java', cmd: (i) => `java ${base(i.fileName)}` },
      { label: 'java -jar', cmd: (i) => `java -jar ${base(i.fileName)}.jar` },
      { label: 'mvn package', cmd: () => 'mvn clean package' },
      { label: 'gradle build', cmd: () => './gradlew build' },
      { label: 'usuń .class', cmd: (i) => `rm -f ${base(i.fileName)}.class` },
    ],
  },
  {
    group: 'Python',
    items: [
      { label: 'python3', cmd: (i) => `python3 ${i.fileName}` },
      { label: 'pip install', cmd: () => 'pip3 install -r requirements.txt' },
      { label: 'venv', cmd: () => 'python3 -m venv venv' },
      { label: 'pytest', cmd: () => 'pytest' },
    ],
  },
  {
    group: 'C++',
    items: [
      { label: 'g++', cmd: (i) => `g++ -o ${base(i.fileName)} ${i.fileName}` },
      { label: 'uruchom', cmd: (i) => `./${base(i.fileName)}` },
      { label: 'make', cmd: () => 'make' },
      { label: 'make clean', cmd: () => 'make clean' },
    ],
  },
  {
    group: 'System',
    items: [
      { label: 'ls', cmd: () => 'ls -la' },
      { label: 'pwd', cmd: () => 'pwd' },
      { label: 'df', cmd: () => 'df -h' },
      { label: 'ps', cmd: () => 'ps aux' },
      { label: 'free', cmd: () => 'free -h' },
      { label: 'top', cmd: () => 'top -bn1 | head -20' },
    ],
  },
];
