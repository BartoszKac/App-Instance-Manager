import { useCallback, useRef, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { fetchDeviceDetails } from '@/shared/network/networkApi';
import type { DeviceDetails } from '@/shared/network/model';
import { fetchServerInventory } from '../api/serversApi';
import { getServerId } from '../model/DeployState';
import type { DeployServer } from '../model/DeployState';
import type { ServerInventory } from '../model/ServerInventory';

export interface ServerPreviewViewModel {
  server: DeployServer | null;
  inventory: ServerInventory | null;
  isLoadingInventory: boolean;
  inventoryError: string | null;
  network: DeviceDetails | null;
  isLoadingNetwork: boolean;
  networkError: string | null;
  open: (server: DeployServer) => void;
  close: () => void;
}

export const useServerPreview = (): ServerPreviewViewModel => {
  const [server, setServer] = useState<DeployServer | null>(null);
  const [inventory, setInventory] = useState<ServerInventory | null>(null);
  const [isLoadingInventory, setIsLoadingInventory] = useState(false);
  const [inventoryError, setInventoryError] = useState<string | null>(null);
  const [network, setNetwork] = useState<DeviceDetails | null>(null);
  const [isLoadingNetwork, setIsLoadingNetwork] = useState(false);
  const [networkError, setNetworkError] = useState<string | null>(null);
  const session = useRef(0);

  const loadInventory = useCallback((id: number, current: number) => {
    setIsLoadingInventory(true);
    fetchServerInventory(id)
      .then((found) => { if (session.current === current) setInventory(found); })
      .catch((err: unknown) => { if (session.current === current) setInventoryError(errorMessage(err, 'Nie udało się pobrać danych serwera.')); })
      .finally(() => { if (session.current === current) setIsLoadingInventory(false); });
  }, []);

  const loadNetwork = useCallback((ip: string, current: number) => {
    setIsLoadingNetwork(true);
    fetchDeviceDetails(ip)
      .then((found) => { if (session.current === current) setNetwork(found); })
      .catch((err: unknown) => { if (session.current === current) setNetworkError(errorMessage(err, 'Nie udało się przeskanować serwera.')); })
      .finally(() => { if (session.current === current) setIsLoadingNetwork(false); });
  }, []);

  const open = useCallback((target: DeployServer) => {
    session.current += 1;
    setServer(target);
    setInventory(null);
    setNetwork(null);
    setInventoryError(null);
    setNetworkError(null);
    loadInventory(getServerId(target), session.current);
    loadNetwork(target.ip, session.current);
  }, [loadInventory, loadNetwork]);

  const close = useCallback(() => {
    session.current += 1;
    setServer(null);
  }, []);

  return { server, inventory, isLoadingInventory, inventoryError, network, isLoadingNetwork, networkError, open, close };
};
