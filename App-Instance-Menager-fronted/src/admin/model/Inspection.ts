import type { InspectionStatus } from '@/shared/inspection/model';

export type { FilePage, InspectedFile, InspectionFailureKind, InspectionState, InspectionStatus } from '@/shared/inspection/model';

export interface InspectionSection {
  key: string;
  label: string;
  category: string;
  value: string;
}

export interface InspectionDetails {
  status: InspectionStatus;
  sections: InspectionSection[];
  fileCount: number;
}
