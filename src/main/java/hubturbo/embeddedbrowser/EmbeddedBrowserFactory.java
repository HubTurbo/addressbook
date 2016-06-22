package hubturbo.embeddedbrowser;

import hubturbo.EmbeddedBrowser;
import hubturbo.embeddedbrowser.fxbrowser.FxBrowserAdapter;
import hubturbo.embeddedbrowser.jxbrowser.JxBrowser;
import hubturbo.embeddedbrowser.jxbrowser.JxBrowserAdapter;
import javafx.scene.web.WebView;

/**
 * A factory class to creates embeddedBrowser instances.
 */
public class EmbeddedBrowserFactory {

    public enum Type {
        FULL_FEATURE_BROWSER, LIMITED_FEATURE_BROWSER;
    }

    private Type type;

    /**
     * @param type The type of browser. e.g. HyperBrowser.Type.FULL_FEATURE_BROWSER
     */
    public EmbeddedBrowserFactory(Type type){
        this.type = type;
    }

    public EmbeddedBrowser createBrowser(){
        if (type == Type.FULL_FEATURE_BROWSER) {
            //In the event of deadlocking again, try uncommenting the line below and passed to jxBrowser constructor
            //BrowserContext context = new BrowserContext(new BrowserContextParams("tmpTab" + i));
            return new JxBrowserAdapter(new JxBrowser());
        } else if (type == Type.LIMITED_FEATURE_BROWSER) {
            return new FxBrowserAdapter(new WebView());
        } else {
            throw new IllegalArgumentException("No such browser type");
        }
    }
}
