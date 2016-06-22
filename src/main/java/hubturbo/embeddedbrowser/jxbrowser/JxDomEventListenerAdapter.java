package hubturbo.embeddedbrowser.jxbrowser;

import hubturbo.embeddedbrowser.EbDomEventListener;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventListener;

/**
 *
 */
public class JxDomEventListenerAdapter implements EbDomEventListener {

    private DOMEventListener listener;

    public JxDomEventListenerAdapter(DOMEventListener listener) {
        this.listener = listener;
    }

    @Override
    public DOMEventListener getDomEventListener() {
        return listener;
    }
}
