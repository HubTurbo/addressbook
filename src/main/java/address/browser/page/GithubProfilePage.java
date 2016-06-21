package address.browser.page;

import hubturbo.EmbeddedBrowser;
import hubturbo.embeddedbrowser.*;
import hubturbo.jxbrowser.JxDomEventListenerAdapter;
import hubturbo.page.Page;
import hubturbo.page.PageInterface;
import javafx.application.Platform;

/**
 * A github profile page
 */
public class GithubProfilePage implements PageInterface {
    //TODO: some code in this class can be generalized to utility methods and pushed to the Page class

    Page page;
    EmbeddedBrowser browser;

    public GithubProfilePage(Page page) {
        this.page = page;
        this.browser = page.getBrowser();
    }

    public boolean isValidGithubProfilePage(){
        return page.verifyPresenceByClassNames(new String[]{"js-pjax-container", "octicon octicon-repo", "repo-list js-repo-list","org-repositories" });
    }

    /**
     * Automates clicking on the Repositories tab and scrolling to the bottom of the page.
     */
    public void automateClickingAndScrolling() {
        //TODO: click and scrollTo should be two methods in the Page class?
        synchronized (this) {
            try {
                if (page.verifyPresenceByClassNames("repo-list js-repo-list") || page.verifyPresenceByIds("org-repositories")) {
                    page.scrollTo(EbEditorCommand.SCROLL_TO_END_OF_DOCUMENT);
                    return;
                }

                if (page.verifyPresence(new String[]{"js-pjax-container", "octicon octicon-repo"})) {
                    page.getElementById("js-pjax-container").addEventListener(EbDomEventType.ON_LOAD, new JxDomEventListenerAdapter(e ->
                            Platform.runLater(() -> page.scrollTo(EbEditorCommand.SCROLL_TO_END_OF_DOCUMENT))), true);
                    page.clickOnElement(page.getElementByClass("octicon octicon-repo"));
                }
            } catch (NullPointerException e) {
                //Page not supported as element not found in the page. Fail silently
            } catch (IllegalStateException e) {
                //Element not found. Fail silently.
            }
        }
    }

    @Override
    public boolean isPageLoading() {
        return page.isPageLoading();
    }

    @Override
    public void setPageLoadFinishListener(EbLoadListener listener) {
        page.setPageLoadFinishListener(listener);
    }
}
