package address.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes and reads file
 */
public class FileUtil {
    private static final String CHARSET = "UTF-8";

    public static boolean isFileExists(String filepath) {
        File file = new File(filepath);

        return file.exists() && file.isFile();
    }

    public static boolean isDirExists(String dirpath) {
        File dir = new File(dirpath);

        return dir.exists() && dir.isDirectory();
    }

    /**
     * Creates a file and its parent directories if it does not exists
     *
     * @return true if file is created, false if file already exists
     */
    public static boolean createFile(File file) throws IOException {
        if (file.exists()) {
            return false;
        }

        createParentDirsOfFile(file);

        return file.createNewFile();
    }

    /**
     * Lists all files in directory and its subdirectories
     */
    public static List<File> listFilesInDir(Path directory) {
        List<File> filepaths = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path path : stream) {
                if (path.toFile().isDirectory()) {
                    filepaths.addAll(listFilesInDir(path));
                } else {
                    filepaths.add(path.toFile());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return filepaths;
    }

    /**
     * Creates the given directory along with its parent directories
     * @param dir the directory to be created; assumed not null
     * @throws IOException if the directory or a parent directory cannot be created
     */
    public static void createDirs(File dir) throws IOException {
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Failed to make directories of " + dir.getName());
        }
    }

    public static void createParentDirsOfFile(File file) throws IOException {
        File parentDir = file.getParentFile();

        createDirs(parentDir);
    }

    /**
     * Move file from source to dest
     * @param isOverwrite set true to overwrite source
     */
    public static void moveFile(Path source, Path dest, boolean isOverwrite) throws IOException {
        if (isOverwrite) {
            Files.move(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.move(source, dest);
        }
    }

    /**
     *
     * @param source
     * @param dest
     * @param isOverwrite
     * @throws IOException
     */
    public static void copyFile(Path source, Path dest, boolean isOverwrite) throws IOException {
        if (isOverwrite) {
            Files.copy(source, dest, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.copy(source, dest);
        }
    }

    /**
     * @return List of filenames failed to be moved
     */
    public static List<String> moveContentOfADirectoryToAnother(String sourceDir, String targetDir) {
        List<File> sourceFiles = FileUtil.listFilesInDir(Paths.get(sourceDir));
        List<String> failedToMoveFiles = new ArrayList<>();

        for (File sourceFile : sourceFiles) {
            Path sourceFilePath = sourceFile.toPath();
            Path targetFilePath = Paths.get(targetDir + File.separator + sourceFile.getName());

            try {
                FileUtil.moveFile(sourceFilePath, targetFilePath, true);
            } catch (IOException e) {
                e.printStackTrace();
                failedToMoveFiles.add(sourceFile.getPath());
            }
        }

        return failedToMoveFiles;
    }

    /**
     * Assumes file exists
     */
    public static String readFromFile(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), CHARSET);
    }

    /**
     * Assumes file exists
     */
    public static void writeToFile(File file, String content) throws IOException {
        Files.write(file.toPath(), content.getBytes(CHARSET));
    }

    public static File getJarFileOfClass(Class givenClass) {
        return new File(givenClass.getProtectionDomain().getCodeSource().getLocation().getPath());
    }
}
