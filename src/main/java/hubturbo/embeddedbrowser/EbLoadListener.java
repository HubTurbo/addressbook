package hubturbo.embeddedbrowser;

/**
 * A listener to listen for page load activity
 */
public interface EbLoadListener {

    void onFinishLoadingFrame(boolean b);
}
