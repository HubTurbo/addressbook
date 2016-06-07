package address.browser;

import java.util.HashMap;

/**
 * Created by YL Lim on 7/6/2016.
 */
public abstract class EmbeddedBrowserPage {

    private EmbeddedBrowser browser;

    public EmbeddedBrowserPage(EmbeddedBrowser browser) {
        this.browser = browser;
    }

    public EmbeddedBrowser getBrowser() {
        return browser;
    }
}
