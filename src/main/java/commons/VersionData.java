package commons;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Lists latest application's version, main app download link and libraries descriptors
 */
@JsonPropertyOrder({ "version", "mainApp", "libraries" })
public class VersionData {
    @JsonProperty("version")
    private String versionString;
    @JsonProperty("mainApp")
    private URL mainAppDownloadLink;
    private ArrayList<LibraryDescriptor> libraries = new ArrayList<>();

    public VersionData() {} // required for serialization

    public String getVersion() {
        return versionString;
    }

    public void setVersion(String versionString) {
        this.versionString = versionString;
    }

    public void setMainAppDownloadLink(String mainAppDownloadLinkString) throws MalformedURLException {
        this.mainAppDownloadLink = new URL(mainAppDownloadLinkString);
    }

    public void setLibraries(ArrayList<LibraryDescriptor> libraries) {
        this.libraries = libraries;
    }

    public ArrayList<LibraryDescriptor> getLibraries() {
        return libraries;
    }

    public URL getDownloadLinkForMainApp() {
        return mainAppDownloadLink;
    }
}
