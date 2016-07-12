package address.updater;

import address.MainApp;
import address.util.AppLogger;
import address.util.FileUtil;
import address.util.LoggerManager;
import address.util.Version;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class is meant to handle backups of the application
 */
public class BackupHandler {
    private static final AppLogger logger = LoggerManager.getLogger(BackupHandler.class);
    private static final int MAX_BACKUP_JAR_KEPT = 3;
    private static final String BACKUP_DIR = "past_versions";
    private static final String BACKUP_MARKER = "_";
    private static final String BACKUP_FILENAME_STRING_FORMAT = "addressbook" + BACKUP_MARKER + "%s.jar";
    private static final String BACKUP_FILENAME_PATTERN_STRING =
            "addressbook" + BACKUP_MARKER + "(" + Version.VERSION_PATTERN_STRING + ")\\.(jar|JAR)$";
    private static final String BACKUP_INSTRUCTION_FILENAME = "Instruction to use past versions.txt";
    private static final String BACKUP_INSTRUCTION_RESOURCE_PATH = "updater/Instruction to use past versions.txt";

    private final Version currentVersion;
    private DependencyHistoryHandler dependencyHistoryHandler;

    public BackupHandler(Version currentVersion, DependencyHistoryHandler dependencyHistoryHandler) {
        this.currentVersion = currentVersion;
        this.dependencyHistoryHandler = dependencyHistoryHandler;
    }

    /**
     * Creates a backup unless app is already being run from backup JAR.
     */
    public void createAppBackup(Version version) throws IOException, URISyntaxException {
        File mainAppJar = FileUtil.getJarFileOfClass(MainApp.class);

        if (isRunFromBackupJar(mainAppJar)) {
            logger.info("Run from a backup; not creating backup");
            return;
        }

        createBackupDirIfMissing();
        extractInstructionToUseBackupVersion();
        makeBackupCopy(mainAppJar, version);
    }

    private void makeBackupCopy(File mainAppJar, Version version) throws IOException {
        String backupFilename = getBackupFilename(version);
        try {
            FileUtil.copyFile(mainAppJar.toPath(), Paths.get(BACKUP_DIR, backupFilename), true);
        } catch (IOException e) {
            logger.debug("Failed to create backup", e);
            throw e;
        }
    }

    private void createBackupDirIfMissing() throws IOException {
        File backupDir = new File(BACKUP_DIR);
        if (FileUtil.isDirExists(backupDir)) return;

        try {
            FileUtil.createDirs(backupDir);
        } catch (IOException e) {
            logger.debug("Failed to create backup directory: {}", e);
            throw e;
        }
    }

    private void extractInstructionToUseBackupVersion() throws IOException {
        File backupInstructionFile = new File(BACKUP_INSTRUCTION_FILENAME);
        try {
            InputStream in = BackupHandler.class.getClassLoader().getResourceAsStream(BACKUP_INSTRUCTION_RESOURCE_PATH);
            Files.copy(in, backupInstructionFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.debug("Failed to extract backup instruction");
            throw e;
        }
    }

    private boolean isRunFromBackupJar(File jar) {
        return jar.getName().contains(BACKUP_MARKER);
    }

    private String getBackupFilename(Version version) {
        return String.format(BACKUP_FILENAME_STRING_FORMAT, version.toString());
    }

    /**
     * Remove old backup jars if there are too many and deletes any unused dependencies
     * <p>
     * Assumes that user has not tampered with the backup files' names
     */
    public void cleanupBackups() {
        logger.debug("Cleaning backups");

        if (!hasCurrentVersionDependencies()) {
            logger.info("Not running from JAR, will not clean backups");
            return;
        }

        List<String> backupFilesNames = getSortedBackupFilesNames();
        List<Version> deletedVersions = backupFilesNames.stream()
                .limit(backupFilesNames.size() - MAX_BACKUP_JAR_KEPT)
                .map(this::deleteBackupFile)
                .collect(Collectors.toCollection(ArrayList::new));

        Set<String> dependenciesOfUnusedVersions = new HashSet<>();
        Set<String> dependenciesOfVersionsInUse = new HashSet<>();

        dependencyHistoryHandler.getDependenciesTableForKnownVersions().entrySet().stream()
                .forEach(e -> {
                    if (deletedVersions.contains(e.getKey())) {
                        dependenciesOfUnusedVersions.addAll(e.getValue());
                    } else {
                        dependenciesOfVersionsInUse.addAll(e.getValue());
                    }
                });

        dependenciesOfUnusedVersions.removeAll(dependenciesOfVersionsInUse);

        dependenciesOfUnusedVersions.stream().forEach(dep -> {
            try {
                FileUtil.deleteFile(new File(dep));
            } catch (IOException e) {
                logger.warn("Failed to delete unused dependency: {}", dep, e);
            }
        });

        dependencyHistoryHandler.cleanUpUnusedDependencies(deletedVersions);
    }

    private Version deleteBackupFile(String backupFileName) {
        logger.debug("Deleting {}", backupFileName);

        try {
            FileUtil.deleteFile(BACKUP_DIR + File.separator + backupFileName);
        } catch (IOException e) {
            logger.warn("Failed to delete old backup file: {}", e);
        }
        return getVersionOfBackupFileFromFilename(backupFileName);
    }

    private boolean hasCurrentVersionDependencies() {
        return dependencyHistoryHandler.getCurrentVersionDependencies() != null &&
                !dependencyHistoryHandler.getCurrentVersionDependencies().isEmpty();
    }

    /**
     * Gets all past backup filenames, sorted from oldest version to latest
     * This does not include the backup made for the current version of the app
     */
    private List<String> getSortedBackupFilesNames() {
        File backupDir = new File(BACKUP_DIR);

        if (!FileUtil.isDirExists(backupDir)) {
            logger.debug("No backup directory");
            return new ArrayList<>();
        }

        File[] backupFiles = backupDir.listFiles();

        if (backupFiles == null) {
            logger.warn("Null list of backup files found.");
            return new ArrayList<>();
        }

        List<File> listOfBackupFiles = new ArrayList<>(Arrays.asList(backupFiles));

        // Exclude current version in case user is running backup Jar
        return listOfBackupFiles.stream()
                .filter(f ->
                        !f.getName().equals(getBackupFilename(currentVersion))
                                && f.getName().matches(BACKUP_FILENAME_PATTERN_STRING))
                .map(File::getName)
                .sorted(getBackupFilenameComparatorByVersion())
                .collect(Collectors.toList());
    }

    private Comparator<String> getBackupFilenameComparatorByVersion() {
        return (a, b) -> getVersionOfBackupFileFromFilename(a)
                .compareTo(getVersionOfBackupFileFromFilename(b));
    }

    /**
     * Gets version of Jar backup file from its name.
     * Expects filename in format "addressbook_V[major].[minor].[patch].jar".
     *
     * @param filename filename of addressbook backup JAR, in format "addressbook_V[major].[minor].[patch].jar"
     * @return version of backup JAR
     */
    private Version getVersionOfBackupFileFromFilename(String filename) {
        Pattern htJarBackupFilenamePattern = Pattern.compile(BACKUP_FILENAME_PATTERN_STRING);
        Matcher htJarBackupFilenameMatcher = htJarBackupFilenamePattern.matcher(filename);
        assert htJarBackupFilenameMatcher.find() : "Invalid backup file name found" + filename;

        return Version.fromString(htJarBackupFilenameMatcher.group(1));
    }
}
