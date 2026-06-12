import java.io.FileWriter;
import java.io.IOException;

public class Main{
    public static void main(String[] args) {
            System.out.println("Wykinuje sie");
        try {
            FileWriter writer = new FileWriter("raport_java.txt");
            writer.write("To jest plik utworzony przez Javę.\n");
            writer.close();

            System.out.println("Zapisano raport_java.txt");
        } catch (IOException e) {
            System.out.println("Błąd: " + e.getMessage());
        }
    }
}