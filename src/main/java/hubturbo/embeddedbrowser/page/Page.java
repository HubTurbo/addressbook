package hubturbo.embeddedbrowser.page;

import hubturbo.EmbeddedBrowser;
import hubturbo.embeddedbrowser.EbLoadListener;

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
    public void setPageLoadFinishListener(EbLoadListener listener){
        this.browser.setLoadListener(listener);
    }

}
