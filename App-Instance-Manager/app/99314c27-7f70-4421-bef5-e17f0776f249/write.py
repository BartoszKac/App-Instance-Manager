# Definiujemy nazwę pliku oraz zawartość
nazwa_pliku = "dupa.txt"
zawartosc = "seks"

# Otwieramy plik w trybie zapisu ('w' - write)
with open(nazwa_pliku, "w", encoding="utf-8") as plik:
    plik.write(zawartosc)

print(f"Plik '{nazwa_pliku}' został pomyślnie zapisany.")