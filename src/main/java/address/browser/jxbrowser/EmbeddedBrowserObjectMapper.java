package address.browser.jxbrowser;

import address.browser.embeddedbrowser.EbDomEventType;
import address.browser.embeddedbrowser.EbEditorCommand;
import com.teamdev.jxbrowser.chromium.EditorCommand;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventType;

/**
 *
 */
public class EmbeddedBrowserObjectMapper {

    public static EditorCommand convertEbEditorCommand(int command) {
        switch(command) {
            case  EbEditorCommand.SCROLL_TO_END_OF_DOCUMENT:
                return EditorCommand.SCROLL_TO_END_OF_DOCUMENT;
        }
        throw new IllegalArgumentException("No such command");
    }

    public static DOMEventType convertEbDomEventType(int eventType) {
        switch(eventType) {
            case EbDomEventType.ON_LOAD:
                return DOMEventType.OnLoad;
        }
        throw new IllegalArgumentException("No such event type");
    }

}
