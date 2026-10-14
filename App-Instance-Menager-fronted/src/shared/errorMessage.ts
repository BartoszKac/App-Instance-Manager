export const errorMessage = (err: unknown, fallback = 'Nieznany błąd'): string => {
  if (err instanceof Error && err.message) return err.message;
  if (typeof err === 'string' && err) return err;
  return fallback;
};
