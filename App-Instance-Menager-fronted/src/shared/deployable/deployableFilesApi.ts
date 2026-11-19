import { apiFetch } from '@/shared/http';
import type { DeployableFiles } from './model';

export const fetchDeployableFiles = () => apiFetch<DeployableFiles>('/api/deploy/deployable-files');
