package hubturbo.updater;

import address.util.*;
import commons.FileUtil;

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
    private static final int MAX_BACKUP_JAR_KEPT = 3;
    private static final String BACKUP_DIR = "past_versions";
    private static final String BACKUP_MARKER = "_";
    private static final String BACKUP_FILENAME_STRING_FORMAT = "addressbook" + BACKUP_MARKER + "%s.jar";
    private static final String BACKUP_FILENAME_REGEX =
            "addressbook" + BACKUP_MARKER + "(" + Version.VERSION_REGEX + ")\\.(jar|JAR)$";
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
    protected void createAppBackup(Version version) throws IOException, URISyntaxException {
        //TODO: generate this dynamically, temp for updater refactoring
        File mainAppJar = new File("lib/resource.jar");

        if (isRunFromBackupJar(mainAppJar)) {
            return;
        }

        createBackupDirIfMissing();
        extractFile(BACKUP_INSTRUCTION_RESOURCE_PATH, BACKUP_INSTRUCTION_FILENAME);
        makeBackupCopy(mainAppJar, version);
    }

    /**
     * Remove old backup jars if there are too many and deletes any unused dependencies
     * <p>
     * Assumes that user has not tampered with the backup files' names
     */
    protected void cleanupBackups() {
        File backupDir = new File(BACKUP_DIR);
        List<String> backupFilesNames = getSortedBackupFilesNames(backupDir, BACKUP_FILENAME_REGEX);
        List<Version> backupVersions = getVersionsFromFileNames(backupFilesNames);

        List<String> backupFilesToDelete = getBackupFilesToDelete(backupFilesNames, MAX_BACKUP_JAR_KEPT);
        backupFilesToDelete.stream()
                .forEach(this::deleteBackupFile);

        List<Version> deletedVersions = getVersionsFromFileNames(backupFilesToDelete);
        List<Version> retainedVersions = backupVersions.stream()
                .filter(backupVersion -> !deletedVersions.contains(backupVersion))
                .collect(Collectors.toCollection(ArrayList::new));

        Set<String> unusedDependencies = getUnusedDependencies(deletedVersions, retainedVersions,
                dependencyHistoryHandler.getDependenciesTableForKnownVersions());

        unusedDependencies.stream()
                .forEach(this::deleteDependencyFile);

        dependencyHistoryHandler.cleanUpUnusedDependencies(deletedVersions);
    }

    private void makeBackupCopy(File mainAppJar, Version version) throws IOException {
        String backupFilename = getBackupFilename(version);
        FileUtil.copyFile(mainAppJar.toPath(), Paths.get(BACKUP_DIR, backupFilename), true);
    }

    private void createBackupDirIfMissing() throws IOException {
        File backupDir = new File(BACKUP_DIR);
        if (FileUtil.isDirExists(backupDir)) return;
        FileUtil.createDirs(backupDir);
    }

    private void extractFile(String fileResourcePath, String fileDestinationPath) throws IOException {
        File destinationFile = new File(fileDestinationPath);
        InputStream in = BackupHandler.class.getClassLoader().getResourceAsStream(fileResourcePath);
        Files.copy(in, destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private boolean isRunFromBackupJar(File jar) {
        return jar.getName().contains(BACKUP_MARKER);
    }

    private String getBackupFilename(Version version) {
        return String.format(BACKUP_FILENAME_STRING_FORMAT, version.toString());
    }

    private ArrayList<Version> getVersionsFromFileNames(List<String> backupFilesToDelete) {
        return backupFilesToDelete.stream()
                .map(this::getVersionFromFileName)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Obtain names of backup files to delete, starting from the beginning of the list
     *
     * @param backupFilesNames
     * @return
     */
    private ArrayList<String> getBackupFilesToDelete(List<String> backupFilesNames, int noOfBackupsToKeep) {
        int noToDelete = Math.min(0, backupFilesNames.size() - noOfBackupsToKeep);
        return backupFilesNames.stream()
                .limit(noToDelete)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Gets the union set of dependencies for all given versions
     *
     * @param versions
     * @param dependenciesTable versions mapped to their dependencies
     * @return
     */
    private Set<String> getDependenciesOfVersions(List<Version> versions, Map<Version, List<String>> dependenciesTable) {
        return versions.stream()
                .flatMap(storedVersion -> dependenciesTable.get(storedVersion).stream())
                .collect(Collectors.toCollection(HashSet::new));
    }

    private Set<String> getUnusedDependencies(List<Version> deletedVersions, List<Version> storedVersions,
                                              Map<Version, List<String>> dependenciesTable) {
        Set<String> possiblyUnusedDependencies = getDependenciesOfVersions(deletedVersions, dependenciesTable);
        Set<String> requiredDependencies = getDependenciesOfVersions(storedVersions, dependenciesTable);
        possiblyUnusedDependencies.removeAll(requiredDependencies);
        return possiblyUnusedDependencies;
    }

    private void deleteDependencyFile(String dependencyFileName) {
        try {
            FileUtil.deleteFile(new File(dependencyFileName));
        } catch (IOException e) {
            System.out.println("Failed to delete unused dependency: " + e);
        }
    }

    private void deleteBackupFile(String backupFileName) {
        try {
            FileUtil.deleteFile(BACKUP_DIR + File.separator + backupFileName);
        } catch (IOException e) {
            System.out.println("Failed to delete old backup file: " + e);
        }
    }

    /**
     * Gets all backup files' names found in backupDir if they match backupFileNameRegex
     * The backup files' names returned will be sorted from oldest version to newest
     * <p>
     * This does not return the backup made for the current version of the app
     */
    private List<String> getSortedBackupFilesNames(File backupDir, String backupFileNameRegex) {
        if (!FileUtil.isDirExists(backupDir)) {
            return new ArrayList<>();
        }

        File[] backupFiles = backupDir.listFiles();

        if (backupFiles == null) {
            return new ArrayList<>();
        }

        List<File> listOfBackupFiles = new ArrayList<>(Arrays.asList(backupFiles));

        // Exclude current version in case user is running backup Jar
        return listOfBackupFiles.stream()
                .filter(file -> !file.getName().equals(getBackupFilename(currentVersion)))
                .filter(file -> file.getName().matches(backupFileNameRegex))
                .map(File::getName)
                .sorted(getBackupFilenameComparatorByVersion())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Comparator<String> getBackupFilenameComparatorByVersion() {
        return (a, b) -> getVersionFromFileName(a)
                .compareTo(getVersionFromFileName(b));
    }

    /**
     * Gets version of Jar backup file from its name.
     * Expects filename in format "addressbook_V[major].[minor].[patch].jar".
     *
     * @param filename filename of addressbook backup JAR, in format "addressbook_V[major].[minor].[patch].jar"
     * @return version of backup JAR
     */
    private Version getVersionFromFileName(String filename) {
        Pattern htJarBackupFilenamePattern = Pattern.compile(BACKUP_FILENAME_REGEX);
        Matcher htJarBackupFilenameMatcher = htJarBackupFilenamePattern.matcher(filename);
        assert htJarBackupFilenameMatcher.find() : "Invalid backup file name found" + filename;

        return Version.fromString(htJarBackupFilenameMatcher.group(1));
    }
}
