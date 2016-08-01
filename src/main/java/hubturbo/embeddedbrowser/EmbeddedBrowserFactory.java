package hubturbo.embeddedbrowser;

import hubturbo.EmbeddedBrowser;
import hubturbo.embeddedbrowser.fxbrowser.FxBrowserAdapter;
import javafx.scene.web.WebView;

/**
 * A factory class to create different concrete implementation of EmbeddedBrowser.
 */
public class EmbeddedBrowserFactory {

    public static EmbeddedBrowser createBrowser(BrowserType type){
        if (type == BrowserType.LIMITED_FEATURE_BROWSER) {
            return new FxBrowserAdapter(new WebView());
        } else {
            throw new IllegalArgumentException("No such browser type");
        }
    }
}
