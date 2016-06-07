package address.updater.model;



/**
 * Download link for a file
 */
public class LibraryDescriptor {
    public enum Os {
        WINDOWS, MAC, LINUX32, LINUX64, ALL
    }

    private String filename;
    private Os os;

    public LibraryDescriptor() {}

    public LibraryDescriptor(String filename, Os os) {
        this.filename = filename;
        this.os = os;
    }

    public String getFilename() {
        return filename;
    }

    public Os getOs() {
        return os;
    }
}
