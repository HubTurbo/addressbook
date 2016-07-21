package address.update;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import commons.FileUtil;
import commons.JsonUtil;
import commons.LibraryDescriptor;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Tracks dependencies used by current version and backup versions of the application
 * Cleans up dependencies not used by mentioned versions
 */
public class DependencyHistoryHandler {
    private static final File DEPENDENCY_HISTORY_FILE = new File("lib/dependency_history");

    private final commons.Version currentVersion;
    private HashMap<commons.Version, List<String>> dependenciesForVersionsInUse = new HashMap<>();

    public DependencyHistoryHandler(commons.Version currentVersion) {
        this.currentVersion = currentVersion;

        loadVersionDependencyHistory();

        try {
            commons.VersionData versionData = FileUtil.deserializeObjectFromJsonFile(new File("VersionData.json"), commons.VersionData.class);

            List<String> libraryFileNames = versionData.getLibraries().stream().map(LibraryDescriptor::getFileName).collect(Collectors.toCollection(ArrayList::new));
            updateVersionDependencies(currentVersion, libraryFileNames);
        } catch (IOException e) {
            System.out.println("Failed to parse data from latest data file." + e);
        }
    }

    /**
     * Updates the dependencies of a version of the application
     */
    public void updateVersionDependencies(commons.Version version, List<String> verDependencies) {
        dependenciesForVersionsInUse.put(version, verDependencies);
        writeVersionDependencyHistory();
    }

    /**
     * Gets dependencies for every version known
     */
    public HashMap<commons.Version, List<String>> getDependenciesTableForKnownVersions() {
        return dependenciesForVersionsInUse;
    }

    /**
     * Deletes any unused dependencies
     */
    public void cleanUpUnusedDependencies(List<commons.Version> unusedVersions) {
        Iterator<Map.Entry<commons.Version, List<String>>> it = dependenciesForVersionsInUse.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<commons.Version, List<String>> entry = it.next();
            if (unusedVersions.contains(entry.getKey())) {
                it.remove();
            }
        }

        writeVersionDependencyHistory();
    }

    private void writeVersionDependencyHistory() {
        if (!DEPENDENCY_HISTORY_FILE.exists()) {
            try {
                FileUtil.createFile(DEPENDENCY_HISTORY_FILE);
            } catch (IOException e) {
            }
        }

        try {
            FileUtil.serializeObjectToJsonFile(DEPENDENCY_HISTORY_FILE, dependenciesForVersionsInUse);
        } catch (JsonProcessingException e) {
            System.out.println("Failed to convert dependencies to JSON" + e);
        } catch (IOException e) {
            System.out.println("Failed to write dependencies to file" + e);
        }
    }

    private void loadVersionDependencyHistory() {
        if (!DEPENDENCY_HISTORY_FILE.exists()) {
            System.out.println("Dependencies file does not exist yet");
            return;
        }

        try {
            String json = FileUtil.readFromFile(DEPENDENCY_HISTORY_FILE);
            dependenciesForVersionsInUse = JsonUtil.fromJsonStringToGivenType(json,
                    new TypeReference<HashMap<commons.Version, List<String>>>() {});
        } catch (IOException e) {
            System.out.println("Failed to read dependencies from file");
            e.printStackTrace();
        }
    }

    public List<String> getCurrentVersionDependencies() {
        return dependenciesForVersionsInUse.get(currentVersion);
    }
}
