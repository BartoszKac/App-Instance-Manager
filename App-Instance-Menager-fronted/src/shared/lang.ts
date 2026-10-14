export type LangKey = 'java' | 'py' | 'cpp' | 'sh' | 'other';

const ALIASES: Record<string, LangKey> = {
  java: 'java',
  JAVA: 'java',
  py: 'py',
  PYTHON: 'py',
  python: 'py',
  cpp: 'cpp',
  cc: 'cpp',
  CPP: 'cpp',
  sh: 'sh',
  BASH: 'sh',
  bash: 'sh',
};

export const LANG_LABEL: Record<LangKey, string> = {
  java: 'Java',
  py: 'Python',
  cpp: 'C++',
  sh: 'Bash',
  other: 'Plik',
};

export const toLangKey = (value: string | null | undefined): LangKey =>
  (value && ALIASES[value]) || 'other';

export const langClass = (value: string | null | undefined): string => `lang--${toLangKey(value)}`;
