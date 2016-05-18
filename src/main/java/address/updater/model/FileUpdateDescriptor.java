package address.updater.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.net.URL;

/**
 * Download link for a file
 */
@XmlRootElement(name="fileupdatedescriptor")
@XmlAccessorType(XmlAccessType.FIELD)
public class FileUpdateDescriptor {
    private URI filePath;
    private URL downloadLink;

    /**
     * Required for XML conversion
     */
    public FileUpdateDescriptor() {}

    public FileUpdateDescriptor(URI filePath, URL downloadLink) {
        this.filePath = filePath;
        this.downloadLink = downloadLink;
    }

    public URI getFilePath() {
        return filePath;
    }

    public URL getDownloadLink() {
        return downloadLink;
    }
}
