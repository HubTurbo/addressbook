package address.updater;

import address.MainApp;
import address.updater.model.LibraryDescriptor;
import address.updater.model.UpdateData;
import address.util.FileUtil;
import address.util.JsonUtil;
import address.util.OsDetector;
import address.util.Version;
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
 * Tracks which dependencies are missing and which are no longer needed (including by backup versions)
 */
public class DependencyTracker {
    private static final File DEPENDENCY_HISTORY_FILE = new File("lib/dependency_history");

    private HashMap<Version, List<String>> dependenciesForVersionsInUse = new HashMap<>();

    public DependencyTracker() {
        readVersionDependency();

        Optional<String> classPath = getClassPathAttributeFromManifest();

        if (!classPath.isPresent()) {
            System.out.println("Class-path undefined");
        } else {
            updateVersionDependency(Version.getCurrentVersion(),
                    new ArrayList<>(Arrays.asList(classPath.get().split("\\s+"))));
        }
    }

    public void updateVersionDependency(Version version, List<String> verDependencies) {
        dependenciesForVersionsInUse.put(version, verDependencies);
        writeVersionDependency();
    }

    public HashMap<Version, List<String>> getAllVersionDependency() {
        return dependenciesForVersionsInUse;
    }

    public void cleanUpUnusedDependencyVersions(List<Version> dependenciesOfUnusedVersions) {
        Iterator<Map.Entry<Version, List<String>>> it = dependenciesForVersionsInUse.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<Version, List<String>> entry = it.next();
            if (dependenciesOfUnusedVersions.contains(entry.getKey())) {
                System.out.println("removing " + entry.getKey());
                it.remove();
            }
        }

        writeVersionDependency();
    }

    private void writeVersionDependency() {
        if (!DEPENDENCY_HISTORY_FILE.exists()) {
            try {
                FileUtil.createFile(DEPENDENCY_HISTORY_FILE);
            } catch (IOException e) {
                System.out.println("Failed to create dependency file");
                e.printStackTrace();
            }
        }

        try {
            FileUtil.writeToFile(DEPENDENCY_HISTORY_FILE, JsonUtil.toJsonString(dependenciesForVersionsInUse));
        } catch (JsonProcessingException e) {
            System.out.println("Failed to convert dependencies to JSON");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Failed to write dependencies to file");
            e.printStackTrace();
        }
    }

    private void readVersionDependency() {
        if (!DEPENDENCY_HISTORY_FILE.exists()) {
            System.out.println("Dependencies file does not exist yet");
            return;
        }

        try {
            String json = FileUtil.readFromFile(DEPENDENCY_HISTORY_FILE);
            dependenciesForVersionsInUse = JsonUtil.fromJsonStringToGivenType(json,
                    new TypeReference<HashMap<Version, List<String>>>() {});
        } catch (IOException e) {
            System.out.println("Failed to read dependencies from file");
            e.printStackTrace();
        }
    }

    /**
     * @return the format is space delimited list, e.g. "lib/1.jar lib/2.jar lib/etc.jar"
     */
    private Optional<String> getClassPathAttributeFromManifest() {
        Class mainAppClass = MainApp.class;
        String className = mainAppClass.getSimpleName() + ".class";
        String resourcePath = mainAppClass.getResource(className).toString();
        if (!resourcePath.startsWith("jar")) {
            System.out.println("Not run from JAR");
            return Optional.empty();
        }
        String manifestPath = resourcePath.substring(0, resourcePath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";

        Manifest manifest;

        try {
            manifest = new Manifest(new URL(manifestPath).openStream());
        } catch (IOException e) {
            System.out.println("Manifest can't be read, not running dependency check");
            e.printStackTrace();
            return Optional.empty();
        }

        Attributes attr = manifest.getMainAttributes();
        return Optional.of(attr.getValue("Class-path"));
    }

    private void excludePlatformSpecificDependencies(List<String> dependencies) {
        String json = FileUtil.readFromInputStream(MainApp.class.getResourceAsStream("/UpdateData.json"));

        UpdateData updateData;

        try {
            updateData = JsonUtil.fromJsonString(json, UpdateData.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        List<String> librariesNotForCurrentMachine =  updateData.getLibraries().stream()
                .filter(libDesc -> libDesc.getOs() != OsDetector.Os.ANY && libDesc.getOs() != OsDetector.getOs())
                .map(libDesc -> "lib/" + libDesc.getFilename())
                .collect(Collectors.toList());

        dependencies.removeAll(librariesNotForCurrentMachine);
    }

    /**
     * @return empty array list if current version is not in dependency list (indicating not run from JAR)
     */
    public List<String> getMissingDependencies() {
        if (getCurrentVersionDependencies() == null) {
            return new ArrayList<>();
        }

        List<String> dependencies = getCurrentVersionDependencies().stream()
                                                                   .map(String::new).collect(Collectors.toList());

        excludePlatformSpecificDependencies(dependencies);

        return dependencies.stream()
                .filter(dependency -> !FileUtil.isFileExists(dependency)).collect(Collectors.toList());
    }

    public List<String> getCurrentVersionDependencies() {
        return dependenciesForVersionsInUse.get(Version.getCurrentVersion());
    }
}
