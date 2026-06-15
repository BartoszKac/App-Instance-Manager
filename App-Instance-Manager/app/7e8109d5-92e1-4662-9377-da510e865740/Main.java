public class Main {
    public static void main(String[] args) {
        // Tworzymy instancję drugiego obiektu
        Gadzina obiekt2 = new Gadzina();
        
        // Wypisujemy tekst obok siebie przy użyciu System.out.print()
        System.out.print("To jest tekst z pierwszej klasy... ");
        System.out.print(obiekt2.dajTekst());
    }
}