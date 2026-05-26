const BASE_URL = 'http://localhost:8888/app';

export const saveJavaFiles = async (filesArray) => {
  const response = await fetch(`${BASE_URL}/`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(filesArray),
  });
  if (!response.ok) throw new Error('Błąd zapisu projektu');
  return response;
};

export const fetchServerFiles = async () => {
  const response = await fetch(`${BASE_URL}/info`);
  if (!response.ok) throw new Error('Nie udało się pobrać listy plików');
  const text = await response.text();
  return text.replace(/[\[\]]/g, '').split(/[, \n]+/).filter(f => f.trim().length > 0);
};

export const compileJavaFile = async (fileName, ext = '.java') => {
  const cleanName = fileName.replace(ext, '');
  const response = await fetch(`${BASE_URL}/compile/${cleanName}?ext=${encodeURIComponent(ext)}`);
  if (!response.ok) throw new Error('Błąd komunikacji z endpointem kompilacji');
  return response;
};

export const deleteJavaFile = async (fileName, ext = '.java') => {
  const cleanName = fileName.replace(ext, '');
  const response = await fetch(`${BASE_URL}/delete/${cleanName}?ext=${encodeURIComponent(ext)}`);
  if (!response.ok) throw new Error('Błąd podczas usuwania pliku');
  return response;
};

// ZMIANA: dodano opcjonalne localPort i remotePort dla port forwardingu
export const deployToRemote = async (fullFileName, configName, localPort, remotePort) => {
  const lastDot = fullFileName.lastIndexOf('.');
  const nameOnly = fullFileName.substring(0, lastDot);
  const extension = fullFileName.substring(lastDot);

  let url = `${BASE_URL}/deploy/${encodeURIComponent(nameOnly)}?ext=${encodeURIComponent(extension)}&configName=${encodeURIComponent(configName)}`;

  if (localPort && remotePort) {
    url += `&localPort=${localPort}&remotePort=${remotePort}`;
  }

  const response = await fetch(url);
  if (!response.ok) throw new Error('Błąd deploymentu (sprawdź logi i config na serwerze)');
  return response.text();
};

export const fetchDeployConfig = async () => {
  const response = await fetch(`${BASE_URL}/config`);
  if (!response.ok) throw new Error('Błąd pobierania konfiguracji');
  return response.json();
};

export const saveDeployConfig = async (configData) => {
  const response = await fetch(`${BASE_URL}/config`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(configData),
  });
  if (!response.ok) throw new Error('Błąd zapisu konfiguracji');
  return response.text();
};
