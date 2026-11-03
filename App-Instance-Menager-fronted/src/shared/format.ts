const UNITS = ['B', 'KiB', 'MiB', 'GiB', 'TiB'];
const STEP = 1024;

export const formatBytes = (bytes: number): string => {
  const exponent = bytes > 0 ? Math.min(Math.floor(Math.log(bytes) / Math.log(STEP)), UNITS.length - 1) : 0;
  const value = bytes / STEP ** exponent;
  return `${exponent === 0 ? value : value.toFixed(1)} ${UNITS[exponent]}`;
};

export const formatEpoch = (epochSeconds: number): string =>
  epochSeconds > 0 ? new Date(epochSeconds * 1000).toLocaleString('pl-PL') : '';

export const formatIso = (iso: string): string => new Date(iso).toLocaleString('pl-PL');
