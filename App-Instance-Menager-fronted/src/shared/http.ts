import { API_BASE_URL } from '@/shared/apiBase';

const readProblemDetail = async (res: Response): Promise<string | undefined> => {
  try {
    const body = (await res.json()) as { detail?: string };
    return body.detail;
  } catch {
    return undefined;
  }
};

export async function apiFetch<T = void>(path: string, opts?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...opts,
  });
  if (!res.ok) {
    const detail = await readProblemDetail(res);
    throw new Error(detail ?? `${res.status} ${res.statusText} — ${path}`);
  }
  const text = await res.text();
  if (!text) return undefined as T;
  return JSON.parse(text) as T;
}
