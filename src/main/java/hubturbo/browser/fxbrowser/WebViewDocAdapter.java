package hubturbo.browser.fxbrowser;

import hubturbo.browser.embeddedbrowser.EbDocument;
import hubturbo.browser.embeddedbrowser.EbElement;
import org.w3c.dom.Document;

/**
 *
 */
public class WebViewDocAdapter implements EbDocument {

    private Document doc;
    public WebViewDocAdapter(Document doc) {
        this.doc = doc;
    }


    @Override
    public EbElement findElementById(String id) {
        return new WebViewDocElementAdapter(doc.getElementById(id));
    }

    @Override
    public EbElement findElementByTag(String tag) {
        return null;
    }

    @Override
    public EbElement findElementByClass(String className) {
        return null;
    }
}
