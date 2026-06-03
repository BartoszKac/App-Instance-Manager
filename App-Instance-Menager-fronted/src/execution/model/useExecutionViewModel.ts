// src/execution/viewmodel/useExecutionViewModel.ts
import { useState } from 'react';
import { DeployedInstance } from '@/editor/model/EditorState';

export const useExecutionViewModel = () => {
  const [instances] = useState<DeployedInstance[]>([
    { id: '1', fileName: 'Main.java', serverName: 'Prod-Server-1', path: '/var/www/app/java/', lang: 'java', status: 'online' },
    { id: '2', fileName: 'api.py', serverName: 'Python-Host', path: '/home/ubuntu/api/', lang: 'py', status: 'online' },
    { id: '3', fileName: 'server.ts', serverName: 'Prod-Server-1', path: '/var/www/app/node/', lang: 'ts', status: 'offline' },
  ]);

  const executeCommand = (instanceId: string, command: string) => {
    console.log(`Executing on ${instanceId}: ${command}`);
    // Logika SSH/Backend
  };

  return {
    instances,
    executeCommand
  };
};