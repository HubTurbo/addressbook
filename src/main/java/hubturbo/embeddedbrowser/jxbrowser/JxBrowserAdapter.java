package hubturbo.embeddedbrowser.jxbrowser;

import hubturbo.embeddedbrowser.EbAttachListener;
import hubturbo.embeddedbrowser.EbLoadListener;
import hubturbo.EmbeddedBrowser;
import hubturbo.embeddedbrowser.EbDocument;
import com.teamdev.jxbrowser.chromium.events.*;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

/**
 * An EmbeddedBrowser adapter for the jxBrowser Browser.
 */
public class JxBrowserAdapter implements EmbeddedBrowser, LoadListener {

    private BrowserView browserView;

    private Optional<EbLoadListener> loadListener = Optional.empty();
    private Optional<EbAttachListener> attachListener = Optional.empty();
    private ChangeListener<Scene> sceneChangeListener = (observable, oldValue, newValue) -> {
        if (oldValue == null && newValue != null) {
            attachListener.ifPresent(EbAttachListener::onAttach);
        }
    };

    public JxBrowserAdapter(JxBrowser browser) {
        this.browserView = new BrowserView(browser);
        this.browserView.sceneProperty().addListener(sceneChangeListener);
    }

    @Override
    public void loadUrl(String url) {
        this.browserView.getBrowser().loadURL(url);
    }

    @Override
    public void loadHTML(String htmlCode) {
        this.browserView.getBrowser().loadHTML(htmlCode);
    }

    @Override
    public Node getBrowserView() {
        return this.browserView;
    }

    @Override
    public boolean isLoading() {
        return browserView.getBrowser().isLoading();
    }

    @Override
    public void dispose() {
        browserView.getBrowser().dispose();
    }

    @Override
    public URL getUrl() throws MalformedURLException {
        return new URL(getUrlString());
    }

    @Override
    public String getUrlString() {
        return browserView.getBrowser().getURL();
    }

    @Override
    public String getOriginUrlString() {
        return ((JxBrowser)browserView.getBrowser()).getOriginUrl();
    }

    @Override
    public URL getOriginUrl() throws MalformedURLException {
        return new URL(getOriginUrlString());
    }

    @Override
    public EbDocument getDomElement() {
        return new JxDocAdapter(browserView.getBrowser().getDocument());
    }

    @Override
    public void executeCommand(int command) {
        browserView.getBrowser().executeCommand(EmbeddedBrowserObjectMapper.convertEbEditorCommand(command));
    }

    @Override
    public void setLoadListener(EbLoadListener listener) {
        this.loadListener = Optional.ofNullable(listener);
        this.browserView.getBrowser().addLoadListener(this);
    }

    @Override
    public void setAttachListener(EbAttachListener listener) {
        this.attachListener = Optional.ofNullable(listener);
    }

    @Override
    public void reset() {
        this.browserView.getBrowser()
                        .getLoadListeners()
                        .stream().forEach(listener -> browserView.getBrowser().removeLoadListener(listener));
        this.attachListener = Optional.empty();
    }

    @Override
    public void onStartLoadingFrame(StartLoadingEvent startLoadingEvent) {
    }

    @Override
    public void onProvisionalLoadingFrame(ProvisionalLoadingEvent provisionalLoadingEvent) {

    }

    @Override
    public void onFinishLoadingFrame(FinishLoadingEvent finishLoadingEvent) {
        if (loadListener.isPresent()) {
            loadListener.get().onFinishLoadingFrame(finishLoadingEvent.isMainFrame());
        }
    }

    @Override
    public void onFailLoadingFrame(FailLoadingEvent failLoadingEvent) {

    }

    @Override
    public void onDocumentLoadedInFrame(FrameLoadEvent frameLoadEvent) {

    }

    @Override
    public void onDocumentLoadedInMainFrame(LoadEvent loadEvent) {

    }
}
