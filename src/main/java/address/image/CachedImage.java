package address.image;

import javafx.scene.image.Image;

/**
 * A wrapping class to bind relation of an image instance to an URL.
 */
public class CachedImage {

    private Image image;
    private String url;

    public CachedImage(Image image, String url) {
        this.image = image;
        this.url = url;
    }

    public Image getImage() {
        return image;
    }

    public String getUrl() {
        return url;
    }
}
