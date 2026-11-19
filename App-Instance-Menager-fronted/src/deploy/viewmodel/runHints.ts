import type { LanguageType, UploadStrategyType } from '@/shared/deployable/model';
import { shellQuote } from '@/shared/shellQuote';

export interface HintInput {
  name: string;
  language: LanguageType;
  strategy: UploadStrategyType;
}

type Hint = (name: string) => string;

const base = (name: string) => name.replace(/\.[^.]+$/, '');
const q = shellQuote;

const PROJECT_HINTS: Record<string, Hint> = {
  'SOURCE_CODE:JAVA': (n) => `javac ${q(n)} && java ${q(base(n))}`,
  'COMPILED_CODE:JAVA': (n) => `java ${q(base(n))}`,
  'SOURCE_CODE:PYTHON': (n) => `python3 ${q(n)}`,
  'SOURCE_CODE:CPP': (n) => `g++ -o ${q(base(n))} ${q(n)} && ./${q(base(n))}`,
  'SOURCE_CODE:BASH': (n) => `bash ${q(n)}`,
};

const EXTENSION_HINTS: Record<string, Hint> = {
  jar: (n) => `java -jar ${q(n)}`,
  class: (n) => `java ${q(base(n))}`,
  java: (n) => `java ${q(n)}`,
  py: (n) => `python3 ${q(n)}`,
  sh: (n) => `bash ${q(n)}`,
};

const runFile: Hint = (n) => `chmod +x -- ${q(n)} && ./${q(n)}`;

const extensionOf = (name: string) => (name.includes('.') ? name.slice(name.lastIndexOf('.') + 1).toLowerCase() : '');

export const runHint = ({ name, language, strategy }: HintInput): string => {
  const hint = strategy === 'REMOTE_EXECUTABLE'
    ? EXTENSION_HINTS[extensionOf(name)] ?? runFile
    : PROJECT_HINTS[`${strategy}:${language}`] ?? runFile;
  return hint(name);
};
