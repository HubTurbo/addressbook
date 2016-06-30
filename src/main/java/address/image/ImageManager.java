package address.image;


import address.MainApp;
import javafx.scene.image.Image;

import java.util.HashMap;

/**
 * An Image Manager that manages the remote images.
 * It performs simple caching of images that has been loaded before.
 * It assumes that remote images is the same from the time the image is loaded and until the application is closed.
 * This is a singleton class.
 */
public class ImageManager {

    private static ImageManager instance;

    HashMap<String, Image> imageHashMap;

    private ImageManager(){
        imageHashMap = new HashMap<>();
    }

    public static ImageManager getInstance(){
        if (instance == null) {
            instance = new ImageManager();
        }
        return instance;
    }

    public static Image getDefaultProfileImage() {
        return new Image(MainApp.class.getResourceAsStream("/images/default_profile_picture.png"));
    }

    /**
     * Gets image from the URL.
     * @param url The URL of the image.
     * @return The graphical view of the image.
     */
    public synchronized Image getImage(String url){
        Image cachedImage = imageHashMap.get(url);

        if (cachedImage != null) {
            return cachedImage;
        }

        Image newImage = new Image(url, 100.0, 100.0, true, false);
        imageHashMap.put(url, newImage);
        return newImage;
    }
}
