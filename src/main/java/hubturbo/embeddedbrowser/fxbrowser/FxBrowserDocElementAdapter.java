package hubturbo.embeddedbrowser.fxbrowser;

import hubturbo.embeddedbrowser.EbDomEventListener;
import hubturbo.embeddedbrowser.EbElement;
import org.w3c.dom.Element;

/**
 *
 */
public class FxBrowserDocElementAdapter implements EbElement {

    Element element;

    public FxBrowserDocElementAdapter(Element element) {
        this.element = element;
    }

    @Override
    public void click() {
        throw new RuntimeException("click() not supported in FxBrowser");
    }

    @Override
    public void addEventListener(int type, EbDomEventListener listener, boolean b) {
        throw new RuntimeException("addEventListener() not supported in FxBrowser");
    }
}
