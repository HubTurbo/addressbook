package address.image;


import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * An Image Manager that manages the remote images.
 * It performs simple caching of images that has been loaded before.
 * It assumes that remote images is the same from the time the image is loaded and until the application is closed.
 * This is a singleton class.
 */
public class ImageManager {

    private static ImageManager instance;
    List<CachedImage> imageList;

    private ImageManager(){
        imageList = new ArrayList<>();
    }

    public static ImageManager getInstance(){
        if (instance == null) {
            instance = new ImageManager();
        }
        return instance;
    }

    /**
     * Gets image from the URL.
     * @param url The URL of the image.
     * @return The graphical view of the image.
     */
    public synchronized Image getImage(String url){
        Optional<CachedImage> cachedImage = imageList.stream().filter(img -> img.getUrl().equals(url)).findAny();

        if (cachedImage.isPresent()) {
            return cachedImage.get().getImage();
        }

        Image newImage = new Image(url);
        imageList.add(new CachedImage(newImage, url));
        return newImage;
    }
}
