package hubturbo.embeddedbrowser.page;

import hubturbo.embeddedbrowser.EbLoadListener;

/**
 * An interface to specify the default page methods.
 */
public interface PageInterface {
    void setPageLoadFinishListener(EbLoadListener listener);
}
