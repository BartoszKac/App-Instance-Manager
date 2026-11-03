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
    key: `${device.ip}:${s.port}`, ip: device.ip, hostname: details.hostname ?? device.hostname, port: s.port,
    service: PORT_LABEL[s.port] ?? s.name ?? String(s.port),
    product: [s.product, s.version].filter(Boolean).join(' '),
    title: s.title ?? s.tlsSubject ?? '',
  }));

const toRows = (devices: DiscoveredDevice[], details: Record<string, DeviceDetails>) =>
  devices.flatMap((device) => {
    const found = details[device.ip];
    return found && found.services.length > 0 ? rowsFromDetails(device, found) : rowsFromPorts(device);
  });

const defaultName = (row: DetectedServiceRow) =>
  `${row.product || row.service} (${row.hostname ?? row.ip})`.slice(0, 100);

export const useServicesViewModel = () => {
  const lan = useLanScan();
  const managed = useManagedServices();
  const { loadSubnets, reset } = lan;
  const { add, setError } = managed;

  useEffect(() => {
    void loadSubnets();
    return reset;
  }, [loadSubnets, reset]);

  const rows = useMemo(() => toRows(lan.result?.devices ?? [], lan.details), [lan.result, lan.details]);
  const savedKeys = useMemo(() => new Set(managed.services.map((s) => `${s.host}:${s.port}`)), [managed.services]);

  const saveDetected = useCallback(async (row: DetectedServiceRow) => {
    try {
      await add({ name: defaultName(row), host: row.ip, port: row.port, description: row.title || null });
    } catch (err: unknown) {
      setError(errorMessage(err, 'Nie udało się zapisać serwisu.'));
    }
  }, [add, setError]);

  return { lan, managed, rows, savedKeys, saveDetected };
};

export type ServicesViewModel = ReturnType<typeof useServicesViewModel>;
