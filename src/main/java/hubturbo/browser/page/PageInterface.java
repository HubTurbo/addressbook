package hubturbo.browser.page;

import hubturbo.browser.embeddedbrowser.EbLoadListener;

/**
 * An interface to specify the default page methods.
 */
public interface PageInterface {

    boolean isPageLoading();
    void setPageLoadFinishListener(EbLoadListener listener);

}
