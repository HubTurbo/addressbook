package address.updater.model;
//TODO: perhaps no need to have a model package here (to avoid confusion with other model packages)


import address.util.OsDetector;

import java.net.URL;

/**
 * Download link for a file
 */
public class LibraryDescriptor {

    private String filename;
    private URL downloadLink;
    private OsDetector.Os os;

    public LibraryDescriptor() {}
    //TODO: not used?

    public LibraryDescriptor(String filename, URL downloadLink, OsDetector.Os os) {
        this.filename = filename;
        this.downloadLink = downloadLink;
        this.os = os;
    }

    public String getFilename() {
        return filename;
    }

    public URL getDownloadLink() {
        return downloadLink;
    }

    public OsDetector.Os getOs() {
        return os;
    }
}
