package hubturbo.embeddedbrowser.jxbrowser;

import hubturbo.embeddedbrowser.EbDomEventListener;
import hubturbo.embeddedbrowser.EbElement;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventListener;

/**
 *
 */
public class JxDocElementAdapter implements EbElement {

    private DOMElement element;

    public JxDocElementAdapter(DOMElement element){
        this.element = element;
    }

    @Override
    public void click() {
        this.element.click();
    }

    @Override
    public void addEventListener(int type, EbDomEventListener listener, boolean b) {
        element.addEventListener(EmbeddedBrowserObjectMapper.convertEbDomEventType(type),
                (DOMEventListener) listener.getDomEventListener(), b);
    }
}
