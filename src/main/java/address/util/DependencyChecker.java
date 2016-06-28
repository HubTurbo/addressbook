package address.util;

import address.MainApp;
import address.exceptions.DependencyCheckException;
import address.updater.VersionDescriptor;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Checks if all required dependencies are present
 */
public class DependencyChecker {
    private static final AppLogger logger = LoggerManager.getLogger(DependencyChecker.class);
    private final String requiredJavaVersionString;
    private Runnable quitApp;

    public DependencyChecker(String requiredJavaVersionString, Runnable quitApp) {
        this.requiredJavaVersionString = requiredJavaVersionString;
        this.quitApp = quitApp;
    }

    public void verify() {
        logger.info("Verifying dependencies");

        try {
            checkJavaVersionDependency(requiredJavaVersionString);
        } catch (DependencyCheckException e) {
            showErrorDialogAndQuit("Java Version Check Failed", "There are missing dependencies", e.getMessage());
        }

        try {
            checkLibrariesDependency();
        } catch (DependencyCheckException e) {
            showErrorDialogAndQuit("Libraries Check Failed", "Your Java version is not compatible", e.getMessage());
        }

        logger.info("All dependencies present");
    }

    public void checkJavaVersionDependency(String javaVersion) throws DependencyCheckException {
        logger.info("Verifying java version dependency");

        JavaVersion requiredVersion;
        try {
            requiredVersion = JavaVersion.fromString(javaVersion);
        } catch (IllegalArgumentException e) {
            logger.warn("Required Java Version string cannot be parsed. This should have been covered by test.");
            assert false;
            return;
        }

        JavaVersion runtimeVersion;
        String javaRuntimeVersionString = System.getProperty("java.runtime.version");
        try {
            runtimeVersion = JavaVersion.fromString(javaRuntimeVersionString);
        } catch (IllegalArgumentException e) {
            throw new DependencyCheckException(String.format(
                    "Java runtime version (%s) is not known and may not be compatible with this app.",
                    javaRuntimeVersionString));
        }

        if (JavaVersion.isJavaVersionLower(runtimeVersion, requiredVersion)) {
            throw new DependencyCheckException(String.format(
                    "Your Java Version (%s) is lower than this app's requirement (%s). Please update your Java.",
                    runtimeVersion, requiredVersion));
        }
    }

    public void checkLibrariesDependency() throws DependencyCheckException {
        logger.info("Verifying dependency libraries are present");

        if (!ManifestFileReader.isRunFromJar()) {
            logger.info("Not running from JAR, will not run libraries check.");
            return;
        }

        List<String> missingDependencies = getMissingDependencies();

        if (!missingDependencies.isEmpty()) {
            StringBuilder message = new StringBuilder("Missing dependencies:\n");
            for (String missingDependency : missingDependencies) {
                message.append("- ").append(missingDependency).append("\n");
            }
            String missingDependenciesMessage = message.toString().trim();
            logger.warn(missingDependenciesMessage);

            throw new DependencyCheckException(missingDependenciesMessage);
        }
    }

    public List<String> getMissingDependencies() {
        Optional<List<String>> dependenciesWrapper = ManifestFileReader.getLibrariesInClasspathFromManifest();

        if (!dependenciesWrapper.isPresent()) {
            logger.info("Dependencies not present - not running check as this indicates not run from JAR");
            return new ArrayList<>();
        }

        List<String> dependencies = dependenciesWrapper.get();

        excludePlatformSpecificDependencies(dependencies);

        return dependencies.stream()
                .filter(dependency -> !FileUtil.isFileExists(dependency)).collect(Collectors.toList());
    }

    private void excludePlatformSpecificDependencies(List<String> dependencies) {
        String json = FileUtil.readFromInputStream(MainApp.class.getResourceAsStream("/UpdateData.json"));

        VersionDescriptor versionDescriptor;

        try {
            versionDescriptor = JsonUtil.fromJsonString(json, VersionDescriptor.class);
            //TODO: is it possible to have a generic static method in StorageManager to read/write various json files?
        } catch (IOException e) {
            logger.warn("Failed to parse JSON data to process platform specific dependencies", e);
            return;
        }

        List<String> librariesNotForCurrentMachine =  versionDescriptor.getLibraries().stream()
                .filter(libDesc -> libDesc.getOs() != OsDetector.Os.ANY && libDesc.getOs() != OsDetector.getOs())
                .map(libDesc -> "lib/" + libDesc.getFilename())
                .collect(Collectors.toList());

        dependencies.removeAll(librariesNotForCurrentMachine);
    }

    public void showErrorDialogAndQuit(String title, String headerText, String contentText) {
        JOptionPane.showMessageDialog(null, headerText + "\n\n" + contentText, title, JOptionPane.ERROR_MESSAGE);
        quitApp.run();
    }
}
