package address.browser;

import javafx.scene.Node;

/**
 * An interface for different type of browser engine.
 */
public interface EmbeddedBrowser {

    void loadPage(String url);
    Node getBrowserView();
    void dispose();

}
