package hubturbo.embeddedbrowser.page;

import hubturbo.embeddedbrowser.EbAttachListener;
import hubturbo.embeddedbrowser.EbLoadListener;

/**
 * An interface to specify the default page methods.
 */
public interface PageInterface {

    boolean isPageLoading();
    void setPageLoadFinishListener(EbLoadListener listener);

    /**
     * Adds a listener to listen when the page is (re)attached to a scene.
     * @param listener An EbAttachListener interface.
     */
    void setPageAttachedToSceneListener(EbAttachListener listener);
}
