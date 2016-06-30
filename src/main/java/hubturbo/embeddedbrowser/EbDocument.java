package hubturbo.embeddedbrowser;

/**
 *
 */
public interface EbDocument {

    EbElement findElementById(String id);
    EbElement findElementByTag(String tag);
    EbElement findElementByClass(String className);
}
