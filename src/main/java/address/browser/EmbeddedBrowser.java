package address.browser;

import javafx.scene.Node;

/**
 * Created by YL Lim on 7/6/2016.
 */
public interface EmbeddedBrowser {

    void loadPage(String url);
    Node getBrowserView();

}
