import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class DynamicRunner {
    public static void main(String[] args) throws Exception {
        // 1. Kod źródłowy przekazany jako String
        String sourceCode = 
            "public class DynamicApp {" +
            "    public static void main(String[] args) {" +
            "        System.out.println(\"Cześć! Działam wewnątrz procesu PID: \" + ProcessHandle.current().pid());" +
            "    }" +
            "}";

        String className = "DynamicApp";
        Path tempDir = Files.createTempDirectory("java_compilation");
        Path sourceFile = tempDir.resolve(className + ".java");

        // 2. Zapisujemy kod do pliku tymczasowego
        Files.writeString(sourceFile, sourceCode);

        // 3. Pobieramy systemowy kompilator (wymaga JDK, nie samo JRE!)
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.err.println("Błąd: Nie znaleziono kompilatora. Upewnij się, że używasz JDK, a nie JRE.");
            return;
        }

        // 4. Kompilacja
        int compilationResult = compiler.run(null, null, null, sourceFile.toString());

        if (compilationResult == 0) {
            System.out.println("Kompilacja zakończona sukcesem!");

            // 5. Ładowanie skompilowanej klasy za pomocą ClassLoader
            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{tempDir.toUri().toURL()});
            Class<?> cls = Class.forName(className, true, classLoader);
            
            // 6. Uruchomienie metody main za pomocą Refleksji
            Method mainMethod = cls.getDeclaredMethod("main", String[].class);
            String[] params = new String[]{};
            mainMethod.invoke(null, (Object) params);
        } else {
            System.err.println("Błąd podczas kompilacji kodu.");
        }
    }
}