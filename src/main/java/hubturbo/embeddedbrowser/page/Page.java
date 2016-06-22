package hubturbo.embeddedbrowser.page;

import hubturbo.EmbeddedBrowser;
import hubturbo.embeddedbrowser.EbEditorCommand;
import hubturbo.embeddedbrowser.EbElement;
import hubturbo.embeddedbrowser.EbLoadListener;

import java.util.Arrays;
import java.util.Optional;

/**
 * An abstract web page of an embedded browser.
 */
public class Page implements PageInterface{

    public static int SCROLL_TO_END = EbEditorCommand.SCROLL_TO_END_OF_DOCUMENT;

    private EmbeddedBrowser browser;

    public Page(EmbeddedBrowser browser) {
        this.browser = browser;
    }

    public EmbeddedBrowser getBrowser() {
        return browser;
    }

    @Override
    public boolean isPageLoading(){
        return this.browser.isLoading();
    }

    public EbElement getElementByClass(String className) {
        return browser.getDomElement().findElementByClass(className);
    }

    public EbElement getElementById(String id) {
        return browser.getDomElement().findElementById(id);
    }

    /**
     * Scrolls the web page. For e.g. Page.SCROLL_TO_END
     * @param scrollType
     */
    public void scrollTo(int scrollType) {
        browser.executeCommand(scrollType);
    }

    /**
     * Clicks on the DOM element.
     * @param repoLink The DOM element that is to be clicked.
     */
    public void clickOnElement(EbElement repoLink) {
        repoLink.click();
    }

    /**
     * Verify if elements are present by classname or id.
     * @param classNamesOrIds Array of className or Id in String form.
     * @return
     */
    public boolean verifyPresence(String[] classNamesOrIds){
        Optional<String> notPresent = Arrays.stream(classNamesOrIds).filter(s
                -> browser.getDomElement().findElementByClass(s) == null && browser.getDomElement().findElementById(s) == null).findAny();
        return !notPresent.isPresent();
    }

    /**
     * Verify if elements are present by classnames.
     * @param classNames Array of className in String form.
     * @return
     */
    public boolean verifyPresenceByClassNames(String[] classNames){
        Optional<String> notPresent = Arrays.stream(classNames).filter(s
                -> browser.getDomElement().findElementByClass(s) == null).findAny();
        return !notPresent.isPresent();
    }

    /**
     * Verify if element is present by classname.
     * @param className Classname in String form.
     * @return
     */
    public boolean verifyPresenceByClassNames(String className){
        return this.getElementByClass(className) != null;
    }

    /**
     * Verify if elements are present by ID.
     * @param ids Array of ID in String form.
     * @return
     */
    public boolean verifyPresenceByIds(String[] ids){
        Optional<String> notPresent = Arrays.stream(ids).filter(s
                -> browser.getDomElement().findElementById(s) == null).findAny();
        return !notPresent.isPresent();
    }

    /**
     * Verify if element is present by ID.
     * @param id ID in String form.
     * @return
     */
    public boolean verifyPresenceByIds(String id){
        return this.getElementById(id) != null;
    }

    @Override
    public void setPageLoadFinishListener(EbLoadListener listener){
        this.browser.addLoadListener(listener);
    }
}
