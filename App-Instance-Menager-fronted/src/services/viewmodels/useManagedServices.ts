import { useCallback, useEffect, useState } from 'react';
import { errorMessage } from '@/shared/errorMessage';
import { createService, deleteService, fetchServices } from '../api/servicesApi';
import { EMPTY_SERVICE_FORM } from '../model/ManagedService';
import type { ManagedService, ManagedServiceForm } from '../model/ManagedService';

export const useManagedServices = () => {
  const [services, setServices] = useState<ManagedService[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [form, setForm] = useState<ManagedServiceForm>(EMPTY_SERVICE_FORM);
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    let cancelled = false;
    fetchServices()
      .then((found) => { if (!cancelled) setServices(found); })
      .catch((err: unknown) => { if (!cancelled) setError(errorMessage(err, 'Nie udało się pobrać serwisów.')); })
      .finally(() => { if (!cancelled) setIsLoading(false); });
    return () => { cancelled = true; };
  }, []);

  const add = useCallback(async (service: Omit<ManagedService, 'id'>) => {
    setError(null);
    const saved = await createService(service);
    setServices((prev) => [...prev, saved]);
  }, []);

  const saveForm = useCallback(async () => {
    setIsSaving(true);
    try {
      await add({ name: form.name, host: form.host, port: Number(form.port), description: form.description || null });
      setForm(EMPTY_SERVICE_FORM);
      setIsFormOpen(false);
    } catch (err: unknown) {
      setError(errorMessage(err, 'Nie udało się dodać serwisu.'));
    } finally {
      setIsSaving(false);
    }
  }, [add, form]);

  const remove = useCallback(async (id: number) => {
    setError(null);
    try {
      await deleteService(id);
      setServices((prev) => prev.filter((s) => s.id !== id));
    } catch (err: unknown) {
      setError(errorMessage(err, 'Nie udało się usunąć serwisu.'));
    }
  }, []);

  return { services, isLoading, error, setError, isFormOpen, setIsFormOpen, form, setForm, isSaving, add, saveForm, remove };
};
