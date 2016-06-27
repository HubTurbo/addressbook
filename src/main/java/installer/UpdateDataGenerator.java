package installer;

import address.MainApp;
import address.updater.model.LibraryDescriptor;
import address.updater.model.UpdateData;
import address.util.FileUtil;
import address.util.JsonUtil;
import address.util.OsDetector;
import address.util.Version;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Used to help developer create release in generating the update data
 */
public class UpdateDataGenerator {
    private static final String MAIN_APP_BASE_DOWNLOAD_LINK =
            "https://github.com/HubTurbo/addressbook/releases/download/";
    private static final File UPDATE_DATA_FILE = new File("UpdateData.json");

    public static void main(String[] args) {
        List<String> arguments = Arrays.asList(args);

        String json;

        try {
            json = FileUtil.readFromFile(UPDATE_DATA_FILE);
        } catch (IOException e) {
            System.out.println("Failed to read update data file");
            e.printStackTrace();
            return;
        }

        UpdateData updateData;

        try {
            updateData = JsonUtil.fromJsonString(json, UpdateData.class);
        } catch (IOException e) {
            System.out.println("Failed to parse update data json");
            e.printStackTrace();
            return;
        }

        updateData.setVersion(MainApp.VERSION.toString());

        String mainAppFilename = arguments.get(0);
        String mainAppDownloadLinkString = MAIN_APP_BASE_DOWNLOAD_LINK + MainApp.VERSION.toString() + "/" +
                mainAppFilename;

        try {
            updateData.setMainAppDownloadLink(mainAppDownloadLinkString);
        } catch (MalformedURLException e) {
            System.out.println("MainApp download link is a malformed URL");
            e.printStackTrace();
            return;
        }

        ArrayList<LibraryDescriptor> previousLibrariesDescriptor = updateData.getLibraries();

        ArrayList<String> currentLibrariesName = new ArrayList<>(arguments.subList(1, arguments.size()));
        ArrayList<LibraryDescriptor> currentLibrariesDescriptor = currentLibrariesName.stream()
                .map(libName -> previousLibrariesDescriptor.stream()
                        .filter(oldLib -> oldLib.getFilename().equals(libName)).findFirst()
                        .orElse(new LibraryDescriptor(libName, null, OsDetector.Os.ANY)))
                .collect(Collectors.toCollection(ArrayList::new));

        updateData.setLibraries(currentLibrariesDescriptor);

        try {
            FileUtil.writeToFile(new File("UpdateData.json"), JsonUtil.toJsonString(updateData));
        } catch (IOException e) {
            System.out.println("Failed to write new update data to file");
            e.printStackTrace();
            return;
        }

        System.out.println("------------------------------------------------------------");
        System.out.println("New libraries to be uploaded, given download URL and set OS:");
        currentLibrariesDescriptor.stream().filter(libDesc -> libDesc.getDownloadLink() == null)
                .forEach(libDesc -> System.out.println(libDesc.getFilename()));
        System.out.println("------------------------------------------------------------");
        //TODO: improve SLAP in this method?
    }
}
