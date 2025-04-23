package src.main.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class FileOperations {

    public static enum FileExtension {
        ALL(""),
        HTML(".html"),
        JSON(".json"),
        JAVA(".java"),
        TEXT(".txt");

        private final String extension;
        FileExtension(String extension) {
            this.extension = extension;
        }
        public String getExtension() {
            return extension;
        }
    }

    public static class ScannerUtil {
        public static Scanner createScanner(InputStream inputStream) {
            return new Scanner(inputStream, StandardCharsets.UTF_8.name());
        }
        public static Scanner createScanner(File file) throws FileNotFoundException {
            return new Scanner(file, StandardCharsets.UTF_8.name());
        }
    }

    public static Set<String> listFiles(Path dir) throws IOException {
        try {
            Set<String> fileSet = listFiles(dir, FileExtension.ALL);
            return fileSet;
        }
        catch (IOException e) {
            throw e;
        }
    }

    public static Set<String> listFiles(Path dir, FileExtension extension) throws IOException {
        Set<String> fileSet = new HashSet<>();
        dir = dir.normalize();
        if(!Files.exists(dir)) throw new IOException("The specified path, " + dir.toString() + ", was not found.");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                String fileName = path.getFileName().toString();
                if (!Files.isDirectory(path) && !FilePaths.IGNORED_FILES.contains(fileName) && fileName.endsWith(extension.getExtension())) {
                    fileSet.add(fileName);
                }
            }
            return fileSet;
        }
        catch (IOException e) {
            System.out.println("Error accessing directory: " + dir);
            throw e;
        }
    }

    public static void emptyFiles(Path dir, String extension) throws IOException {
        Set<String> files = listFiles(dir); // does not include ignored files
        for (String fileName : files) {
            // Delete if extension matches or if wildcard
            if (extension.equals("*") || fileName.endsWith(extension)) {
                try {
                    Path path = dir.resolve(fileName);
                    Files.delete(path);
                } catch (IOException e) {
                    System.err.println("Failed to delete file: " + fileName);
                    throw e;
                }
            }
        }
    }

    public static void writeFile(String filename, String extension, Path destination, String content) {
        writeFile(filename, extension, destination, Collections.singletonList(content));
    }
    public static void writeFile(String filename, String extension, Path destination, List<String> content) {
        Path filePath = destination.resolve(filename + extension);
        File file = filePath.toFile();
        writeFile(file, content);
    }

    public static void writeFile(File file, List<String> content) {
        try {
            Files.createDirectories(file.getParentFile().toPath());
            file.createNewFile();
            try (FileWriter writer = new FileWriter(file, false)) {
                for (String line : content) {
                    writer.write(line + "\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
