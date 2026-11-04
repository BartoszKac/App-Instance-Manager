export interface InventorySection {
  key: string;
  label: string;
  value: string;
}

export interface ServerInventory {
  id: number;
  name: string;
  ip: string;
  user: string;
  connected: boolean;
  error: string | null;
  sections: InventorySection[];
}
