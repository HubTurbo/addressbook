package address.browser;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import javafx.scene.Node;

/**
 * An EmbeddedBrowser adapter for the jxBrowser Browser.
 */
public class JxBrowserAdapter implements EmbeddedBrowser {

    private BrowserView browserView;

    public JxBrowserAdapter(Browser browser) {
        this.browserView = new BrowserView(browser);
    }

    @Override
    public void loadPage(String url) {
        this.browserView.getBrowser().loadURL(url);
    }

    @Override
    public Node getBrowserView() {
        return this.browserView;
    }

    @Override
    public void dispose() {
        browserView.getBrowser().dispose();
    }


}
