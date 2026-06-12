class Pracownik {
    private String imie;
    private String stanowisko;

    public Pracownik(String imie, String stanowisko) {
        this.imie = imie;
        this.stanowisko = stanowisko;
        System.out.println("[SYSTEM] Utworzono profil pracownika: " + imie);
    }

    public void rozpocznijDzien() {
        System.out.println("--- " + imie + " loguje się do pracy ---");
        System.out.println(imie + ": Odpalam komputer...");
    }

    public void wykonajZadania() {
        System.out.println("\n--- Dużo logów z pracy ---");
        for (int i = 1; i <= 4; i++) {
            System.out.println("[LOG " + i + "] Przetwarzanie linijki kodu nr " + (i * 250));
            System.out.println("[LOG " + i + "] Testowanie modułu... OK");
        }
    }

    public void zakonczDzien() {
        System.out.println("\n--- " + imie + " kończy pracę ---");
        System.out.println(imie + ": Zamykanie systemu.");
    }
}