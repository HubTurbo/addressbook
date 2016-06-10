package address.browser.jxbrowser;

import address.browser.embeddedbrowser.EbDomEventListener;
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
