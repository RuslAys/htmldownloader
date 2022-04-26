import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Saver {
    public static void saveOnDisk(String content, String path) {
        try {
            Files.writeString(Paths.get(path), content,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (Exception e) {
            System.out.printf("An error occurred while saving file to %s: %s\n", path, e);
        }
    }

    public static void createDirIfNotExists(String path) {
        try {
            Path dirPath = Path.of(path);
            if (!Files.exists(dirPath)) {
                Files.createDirectory(dirPath);
            }
        } catch (IOException e) {
            System.out.printf("An error occurred while creating directory %s: %s\n", path, e);
        }
    }

    public static void createSubDirs(String baseDir, String path) {
        if (path.startsWith("/")) {
            path = path.replaceFirst("/", "");
        }
        if (path.contains("/")) {
            int separatorIdx = path.indexOf("/");
            path = path.substring(0, separatorIdx);
            createDirIfNotExists(baseDir + "/" + path);
        }
    }
}
