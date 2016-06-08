package address.browser;

import javafx.scene.Node;
import javafx.scene.web.WebView;

/**
 * An EmbeddedBrowser adapter for Java WebView browser.
 */
public class WebViewBrowserAdapter implements EmbeddedBrowser {

    private WebView webView;

    public WebViewBrowserAdapter(WebView webview) {
        webView = webview;
    }

    @Override
    public void loadPage(String url) {
        webView.getEngine().load(url);
    }

    @Override
    public Node getBrowserView() {
        return webView;
    }

    @Override
    public void dispose() {
        webView = null;
    }
}
