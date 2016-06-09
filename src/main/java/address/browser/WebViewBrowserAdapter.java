package address.browser;

import javafx.scene.Node;
import javafx.scene.web.WebView;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * An EmbeddedBrowser adapter for the Java WebView browser.
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

    @Override
    public URL getUrl() throws MalformedURLException {
        return new URL(getUrlString());
    }

    @Override
    public String getUrlString() {
        return webView.getEngine().getDocument().getDocumentURI();
    }

}
