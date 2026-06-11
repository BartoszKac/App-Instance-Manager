#!/bin/bash

# Wyświetlenie prostego tekstu
echo "=== TEST URUCHOMIENIA BASH ==="
echo "Status: Sukces!"

# Test parametrów (jeśli Twój Executor przekazuje jakieś argumenty)
echo "Nazwa skryptu: $0"
echo "Liczba przekazanych argumentów: $#"
if [ $# -gt 0 ]; then
    echo "Argumenty: $@"
fi

# Test środowiska
echo "Aktualny katalog: $(pwd)"
echo "Użytkownik: $(whoami)"
echo "=============================="