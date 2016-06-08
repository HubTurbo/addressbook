package address.browser.page;

import address.browser.EmbeddedBrowser;

import java.util.HashMap;

/**
 * An abstract web page of an embedded browser.
 */
public abstract class Page {

    private EmbeddedBrowser browser;

    public Page(EmbeddedBrowser browser) {
        this.browser = browser;
    }

    public EmbeddedBrowser getBrowser() {
        return browser;
    }
}
