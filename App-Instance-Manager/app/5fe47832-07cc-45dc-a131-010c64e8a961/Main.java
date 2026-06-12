import java.nio.file.*;

public class Main {
    public static void main(String[] args) throws Exception {
        // Brak ścieżki = zapis w tym samym folderze, w którym uruchomiono program
        Files.writeString(Path.of("test.txt"), "Plik w tym samym folderze!");
    }
}