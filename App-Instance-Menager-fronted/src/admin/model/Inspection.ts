export type InspectionState = 'QUEUED' | 'RUNNING' | 'DONE' | 'FAILED';

export type InspectionFailureKind = 'LOGIN_REJECTED' | 'HOST_KEY_CHANGED' | 'OTHER';

export interface InspectionStatus {
  serverId: number;
  name: string;
  ip: string;
  user: string;
  state: InspectionState | null;
  stage: string | null;
  filesFound: number;
  error: string | null;
  failureKind: InspectionFailureKind | null;
  dataFrom: string | null;
  truncated: boolean;
}

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

export interface InspectedFile {
  path: string;
  name: string;
  type: string;
  sizeBytes: number;
  owner: string;
  group: string;
  mode: string;
  modifiedEpochSeconds: number;
  denied: boolean;
}

export interface FilePage {
  entries: InspectedFile[];
  total: number;
}
