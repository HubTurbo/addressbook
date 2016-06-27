package address.updater;

import address.MainApp;
import address.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * Tracks dependencies used by current version and backup versions of the application
 * Cleans up dependencies not used by mentioned versions
 */
public class DependencyHistoryHandler {
    private static final AppLogger logger = LoggerManager.getLogger(DependencyHistoryHandler.class);
    private static final File DEPENDENCY_HISTORY_FILE = new File("lib/dependency_history");

    private HashMap<Version, List<String>> dependenciesForVersionsInUse = new HashMap<>();

    public DependencyHistoryHandler() {
        loadVersionDependencyHistory();

        if (!ManifestFileReader.isRunFromJar()) {
            logger.debug("Not running from JAR, will not update version dependencies");
            return;
        }

        Optional<List<String>> libraries = ManifestFileReader.getLibrariesInClasspathFromManifest();

        assert libraries.isPresent() : "No libraries being used - should not happen";

        updateVersionDependencies(MainApp.VERSION, libraries.get());
    }

    /**
     * Updates the dependencies of a version of the application
     */
    public void updateVersionDependencies(Version version, List<String> verDependencies) {
        dependenciesForVersionsInUse.put(version, verDependencies);
        writeVersionDependencyHistory();
    }

    /**
     * Gets dependencies for every version known
     */
    public HashMap<Version, List<String>> getDependenciesOfAllVersion() {
        return dependenciesForVersionsInUse;
    }

    /**
     * Deletes dependencies of version which are no longer used
     */
    public void cleanUpUnusedDependencyVersions(List<Version> unusedVersions) {
        Iterator<Map.Entry<Version, List<String>>> it = dependenciesForVersionsInUse.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Version, List<String>> entry = it.next();
            if (unusedVersions.contains(entry.getKey())) {
                logger.debug("Removing {}", entry.getKey());
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
                logger.debug("Failed to create dependency file", e);
            }
        }

        try {
            FileUtil.writeToFile(DEPENDENCY_HISTORY_FILE, JsonUtil.toJsonString(dependenciesForVersionsInUse));
        } catch (JsonProcessingException e) {
            logger.debug("Failed to convert dependencies to JSON", e);
        } catch (IOException e) {
            logger.debug("Failed to write dependencies to file", e);
        }
    }

    private void loadVersionDependencyHistory() {
        if (!DEPENDENCY_HISTORY_FILE.exists()) {
            logger.debug("Dependencies file does not exist yet");
            return;
        }

        try {
            String json = FileUtil.readFromFile(DEPENDENCY_HISTORY_FILE);
            dependenciesForVersionsInUse = JsonUtil.fromJsonStringToGivenType(json,
                    new TypeReference<HashMap<Version, List<String>>>() {});
        } catch (IOException e) {
            logger.debug("Failed to read dependencies from file");
            e.printStackTrace();
        }
    }

    public List<String> getCurrentVersionDependencies() {
        return dependenciesForVersionsInUse.get(MainApp.VERSION);
    }
}
