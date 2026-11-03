import { useMemo, useState } from 'react';
import { useCredentialsForm } from './useCredentialsForm';
import { useFileSearch } from './useFileSearch';
import { useFileTree } from './useFileTree';
import { useInspectionDetails } from './useInspectionDetails';
import { useInspectionServers } from './useInspectionServers';

export const FILES_TAB = 'Pliki';
export const UPLOAD_TAB = 'Wyślij';

export const useAdminViewModel = () => {
  const servers = useInspectionServers();
  const serverId = servers.selected?.serverId ?? null;
  const dataFrom = servers.selected?.dataFrom ?? null;
  const details = useInspectionDetails(servers.selected);
  const tree = useFileTree(serverId, dataFrom);
  const search = useFileSearch(dataFrom === null ? null : serverId);
  const credentials = useCredentialsForm(servers.selected, servers.refresh);
  const [tab, setTab] = useState(FILES_TAB);

  const sections = useMemo(() => details.details?.sections ?? [], [details.details]);
  const categories = useMemo(() => [...new Set(sections.map((section) => section.category))], [sections]);
  const tabs = useMemo(() => [FILES_TAB, UPLOAD_TAB, ...categories], [categories]);
  const activeTab = tabs.includes(tab) ? tab : FILES_TAB;
  const visibleSections = useMemo(() => sections.filter((section) => section.category === activeTab), [sections, activeTab]);

  return {
    servers,
    tree,
    search,
    credentials,
    tabs,
    activeTab,
    setTab,
    visibleSections,
    fileCount: details.details?.fileCount ?? 0,
    error: servers.error ?? details.error ?? tree.error ?? search.error,
  };
};

export type AdminViewModel = ReturnType<typeof useAdminViewModel>;
