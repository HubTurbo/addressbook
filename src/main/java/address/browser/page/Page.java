package address.browser.page;

import address.browser.embeddedbrowser.EmbeddedBrowser;

/**
 * An abstract web page of an embedded browser.
 */
public class Page implements PageInterface{

    private EmbeddedBrowser browser;

    public Page(EmbeddedBrowser browser) {
        this.browser = browser;
    }

    public EmbeddedBrowser getBrowser() {
        return browser;
    }

    @Override
    public boolean isPageLoading(){
        return this.browser.isLoading();
    }
}
