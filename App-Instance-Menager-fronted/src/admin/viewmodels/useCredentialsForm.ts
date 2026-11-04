import { useCallback, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { updateCredentials } from '../api/inspectionApi';
import type { InspectionStatus } from '../model/Inspection';

export const useCredentialsForm = (server: InspectionStatus | null, onSaved: () => Promise<void>) => {
  const [isOpen, setIsOpen] = useState(false);
  const [user, setUser] = useState('');
  const [password, setPassword] = useState('');
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const open = useCallback(() => {
    setUser(server?.user ?? '');
    setPassword('');
    setError(null);
    setIsOpen(true);
  }, [server]);

  const close = useCallback(() => {
    setPassword('');
    setIsOpen(false);
  }, []);

  const save = useCallback(async () => {
    if (!server) return;
    setIsSaving(true);
    setError(null);
    try {
      await updateCredentials(server.serverId, { user: user.trim(), password });
      close();
      await onSaved();
    } catch (err: unknown) {
      setError(errorMessage(err, 'Nie udało się zapisać hasła.'));
    } finally {
      setIsSaving(false);
    }
  }, [server, user, password, close, onSaved]);

  const canSave = !isSaving && user.trim() !== '' && password !== '';

  return { isOpen, user, setUser, password, setPassword, isSaving, error, canSave, open, close, save };
};

export type CredentialsForm = ReturnType<typeof useCredentialsForm>;
