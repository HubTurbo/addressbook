package address.updater;

import address.MainApp;
import address.util.AppLogger;
import address.util.FileUtil;
import address.util.LoggerManager;
import address.util.Version;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Deletes backup apps and their dependencies that are no longer used.
 */
public class BackupHandler {
    private static final AppLogger logger = LoggerManager.getLogger(BackupHandler.class);
    private static final int MAX_BACKUP_JAR_KEPT = 3;
    private static final String BACKUP_MARKER = "_";
    private static final String BACKUP_FILENAME_STRING_FORMAT = "addressbook" + BACKUP_MARKER + "%s.jar";
    private static final String BACKUP_FILENAME_PATTERN_STRING =
            "addressbook" + BACKUP_MARKER + "(" + Version.VERSION_PATTERN_STRING + ")\\.(jar|JAR)$";

    private final Version currentVersion;
    private DependencyHistoryHandler dependencyHistoryHandler;

    public BackupHandler(Version currentVersion, DependencyHistoryHandler dependencyHistoryHandler) {
        this.currentVersion = currentVersion;
        this.dependencyHistoryHandler = dependencyHistoryHandler;
    }

    /**
     * Creates backup if app is not run from backup JAR.
     * No backup is made if run from backup JAR.
     * Assumes app is run from JAR
     */
    public void createBackupOfApp(Version version) throws IOException, URISyntaxException {
        File mainAppJar = FileUtil.getJarFileOfClass(MainApp.class);

        if (isRunFromBackupJar(mainAppJar.getName())) {
            logger.info("Run from a backup; not creating backup");
            return;
        }

        String backupFilename = getBackupFilename(version);

        try {
            FileUtil.copyFile(mainAppJar.toPath(), Paths.get(backupFilename), true);
        } catch (IOException e) {
            logger.debug("Failed to create backup", e);
            throw e;
        }
    }

    private boolean isRunFromBackupJar(String jarName) {
        return jarName.contains(BACKUP_MARKER);
    }

    private String getBackupFilename(Version version) {
        return String.format(BACKUP_FILENAME_STRING_FORMAT, version.toString());
    }

    /**
     * Assumes user won't change backup filenames
     */
    public void cleanupBackups() {

        if (dependencyHistoryHandler.getCurrentVersionDependencies() == null ||
                dependencyHistoryHandler.getCurrentVersionDependencies().isEmpty()) {
            logger.info("Not running from JAR, will not clean backups");
            return;
        }

        List<String> allBackupFilenames = getAllBackupFilenamesAsideFromCurrent();

        // delete unused backups and remember their versions
        List<Version> deletedVersions = new ArrayList<>();

        for (int i = 0; i < (allBackupFilenames.size() - MAX_BACKUP_JAR_KEPT); i++) {
            logger.debug("Deleting {}", allBackupFilenames.get(i));

            try {
                FileUtil.deleteFile(allBackupFilenames.get(i));
                deletedVersions.add(getVersionOfBackupFileFromFilename(allBackupFilenames.get(i)));
            } catch (IOException e) {
                logger.warn("Failed to delete old backup file: {}", e);
            }
        }

        Set<String> dependenciesOfUnusedVersions = new HashSet<>();
        Set<String> dependenciesOfVersionsInUse = new HashSet<>();
        List<Version> unusedVersions = new ArrayList<>();

        dependencyHistoryHandler.getDependenciesOfAllVersion().entrySet().stream()
                .forEach(e -> {
                    if (deletedVersions.contains(e.getKey())) {
                        dependenciesOfUnusedVersions.addAll(e.getValue());
                        unusedVersions.add(e.getKey());
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

        dependencyHistoryHandler.cleanUpUnusedDependencyVersions(unusedVersions);
    }

    /**
     * @return all backup filenames aside from current app sorted from lowest version to latest
     */
    private List<String> getAllBackupFilenamesAsideFromCurrent() {
        File currDirectory = new File(".");

        File[] filesInCurrentDirectory = currDirectory.listFiles();

        if (filesInCurrentDirectory == null) {
            // current directory always exists
            assert false;
            return new ArrayList<>();
        }

        List<File> listOfFilesInCurrDirectory = new ArrayList<>(Arrays.asList(filesInCurrentDirectory));

        // Exclude current version in case user is running backup Jar
        return listOfFilesInCurrDirectory.stream()
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
     * Gets version of Jar backup file.
     * Expects filename in format "addressbook_V[major].[minor].[patch].jar".
     * @param filename filename of addressbook backup JAR, in format "addressbook_V[major].[minor].[patch].jar"
     * @return version of backup JAR
     */
    private Version getVersionOfBackupFileFromFilename(String filename) {
        Pattern htJarBackupFilenamePattern = Pattern.compile(BACKUP_FILENAME_PATTERN_STRING);
        Matcher htJarBackupFilenameMatcher = htJarBackupFilenamePattern.matcher(filename);
        if (!htJarBackupFilenameMatcher.find()) {
            assert false;
        }

        return Version.fromString(htJarBackupFilenameMatcher.group(1));
    }
}
