package address.EmbeddedBrowser;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.EditorCommand;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventType;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;

/**
 * Manages the browser.
 */
public class BrowserManager {

    private Browser browser;

    public BrowserManager() {
        this.browser = new Browser();
        registerListeners();
    }

    private void registerListeners() {
        this.browser.addLoadListener(new LoadAdapter() {
            @Override
            public void onFinishLoadingFrame(FinishLoadingEvent finishLoadingEvent) {
                automateClickingAndScrolling();
            }
        });
    }

    /**
     * Frees resources allocated to the browser.
     */
    public void freeBrowserResources(){
        browser.dispose();
    }

    public Browser getBrowser(){
        return browser;
    }

    private void automateClickingAndScrolling() {
        DOMElement container = browser.getDocument().findElement(By.id("js-pjax-container"));
        DOMElement link = browser.getDocument().findElement(By.className("octicon octicon-repo"));
        if(link != null) {
            container.addEventListener(DOMEventType.OnLoad, e ->
                            browser.executeCommand(EditorCommand.SCROLL_TO_END_OF_DOCUMENT)
                    , true);
            link.click();
        }
    }
}
