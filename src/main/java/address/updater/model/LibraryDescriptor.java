package address.updater.model;


import address.util.OsDetector;

/**
 * Download link for a file
 */
public class LibraryDescriptor {

    private String filename;
    private OsDetector.Os os;

    public LibraryDescriptor() {}

    public LibraryDescriptor(String filename, OsDetector.Os os) {
        this.filename = filename;
        this.os = os;
    }

    public String getFilename() {
        return filename;
    }

    public OsDetector.Os getOs() {
        return os;
    }
}
