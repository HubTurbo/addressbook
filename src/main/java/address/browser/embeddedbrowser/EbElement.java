package address.browser.embeddedbrowser;

/**
 *
 */
public interface EbElement {
    void click();
    void addEventListener(int type, EbDomEventListener listener, boolean b);

}
