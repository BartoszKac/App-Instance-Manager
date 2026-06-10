tresc = "To jest przykładowa treść zapisana do pliku."

with open("plik_python.txt", "w", encoding="utf-8") as plik:
    plik.write(tresc)

print("Zapisano do pliku.")


