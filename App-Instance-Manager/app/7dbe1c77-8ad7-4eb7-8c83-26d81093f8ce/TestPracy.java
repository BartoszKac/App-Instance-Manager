public class TestPracy {
    public static void main(String[] args) {
        System.out.println("=== START PROGRAMU (JEDEN PLIK, BEZ PAKIETÓW) ===\n");

        // Klasa TestPracy bez problemu widzi klasę Pracownik, bo są w tym samym pliku
        Pracownik pracownik = new Pracownik("Janek", "Programista");

        pracownik.rozpocznijDzien();
        pracownik.wykonajZadania();
        pracownik.zakonczDzien();

        System.out.println("\n=== KONIEC PROGRAMU ===");
    }
}