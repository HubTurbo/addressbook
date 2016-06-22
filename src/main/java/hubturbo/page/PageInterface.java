package hubturbo.page;

import hubturbo.embeddedbrowser.EbLoadListener;

/**
 * An interface to specify the default page methods.
 */
public interface PageInterface {

    boolean isPageLoading();
    void setPageLoadFinishListener(EbLoadListener listener);

}
