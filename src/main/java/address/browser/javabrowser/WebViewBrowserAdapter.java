package address.browser.javabrowser;

import address.browser.embeddedbrowser.EbLoadListener;
import address.browser.embeddedbrowser.EmbeddedBrowser;
import address.browser.embeddedbrowser.EbDocument;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.Node;
import javafx.scene.web.WebView;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * An EmbeddedBrowser adapter for the Java WebView browser.
 */
public class WebViewBrowserAdapter implements EmbeddedBrowser, ChangeListener<Worker.State> {

    private WebView webView;

    private volatile Worker.State state;

    private EbLoadListener listener;

    public WebViewBrowserAdapter(WebView webview) {
        webView = webview;
        webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            state = newValue;
        });
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
        return state != Worker.State.SUCCEEDED;
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
        return webView.getEngine().getLocation();
    }

    @Override
    public String getOriginUrlString() {
        return getUrlString();
    }

    @Override
    public URL getOriginUrl() throws MalformedURLException {
        return new URL(getOriginUrlString());
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
    }

    @Override
    public void addLoadListener(EbLoadListener listener) {
        this.listener = listener;
        this.webView.getEngine().getLoadWorker().stateProperty().addListener(this);
    }

    @Override
    public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue,
                        Worker.State newValue) {
        if (newValue == Worker.State.SUCCEEDED) {
            listener.onFinishLoadingFrame(true);
        }
    }
}
