package address.browser.javabrowser;

import address.browser.embeddedbrowser.EbLoadListener;
import address.browser.embeddedbrowser.EmbeddedBrowser;
import address.browser.embeddedbrowser.EbDocument;
import com.teamdev.jxbrowser.chromium.EditorCommand;
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
    public void loadUrl(String url) {
        webView.getEngine().load(url);
    }

    @Override
    public Node getBrowserView() {
        return webView;
    }

    @Override
    public boolean isLoading() {
        return webView.getEngine().getLoadWorker().isRunning();
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
        if (webView.getEngine().getDocument() == null) {
            return "";
        }
        return webView.getEngine().getLocation();
    }

    @Override
    public EbDocument getDomElement() {
        if (this.webView.getEngine().getDocument() == null) {
            return null;
        }
        return new WebViewDocAdapter(this.webView.getEngine().getDocument());
    }

    @Override
    public void executeCommand(int command) {
        //Not supported on web view browser.
    }

    @Override
    public void addLoadListener(EbLoadListener listener) {

    }

}
