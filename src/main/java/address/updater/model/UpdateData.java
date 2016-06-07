package address.updater.model;

import address.util.Version;

import java.util.ArrayList;

/**
 * Lists app's latest version's main app and libraries data as stored in server
 */
public class UpdateData {
    private Version version;
    private String mainAppFilename;
    private ArrayList<LibraryDescriptor> libraries = new ArrayList<>();

    public UpdateData() {}

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
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
}
