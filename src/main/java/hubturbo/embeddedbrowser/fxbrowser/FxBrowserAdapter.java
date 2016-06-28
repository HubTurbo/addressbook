package hubturbo.embeddedbrowser.fxbrowser;

import hubturbo.embeddedbrowser.EbAttachListener;
import hubturbo.embeddedbrowser.EbLoadListener;
import hubturbo.EmbeddedBrowser;
import hubturbo.embeddedbrowser.EbDocument;
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
public class FxBrowserAdapter implements EmbeddedBrowser, ChangeListener<Worker.State> {

    private WebView webView;

    private volatile Worker.State state;

    private EbLoadListener listener;

    public FxBrowserAdapter(WebView webview) {
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
    public void loadHTML(String htmlCode) {
        webView.getEngine().loadContent(htmlCode);
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
        return new FxBrowserDocAdapter(this.webView.getEngine().getDocument());
    }

    @Override
    public void executeCommand(int command) {
        throw new RuntimeException("executeCommand() not supported by FxBrowserAdapter");
    }

    @Override
    public void setLoadListener(EbLoadListener listener) {
        this.listener = listener;
        this.webView.getEngine().getLoadWorker().stateProperty().addListener(this);
    }

    @Override
    public void setAttachListener(EbAttachListener listener) {
        throw new RuntimeException("setAttachListener() not supported by FxBrowserAdapter");
    }

    @Override
    public void reset() {
        throw new RuntimeException("reset() not supported by FxBrowserAdapter");
    }

    @Override
    public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue,
                        Worker.State newValue) {
        if (newValue == Worker.State.SUCCEEDED) {
            listener.onFinishLoadingFrame(true);
        }
    }
}
