package address.updater;

import address.util.OsDetector;

import java.net.URL;

/**
 * Download link for a file
 */
public class LibraryDescriptor {

    private String filename;
    private URL downloadLink;
    private OsDetector.Os os;

    public LibraryDescriptor() {} // required for serialization

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
