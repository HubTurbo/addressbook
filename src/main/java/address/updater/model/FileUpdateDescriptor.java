package address.updater.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.net.URL;

/**
 * Download link for a file
 */
public class FileUpdateDescriptor {
    public enum Os {
        WINDOWS, MAC, LINUX32, LINUX64, ALL
    }

    private boolean isMainApp;
    private URL downloadLink;
    private Os os;

    public FileUpdateDescriptor() {}

    public FileUpdateDescriptor(URL downloadLink, Os os, boolean isMainApp) {
        this.downloadLink = downloadLink;
        this.os = os;
        this.isMainApp = isMainApp;
    }

    /**
     * Gets the file that this update applies to
     */
    public String getDestinationFile() {
        if (isMainApp) {
            return "addressbook.jar";
        } else {
            String urlString = downloadLink.getFile();
            String filename = urlString.substring(urlString.lastIndexOf('/') + 1).split("\\?")[0].split("#")[0];

            return "lib" + File.separator + filename;
        }
    }

    public URL getDownloadLink() {
        return downloadLink;
    }

    public Os getOs() {
        return os;
    }
}
