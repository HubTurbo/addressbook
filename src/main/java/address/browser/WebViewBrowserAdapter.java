package address.browser;

import javafx.scene.Node;
import javafx.scene.web.WebView;

/**
 * Created by YL Lim on 7/6/2016.
 */
public class WebViewBrowserAdapter implements EmbeddedBrowser {

    private WebView webView;

    public WebViewBrowserAdapter() {
        webView = new WebView();
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
