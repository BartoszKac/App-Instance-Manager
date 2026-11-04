export interface ManagedService {
  id: number;
  name: string;
  host: string;
  port: number;
  description: string | null;
}

export interface ManagedServiceForm {
  name: string;
  host: string;
  port: string;
  description: string;
}

export interface DetectedServiceRow {
  key: string;
  ip: string;
  hostname: string | null;
  port: number;
  service: string;
  product: string;
  title: string;
}

export const EMPTY_SERVICE_FORM: ManagedServiceForm = { name: '', host: '', port: '', description: '' };
