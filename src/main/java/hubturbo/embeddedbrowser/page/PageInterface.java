package hubturbo.embeddedbrowser.page;

import hubturbo.embeddedbrowser.EbAttachListener;
import hubturbo.embeddedbrowser.EbLoadListener;

/**
 * An interface to specify the default page methods.
 */
public interface PageInterface {

    boolean isPageLoading();
    void setPageLoadFinishListener(EbLoadListener listener);
    void setPageAttachedToSceneListener(EbAttachListener listener);
}
