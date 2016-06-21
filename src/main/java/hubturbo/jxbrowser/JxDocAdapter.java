package hubturbo.jxbrowser;

import hubturbo.embeddedbrowser.EbDocument;
import hubturbo.embeddedbrowser.EbElement;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMDocument;

/**
 *
 */
public class JxDocAdapter implements EbDocument {

    DOMDocument domDocument;

    public JxDocAdapter(DOMDocument domDocument) {
        this.domDocument = domDocument;
    }


    @Override
    public EbElement findElementById(String id) {

        if (domDocument.findElement(By.id(id)) == null) {
            return null;
        }
        return new JxDocElementAdapter(domDocument.findElement(By.id(id)));
    }

    @Override
    public EbElement findElementByTag(String tag) {

        if (domDocument.findElement(By.tagName(tag)) == null) {
            return null;
        } else {
            return new JxDocElementAdapter(domDocument.findElement(By.tagName(tag)));
        }
    }

    @Override
    public EbElement findElementByClass(String className) {

        if (domDocument.findElement(By.className(className)) == null) {
            return null;
        }
        return new JxDocElementAdapter(domDocument.findElement(By.className(className)));
    }
}
