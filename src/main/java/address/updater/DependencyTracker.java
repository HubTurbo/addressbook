package address.updater;

import address.util.FileUtil;
import address.util.JsonUtil;
import address.util.OsDetector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tracks which dependencies are missing and which are no longer needed (including by backup versions)
 */
public class DependencyTracker {
    private static final File DEPENDENCY_HISTORY_FILE = new File("lib/history");
    private HashMap<Integer, List<String>> dependenciesPerKnownVersions = new HashMap<>();

    public DependencyTracker() {
        if (DEPENDENCY_HISTORY_FILE.exists()) {
            readVersionDependency();
        } else {
            try {
                FileUtil.createFile(DEPENDENCY_HISTORY_FILE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public DependencyTracker(List<String> currVerDependencies) {
        this();
        updateVersionDependency(UpdateManager.VERSION, currVerDependencies);
    }

    public void updateVersionDependency(int version, List<String> verDependencies) {
        dependenciesPerKnownVersions.put(version, verDependencies);
        writeVersionDependency();
    }

    private void writeVersionDependency() {
        try {
            FileUtil.writeToFile(DEPENDENCY_HISTORY_FILE, JsonUtil.toJsonString(dependenciesPerKnownVersions));
        } catch (JsonProcessingException e) {
            System.out.println("Failed to convert dependencies to JSON");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Failed to write dependencies to file");
            e.printStackTrace();
        }
    }

    private void readVersionDependency() {
        try {
            String json = FileUtil.readFromFile(DEPENDENCY_HISTORY_FILE);
            dependenciesPerKnownVersions = JsonUtil.fromJsonStringToGivenType(json,
                    new TypeReference<HashMap<Integer, List<String>>>() {
                    });
        } catch (IOException e) {
            System.out.println("Failed to read dependencies from file");
            e.printStackTrace();
        }
    }

    private void excludePlatformSpecificDependencies(List<String> dependencies) {
        List<String> windowsDependencies = new ArrayList<>();
        windowsDependencies.add("lib/jxbrowser-win-6.4.jar");
        List<String> macDependencies = new ArrayList<>();
        macDependencies.add("lib/jxbrowser-mac-6.4.jar");
        List<String> linux32Dependencies = new ArrayList<>();
        linux32Dependencies.add("lib/jxbrowser-linux32-6.4.jar");
        List<String> linux64Dependencies = new ArrayList<>();
        linux64Dependencies.add("lib/jxbrowser-linux64-6.4.jar");

        if (OsDetector.isOnWindows()) {
            dependencies.removeAll(macDependencies);
            dependencies.removeAll(linux32Dependencies);
            dependencies.removeAll(linux64Dependencies);
        } else if (OsDetector.isOnMac()) {
            dependencies.removeAll(windowsDependencies);
            dependencies.removeAll(linux32Dependencies);
            dependencies.removeAll(linux64Dependencies);
        } else if (OsDetector.isOn32BitsLinux()) {
            dependencies.removeAll(windowsDependencies);
            dependencies.removeAll(macDependencies);
            dependencies.removeAll(linux64Dependencies);
        } else if (OsDetector.isOn64BitsLinux()) {
            dependencies.removeAll(windowsDependencies);
            dependencies.removeAll(macDependencies);
            dependencies.removeAll(linux32Dependencies);
        }
    }

    public List<String> getMissingDependencies() {
        List<String> dependencies = dependenciesPerKnownVersions.get(UpdateManager.VERSION).stream()
                .map(String::new).collect(Collectors.toList());

        excludePlatformSpecificDependencies(dependencies);

        return dependencies.stream()
                .filter(dependency -> !FileUtil.isFileExists(dependency)).collect(Collectors.toList());
    }
}
