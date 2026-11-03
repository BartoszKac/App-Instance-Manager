import type { InspectionState } from '../model/Inspection';
import type { Tone } from '@/shared/ui/atoms';


export const STATE_LABEL: Record<InspectionState, string> = {
  QUEUED: 'W kolejce',
  RUNNING: 'Badanie…',
  DONE: 'Zbadany',
  FAILED: 'Błąd',
};

export const STATE_TONE: Record<InspectionState, Tone> = {
  QUEUED: 'idle',
  RUNNING: 'warn',
  DONE: 'ok',
  FAILED: 'error',
};

export const STAGE_LABEL: Record<string, string> = {
  CONNECTING: 'Łączenie przez SSH',
  FACTS: 'Zbieranie danych systemu',
  FILES: 'Listowanie plików',
};
