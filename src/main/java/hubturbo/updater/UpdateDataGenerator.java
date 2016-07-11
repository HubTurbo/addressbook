package hubturbo.updater;

import address.MainApp;
import address.storage.StorageManager;
import address.updater.LibraryDescriptor;
import address.updater.VersionDescriptor;
import address.util.FileUtil;
import address.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Used to help developer create release in generating the update data
 */
public class UpdateDataGenerator {
    private static final String BASE_DOWNLOAD_LINK =
            "https://github.com/HubTurbo/addressbook/releases/download/Resources/";
    private static final File UPDATE_DATA_FILE = new File("UpdateData.json");

    public static void main(String[] args) {
        List<String> arguments = Arrays.asList(args);

        VersionDescriptor previousVersionDescriptor;

        try {
            previousVersionDescriptor = getPreviousUpdateData();
        } catch (IOException e) {
            System.out.println("Failed to read update data file");
            e.printStackTrace();
            return;
        }

        VersionDescriptor versionDescriptor = new VersionDescriptor();
        versionDescriptor.setVersion(MainApp.VERSION.toString());

        try {
            setUpdateDataMainAppDownloadLink(versionDescriptor, arguments.get(0));
        } catch (MalformedURLException e) {
            System.out.println("MainApp download link is a malformed URL");
            e.printStackTrace();
            return;
        }

        ArrayList<String> currentLibrariesName = new ArrayList<>(arguments.subList(1, arguments.size()));

        ArrayList<LibraryDescriptor> currentLibraryDescriptors = currentLibrariesName.stream()
                .map(libName -> new LibraryDescriptor(libName, null, null))
                .collect(Collectors.toCollection(ArrayList::new));

        populateCurrLibDescriptorDownloadLink(currentLibraryDescriptors);

        populateCurrLibDescriptorWithExistingLibDescriptorOs(previousVersionDescriptor.getLibraries(),
                                                             currentLibraryDescriptors);

        versionDescriptor.setLibraries(currentLibraryDescriptors);

        try {
            StorageManager.serializeObjectToJsonFile(UPDATE_DATA_FILE, versionDescriptor);
        } catch (IOException e) {
            System.out.println("Failed to write new update data to file");
            e.printStackTrace();
            return;
        }

        notifyOfNewLibrariesToBeGivenMoreInformation(currentLibraryDescriptors);
    }

    private static VersionDescriptor getPreviousUpdateData() throws IOException {
        return JsonUtil.fromJsonString(FileUtil.readFromFile(UPDATE_DATA_FILE), VersionDescriptor.class);
    }

    private static void setUpdateDataMainAppDownloadLink(VersionDescriptor versionDescriptor, String mainAppFilename)
            throws MalformedURLException {
        String mainAppDownloadLinkString = BASE_DOWNLOAD_LINK + mainAppFilename;

        versionDescriptor.setMainAppDownloadLink(mainAppDownloadLinkString);
    }

    private static URL getDownloadLinkForLib(String libFilename) throws MalformedURLException {
        return new URL(BASE_DOWNLOAD_LINK + libFilename);
    }

    private static void populateCurrLibDescriptorDownloadLink(ArrayList<LibraryDescriptor> currentLibraryDescriptors) {
        currentLibraryDescriptors.stream().forEach(libDesc -> {
            try {
                libDesc.setDownloadLink(getDownloadLinkForLib(libDesc.getFilename()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                System.out.println("Failed to set download link for " + libDesc.getFilename() +
                        "; please update the download link manually");
            }
        });
    }

    private static void populateCurrLibDescriptorWithExistingLibDescriptorOs(
            ArrayList<LibraryDescriptor> previousLibraryDescriptors,
            ArrayList<LibraryDescriptor> currentLibraryDescriptors) {
        currentLibraryDescriptors.stream()
                .forEach(libDesc ->
                        previousLibraryDescriptors.stream()
                                .filter(prevLibDesc -> prevLibDesc.getFilename().equals(libDesc.getFilename()))
                                .findFirst()
                                .ifPresent(prevLibDesc -> libDesc.setOs(prevLibDesc.getOs())));
    }

    private static void notifyOfNewLibrariesToBeGivenMoreInformation(ArrayList<LibraryDescriptor> libraryDescriptors) {
        System.out.println("------------------------------------------------------------");
        System.out.println("New libraries to be uploaded, given download URL and set OS:");
        libraryDescriptors.stream().filter(libDesc -> libDesc.getOs() == null)
                .forEach(libDesc -> System.out.println(libDesc.getFilename()));
        System.out.println("------------------------------------------------------------");
    }
}
