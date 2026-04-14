const BASE_URL = 'http://localhost:8081/app';

// Wysyłanie całej tablicy plików (projektu) na serwer
export const saveJavaFiles = async (filesArray) => {
  const response = await fetch(`${BASE_URL}/`, { // Nowy endpoint dla paczki plików!
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(filesArray),
  });
  
  if (!response.ok) {
    throw new Error('Błąd zapisu projektu');
  }
  return response;
};

// Pobieranie listy plików z serwera
export const fetchServerFiles = async () => {
  const response = await fetch(`${BASE_URL}/info`);
  
  if (!response.ok) {
    throw new Error('Nie udało się pobrać listy plików');
  }
  
  const text = await response.text();
  return text.replace(/[\[\]]/g, '').split(/[, \n]+/).filter(f => f.trim().length > 0);
};

// Usuwanie pliku
// Zlecenie kompilacji - dodajemy parametr ext
export const compileJavaFile = async (fileName, ext = ".java") => {
  const cleanName = fileName.replace(ext, '');
  // Dodajemy ?ext= na końcu
  const response = await fetch(`${BASE_URL}/compile/${cleanName}?ext=${ext}`);
  
  if (!response.ok) {
    throw new Error('Błąd komunikacji z endpointem kompilacji');
  }
  return response;
};

// Usuwanie - również dodajemy ext
export const deleteJavaFile = async (fileName, ext = ".java") => {
  const cleanName = fileName.replace(ext, '');
  const response = await fetch(`${BASE_URL}/delete/${cleanName}?ext=${ext}`);
  
  if (!response.ok) {
    throw new Error('Błąd podczas usuwania pliku');
  }
  return response;
};