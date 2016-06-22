package hubturbo.embeddedbrowser.fxbrowser;

import hubturbo.embeddedbrowser.EbDocument;
import hubturbo.embeddedbrowser.EbElement;
import org.w3c.dom.Document;

/**
 *
 */
public class FxBrowserDocAdapter implements EbDocument {

    private Document doc;
    public FxBrowserDocAdapter(Document doc) {
        this.doc = doc;
    }


    @Override
    public EbElement findElementById(String id) {
        return new FxBrowserDocElementAdapter(doc.getElementById(id));
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
