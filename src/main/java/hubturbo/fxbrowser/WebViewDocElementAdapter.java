package hubturbo.fxbrowser;

import hubturbo.embeddedbrowser.EbDomEventListener;
import hubturbo.embeddedbrowser.EbElement;
import org.w3c.dom.Element;

/**
 *
 */
public class WebViewDocElementAdapter implements EbElement {

    Element element;

    public WebViewDocElementAdapter(Element element) {
        this.element = element;
    }

    @Override
    public void click() {

    }

    @Override
    public void addEventListener(int type, EbDomEventListener listener, boolean b) {

    }
}
