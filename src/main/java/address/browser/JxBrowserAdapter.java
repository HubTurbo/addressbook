package address.browser;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import javafx.scene.Node;

/**
 * Created by YL Lim on 7/6/2016.
 */
public class JxBrowserAdapter implements EmbeddedBrowser {

    private BrowserView browserView;

    public JxBrowserAdapter() {
        this.browserView = new BrowserView(new Browser());
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
