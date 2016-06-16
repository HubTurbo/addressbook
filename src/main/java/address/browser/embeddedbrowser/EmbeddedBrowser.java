package address.browser.embeddedbrowser;
//TODO: move the whole package to hubturbo.embeddedbrowser?
// can do the same for other embedded browser code that is not related to addressbook

import javafx.scene.Node;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * An interface for different type of browser engine.
 */
public interface EmbeddedBrowser {

    //TODO: add header comments to interface methods in this packagage?

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
