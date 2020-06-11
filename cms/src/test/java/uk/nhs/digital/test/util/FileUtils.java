package uk.nhs.digital.test.util;

import com.github.jknack.handlebars.internal.Files;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class FileUtils {

    public static String readFileInClassPath(final String fileClassPath) {
        try {
            return Files.read(FileUtils.class.getResourceAsStream(fileClassPath), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read test content file from " + fileClassPath, e);
        }
    }

    public static String readFile(final String fileSystemPath) {
        try {
            return Files.read(Paths.get(fileSystemPath).toFile(), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new UncheckedIOException("Failed to read test content file from " + fileSystemPath, e);
        }
    }
}
