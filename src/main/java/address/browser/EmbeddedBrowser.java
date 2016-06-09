package address.browser;

import javafx.scene.Node;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * An interface for different type of browser engine.
 */
public interface EmbeddedBrowser {

    void loadPage(String url);
    Node getBrowserView();
    void dispose();
    URL getUrl() throws MalformedURLException;
    String getUrlString();
    String getBaseUrl();

}
