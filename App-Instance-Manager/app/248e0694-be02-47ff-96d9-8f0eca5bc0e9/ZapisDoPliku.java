import java.io.FileWriter;
import java.io.IOException;

public class ZapisDoPliku {
    public static void main(String[] args) {
        String tresc = "To jest przykładowa treść zapisana do pliku.";

        try (FileWriter writer = new FileWriter("plik_java.txt")) {
            writer.write(tresc);
            System.out.println("Zapisano do pliku.");
        } catch (IOException e) {
            System.out.println("Błąd podczas zapisu: " + e.getMessage());
        }
    }
}




