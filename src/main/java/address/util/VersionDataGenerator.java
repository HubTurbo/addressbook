package address.util;

import address.MainApp;
import commons.FileUtil;
import commons.JsonUtil;
import commons.LibraryDescriptor;
import commons.VersionData;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * This class is meant to help developers generate the latest app data into VersionData.json,
 * including the libraries and their download links
 *
 * To be used with gradle task generateVersionData to provide command line arguments, generally when creating a release
 * Command line arguments should be the name of libraries currently used, except the first argument which
 * should be the name of the main application
 */
public class VersionDataGenerator {
    private static final String BASE_DOWNLOAD_LINK =
            "https://github.com/HubTurbo/addressbook/releases/download/Resources/";
    private static final File VERSION_DATA_FILE = new File("VersionData.json");

    public static void main(String[] args) {
        VersionDataGenerator versionDataGenerator = new VersionDataGenerator();
        versionDataGenerator.generateNewVersionData(args);
    }

    /**
     * Attempts to generate data based on the given library list
     *
     * @param curLibraries at least 2 jar filenames, with first being the resource app
     */
    public void generateNewVersionData(String[] curLibraries) {
        List<String> curLibraryList = Arrays.asList(curLibraries);

        System.out.println("Libraries found: " + curLibraryList);
        Optional<VersionData> previousVersionData;
        try {
            previousVersionData = Optional.of(readVersionDataFromFile(VERSION_DATA_FILE));
        } catch (IOException e) {
            System.out.println("Warning: Failed to read previous version data file.");
            previousVersionData = Optional.empty();
        }

        VersionData versionData = new VersionData();
        versionData.setVersion(MainApp.VERSION.toString());

        List<LibraryDescriptor> currentLibrariesDescriptors = convertToLibraryDescriptors(curLibraryList);

        if (previousVersionData.isPresent()) {
            transferOsInformation(previousVersionData.get().getLibraries(), currentLibrariesDescriptors);
        }
        versionData.setLibraries(currentLibrariesDescriptors);

        try {
            FileUtil.serializeObjectToJsonFile(VERSION_DATA_FILE, versionData);
        } catch (IOException e) {
            System.out.println("Error: Failed to write new version data to file");
            e.printStackTrace();
            return;
        }

        notifyOfNewLibraries(currentLibrariesDescriptors);
    }

    private List<LibraryDescriptor> convertToLibraryDescriptors(List<String> librariesNames) {
        List<LibraryDescriptor> libraryDescriptors = new ArrayList<>();
        for (String libraryName : librariesNames) {
            try {
                libraryDescriptors.add(new LibraryDescriptor(libraryName, getDownloadLinkForLibrary(libraryName), null));
            } catch (MalformedURLException e) {
                System.out.println("Warning: Failed to set download link for " + libraryName +
                        "; please update the download link manually");
            }
        }
        return libraryDescriptors;
    }

    private VersionData readVersionDataFromFile(File file) throws IOException {
        return JsonUtil.fromJsonString(FileUtil.readFromFile(file), VersionData.class);
    }

    private String getDownloadLinkForLibrary(String libraryFileName) {
        return BASE_DOWNLOAD_LINK + libraryFileName;
    }

    /**
     * Attempts to transfer OS information from previousLibraryDescriptors
     * into matching libraryDescriptors in currentLibraryDescriptors
     *
     * Library descriptors are considered matching if they have the same file name
     * This method assumes that the there are no same library descriptors in the a list
     * @param previousLibraryDescriptors
     * @param currentLibraryDescriptors
     */
    private void transferOsInformation(List<LibraryDescriptor> previousLibraryDescriptors,
                                       List<LibraryDescriptor> currentLibraryDescriptors) {
        currentLibraryDescriptors.forEach(currentLibraryDescriptor -> {
            Optional<LibraryDescriptor> libraryDescriptorWithOsInformation = getMatchingLibraryDescriptor(currentLibraryDescriptor, previousLibraryDescriptors);
            libraryDescriptorWithOsInformation.ifPresent(desc -> currentLibraryDescriptor.setOs(desc.getOs()));
        });
    }

    /**
     * Attempts to find from the given list, a library descriptor with a matching filename as libraryDescriptor
     *
     * @param libraryDescriptor
     * @param prevLibraryDescriptors
     * @return
     */
    private Optional<LibraryDescriptor> getMatchingLibraryDescriptor(LibraryDescriptor libraryDescriptor, List<LibraryDescriptor> prevLibraryDescriptors) {
        return prevLibraryDescriptors.stream()
                .filter(prevLibDesc -> prevLibDesc.getFileName().equals(libraryDescriptor.getFileName()))
                .findFirst();
    }

    private void notifyOfNewLibraries(List<LibraryDescriptor> libraryDescriptors) {
        System.out.println("------------------------------------------------------------");
        System.out.println("For each updated library below, modify download URL if needed and set its OS:");
        libraryDescriptors.stream().filter(libDesc -> libDesc.getOs() == null)
                .forEach(libDesc -> System.out.println(libDesc.getFileName()));
        System.out.println("------------------------------------------------------------");
    }
}
