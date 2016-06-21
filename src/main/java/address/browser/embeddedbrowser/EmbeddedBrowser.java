package address.browser.embeddedbrowser;

import javafx.scene.Node;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * An interface for different type of browser engine.
 */
public interface EmbeddedBrowser {

    void loadUrl(String url);
    Node getBrowserView();
    boolean isLoading();
    void dispose();
    URL getUrl() throws MalformedURLException;
    String getUrlString();
    String getOriginUrlString();
    URL getOriginUrl() throws MalformedURLException;
    EbDocument getDomElement();
    void executeCommand(int command);
    void addLoadListener(EbLoadListener listener);

}
