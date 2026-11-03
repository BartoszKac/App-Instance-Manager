package com.example.inspector.inspection;

import com.example.inspection.grpc.v1.TargetPlatform;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.zip.GZIPOutputStream;

public enum Platform {
    POSIX(TargetPlatform.TARGET_PLATFORM_POSIX, "posix.sh", '/', script -> script),
    WINDOWS(TargetPlatform.TARGET_PLATFORM_WINDOWS, "windows.ps1", '\\', Platform::toEncodedPowerShell);

    private static final String BOOTSTRAP = "$b='%s';$m=New-Object IO.MemoryStream(,[Convert]::FromBase64String($b));"
            + "$g=New-Object IO.Compression.GZipStream($m,[IO.Compression.CompressionMode]::Decompress);"
            + "iex (New-Object IO.StreamReader($g,[Text.Encoding]::UTF8)).ReadToEnd()";

    private final TargetPlatform target;
    private final String suffix;
    private final char separator;
    private final UnaryOperator<String> toCommand;

    Platform(TargetPlatform target, String suffix, char separator, UnaryOperator<String> toCommand) {
        this.target = target;
        this.suffix = suffix;
        this.separator = separator;
        this.toCommand = toCommand;
    }

    public static List<Platform> orderFor(TargetPlatform preferred) {
        return preferred == TargetPlatform.TARGET_PLATFORM_WINDOWS ? List.of(WINDOWS, POSIX) : List.of(POSIX, WINDOWS);
    }

    public TargetPlatform target() {
        return target;
    }

    public String factsCommand() {
        return command("scripts/facts-" + suffix);
    }

    public String filesCommand() {
        return command("scripts/files-" + suffix);
    }

    public String parentOf(String path) {
        int last = path.lastIndexOf(separator);
        if (last < 0 || last == path.length() - 1) {
            return "";
        }
        boolean underRoot = last == 0 || (separator == '\\' && last == 2);
        return underRoot ? path.substring(0, last + 1) : path.substring(0, last);
    }

    public String nameOf(String path) {
        int last = path.lastIndexOf(separator);
        return last < 0 || last == path.length() - 1 ? path : path.substring(last + 1);
    }

    private String command(String resource) {
        try {
            String script = new ClassPathResource(resource).getContentAsString(StandardCharsets.UTF_8);
            return toCommand.apply(script);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String toEncodedPowerShell(String script) {
        String bootstrap = BOOTSTRAP.formatted(gzipBase64(script));
        String encoded = Base64.getEncoder().encodeToString(bootstrap.getBytes(StandardCharsets.UTF_16LE));
        return "powershell -NoProfile -NonInteractive -EncodedCommand " + encoded;
    }

    private static String gzipBase64(String script) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bytes)) {
            gzip.write(script.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return Base64.getEncoder().encodeToString(bytes.toByteArray());
    }
}
