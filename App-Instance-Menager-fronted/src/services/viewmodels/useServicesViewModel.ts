import { useCallback, useEffect, useMemo } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { useLanScan } from '@/shared/network/useLanScan';
import { PORT_LABEL } from '@/shared/network/model';
import type { DeviceDetails, DiscoveredDevice } from '@/shared/network/model';
import type { DetectedServiceRow } from '../model/ManagedService';
import { useManagedServices } from './useManagedServices';

const rowsFromPorts = (device: DiscoveredDevice): DetectedServiceRow[] =>
  device.openPorts.map((port) => ({
    key: `${device.ip}:${port}`, ip: device.ip, hostname: device.hostname, port,
    service: PORT_LABEL[port] ?? String(port), product: '', title: '',
  }));

const rowsFromDetails = (device: DiscoveredDevice, details: DeviceDetails): DetectedServiceRow[] =>
  details.services.map((s) => ({
    key: `${device.ip}:${s.port}`, ip: device.ip, hostname: device.hostname ?? details.hostname, port: s.port,
    service: PORT_LABEL[s.port] ?? s.name ?? String(s.port),
    product: [s.product, s.version].filter(Boolean).join(' '),
    title: s.title ?? s.tlsSubject ?? '',
  }));

const rowsOf = (device: DiscoveredDevice, details?: DeviceDetails) => {
  const detailed = details ? rowsFromDetails(device, details) : [];
  const known = new Set(detailed.map((row) => row.port));
  const pending = rowsFromPorts(device).filter((row) => !known.has(row.port));
  return [...detailed, ...pending].sort((a, b) => a.port - b.port);
};

const toRows = (devices: DiscoveredDevice[], details: Record<string, DeviceDetails>) =>
  devices.flatMap((device) => rowsOf(device, details[device.ip]));

const defaultName = (row: DetectedServiceRow) =>
  `${row.product || row.service} (${row.hostname ?? row.ip})`.slice(0, 100);

export const useServicesViewModel = () => {
  const lan = useLanScan();
  const managed = useManagedServices();
  const { load } = lan;
  const { add, setError } = managed;

  useEffect(() => {
    void load();
  }, [load]);

  const rows = useMemo(() => toRows(lan.job?.devices ?? [], lan.job?.details ?? {}), [lan.job]);
  const savedKeys = useMemo(() => new Set(managed.services.map((s) => `${s.host}:${s.port}`)), [managed.services]);

  const saveDetected = useCallback(async (row: DetectedServiceRow) => {
    try {
      await add({ name: defaultName(row), host: row.ip, port: row.port, description: row.title.slice(0, 255) || null });
    } catch (err: unknown) {
      setError(errorMessage(err, 'Nie udało się zapisać serwisu.'));
    }
  }, [add, setError]);

  return { lan, managed, rows, savedKeys, saveDetected };
};

export type ServicesViewModel = ReturnType<typeof useServicesViewModel>;
