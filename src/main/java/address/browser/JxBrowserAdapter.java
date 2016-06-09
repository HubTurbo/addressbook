package address.browser;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import javafx.scene.Node;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

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

    @Override
    public URL getUrl() throws MalformedURLException {

        return new URL(getUrlString());
    }

    @Override
    public String getUrlString() {
        return browserView.getBrowser().getURL();
    }

    @Override
    public String getBaseUrl() {

        URL url = null;
        String parsedString;
        try {
            url = new URL(getUrlString());
            parsedString = url.toURI().normalize().toASCIIString();
        } catch (MalformedURLException e) {
            return "";
        } catch (URISyntaxException e) {
            return "";
        }
        return parsedString;
        //return url.getProtocol() + "://" + url.getHost() + url.getPath();
    }


}
