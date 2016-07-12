package address.updater;

import address.util.OsDetector;

import java.net.URL;

/**
 * Contains information for a dependency library, including its download link
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

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public URL getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(URL downloadLink) {
        this.downloadLink = downloadLink;
    }

    public OsDetector.Os getOs() {
        return os;
    }

    public void setOs(OsDetector.Os os) {
        this.os = os;
    }
}
