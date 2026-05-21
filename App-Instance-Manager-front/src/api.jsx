const BASE_URL = 'http://localhost:8888/app';

// Wysyłanie projektu (wszystkie pliki naraz)
export const saveJavaFiles = async (filesArray) => {
  const response = await fetch(`${BASE_URL}/`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(filesArray),
  });
  if (!response.ok) throw new Error('Błąd zapisu projektu');
  return response;
};

// Lista plików na serwerze
export const fetchServerFiles = async () => {
  const response = await fetch(`${BASE_URL}/info`);
  if (!response.ok) throw new Error('Nie udało się pobrać listy plików');
  const text = await response.text();
  return text.replace(/[\[\]]/g, '').split(/[, \n]+/).filter(f => f.trim().length > 0);
};

// Kompilacja
export const compileJavaFile = async (fileName, ext = '.java') => {
  const cleanName = fileName.replace(ext, '');
  const response = await fetch(`${BASE_URL}/compile/${cleanName}?ext=${encodeURIComponent(ext)}`);
  if (!response.ok) throw new Error('Błąd komunikacji z endpointem kompilacji');
  return response;
};

// Usuwanie
export const deleteJavaFile = async (fileName, ext = '.java') => {
  const cleanName = fileName.replace(ext, '');
  const response = await fetch(`${BASE_URL}/delete/${cleanName}?ext=${encodeURIComponent(ext)}`);
  if (!response.ok) throw new Error('Błąd podczas usuwania pliku');
  return response;
};

// Deploy - wysyła nazwę głównego pliku ze źródłowym rozszerzeniem (.java/.py/.cpp)
// Backend sam:
//   1. Pobiera listę wszystkich powiązanych plików z ProcessMenager
//   2. Wysyła je wszystkie przez SFTP na serwer
//   3. Uruchamia właściwe polecenie (java/python3/./exe) zdalnie
export const deployToRemote = async (fullFileName) => {
  const lastDot = fullFileName.lastIndexOf('.');
  const nameOnly = fullFileName.substring(0, lastDot);
  const extension = fullFileName.substring(lastDot); // np. ".java", ".py", ".cpp"

  const response = await fetch(
    `${BASE_URL}/deploy/${encodeURIComponent(nameOnly)}?ext=${encodeURIComponent(extension)}`
  );
  if (!response.ok) throw new Error('Błąd deploymentu (sprawdź logi i config na serwerze)');
  return response.text();
};

// Pobieranie konfiguracji SSH z serwera
export const fetchDeployConfig = async () => {
  const response = await fetch(`${BASE_URL}/config`);
  if (!response.ok) throw new Error('Błąd pobierania konfiguracji');
  return response.json();
};

// Zapis konfiguracji SSH na serwerze
export const saveDeployConfig = async (configData) => {
  const response = await fetch(`${BASE_URL}/config`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(configData),
  });
  if (!response.ok) throw new Error('Błąd zapisu konfiguracji');
  return response.text();
};