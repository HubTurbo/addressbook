package hubturbo.browser.page;

import hubturbo.EmbeddedBrowser;
import hubturbo.browser.embeddedbrowser.EbElement;
import hubturbo.browser.embeddedbrowser.EbLoadListener;

import java.util.Arrays;
import java.util.Optional;

/**
 * An abstract web page of an embedded browser.
 */
public class Page implements PageInterface{

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

    public void scrollTo(int scrollType) {
        browser.executeCommand(scrollType);
    }

    public void clickOnElement(EbElement repoLink) {
        repoLink.click();
    }

    /**
     * Verify if elements are present by classname or id.
     * @param classNamesOrIds
     * @return
     */
    public boolean verifyPresence(String[] classNamesOrIds){
        Optional<String> notPresent = Arrays.stream(classNamesOrIds).filter(s
                -> browser.getDomElement().findElementByClass(s) == null && browser.getDomElement().findElementById(s) == null).findAny();
        return !notPresent.isPresent();
    }

    public boolean verifyPresenceByClassNames(String[] classNames){
        Optional<String> notPresent = Arrays.stream(classNames).filter(s
                -> browser.getDomElement().findElementByClass(s) == null).findAny();
        return !notPresent.isPresent();
    }

    public boolean verifyPresenceByClassNames(String className){
        return this.getElementByClass(className) != null;
    }

    public boolean verifyPresenceByIds(String[] ids){
        Optional<String> notPresent = Arrays.stream(ids).filter(s
                -> browser.getDomElement().findElementById(s) == null).findAny();
        return !notPresent.isPresent();
    }

    public boolean verifyPresenceByIds(String id){
        return this.getElementById(id) != null;
    }

    @Override
    public void setPageLoadFinishListener(EbLoadListener listener){
        this.browser.addLoadListener(listener);
    }
}
