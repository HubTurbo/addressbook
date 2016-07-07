package address.util;

import java.io.Serializable;

/**
 * A Serializable class that contains the screen width and height.
 */
public class ScreenSize implements Serializable {

    private Double width;
    private Double height;

    public ScreenSize() {
    }

    public ScreenSize(Double width, Double height) {
        this.width = width;
        this.height = height;
    }

    public Double getWidth() {
        return width;
    }

    public Double getHeight() {
        return height;
    }

}
