package hubturbo.page;

import hubturbo.embeddedbrowser.EbLoadListener;

/**
 *
 */
public interface PageInterface {

    boolean isPageLoading();
    void setPageLoadFinishListener(EbLoadListener listener);

}
