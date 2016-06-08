package address.updater.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;

/**
 * Lists app's latest version's main app and libraries data as stored in server
 */
@JsonPropertyOrder({ "version", "mainApp", "libraries" })
public class UpdateData {
    @JsonIgnore
    private static String downloadLink = "https://github.com/HubTurbo/addressbook/releases/download/";

    @JsonProperty("version")
    private String versionString;
    @JsonProperty("mainApp")
    private String mainAppFilename;
    private ArrayList<LibraryDescriptor> libraries = new ArrayList<>();

    public UpdateData() {}

    public String getVersion() {
        return versionString;
    }

    public void setVersion(String versionString) {
        this.versionString = versionString;
    }

    public String getMainAppFilename() {
        return mainAppFilename;
    }

    public void setMainAppFilename(String mainAppFilename) {
        this.mainAppFilename = mainAppFilename;
    }

    public void setLibraries(ArrayList<LibraryDescriptor> libraries) {
        this.libraries = libraries;
    }

    public ArrayList<LibraryDescriptor> getLibraries() {
        return libraries;
    }

    public String getDownloadLinkForMainApp() {
        return getVersionDownloadLink() + convertNameToDownloadFileName(mainAppFilename);
    }

    public String getDownloadLinkForALibrary(LibraryDescriptor libraryDescriptor) {
        return getVersionDownloadLink() + convertNameToDownloadFileName(libraryDescriptor.getFilename());
    }

    private String getVersionDownloadLink() {
        return downloadLink + versionString + "/";
    }

    private static String convertNameToDownloadFileName(String name) {
        return name.replaceAll("\\s+", ".");
    }
}
