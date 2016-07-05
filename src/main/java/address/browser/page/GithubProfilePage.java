package address.browser.page;

import hubturbo.embeddedbrowser.EbDomEventType;
import hubturbo.embeddedbrowser.EbLoadListener;
import hubturbo.embeddedbrowser.jxbrowser.JxDomEventListenerAdapter;
import hubturbo.embeddedbrowser.page.Page;
import hubturbo.embeddedbrowser.EbAttachListener;
import hubturbo.embeddedbrowser.page.PageInterface;
import javafx.application.Platform;

/**
 * A GitHub profile page
 */
public class GithubProfilePage implements PageInterface {

    private static final String REPO_LIST_CLASS_NAME = "repo-list js-repo-list";
    private static final String ORGANIZATION_REPO_ID = "org-repositories";
    private static final String JS_PJAX_CONTAINER_ID = "js-pjax-container";
    private static final String OCTICON_REPO_CLASS_NAME = "octicon octicon-repo";

    private Page page;

    private Boolean wasAutoScrollingSetup = false;

    public GithubProfilePage(Page page) {
        this.page = page;
    }

    public boolean isValidGithubProfilePage(){
        return page.verifyPresence(new String[]{JS_PJAX_CONTAINER_ID, OCTICON_REPO_CLASS_NAME})
                || page.verifyPresence(new String[]{REPO_LIST_CLASS_NAME, ORGANIZATION_REPO_ID});
    }

    /**
     * Setup page automation.
     * Automation tasks: 1) Clicking on the Repositories tab(if not clicked).
     *                   2) Scrolling to the end of the page when a page is loaded.
     */
    public synchronized void setupPageAutomation() {
        if (!wasAutoScrollingSetup) {
            this.setPageLoadFinishListener(e -> Platform.runLater(this::executePageLoadedTasks));
            this.setPageAttachedToSceneListener(() -> Platform.runLater(this::executePageLoadedTasks));
            this.wasAutoScrollingSetup = true;
        }
    }

    /**
     * Executes Page loaded tasks
     * Tasks:
     * 1 ) Verify if page is at repositories page
     *      - if yes, scroll to the bottom of the page.
     *      - if no, click on the repositories tab and scroll to the bottom of the page.
     */
    private void executePageLoadedTasks() {
        try {
            if (page.verifyPresenceByClassNames(REPO_LIST_CLASS_NAME) || page.verifyPresenceByIds(ORGANIZATION_REPO_ID)) {
                page.scrollTo(Page.SCROLL_TO_END);
                return;
            }

            if (page.verifyPresence(new String[]{JS_PJAX_CONTAINER_ID, OCTICON_REPO_CLASS_NAME})) {
                page.getElementById(JS_PJAX_CONTAINER_ID).addEventListener(EbDomEventType.ON_LOAD, new JxDomEventListenerAdapter(e ->
                        Platform.runLater(() -> page.scrollTo(Page.SCROLL_TO_END))), true);
                page.clickOnElement(page.getElementByClass(OCTICON_REPO_CLASS_NAME));
            }
        } catch (NullPointerException e) {
            //Page not supported as element not found in the page. Fail silently
        } catch (IllegalStateException e) {
            //Element not found. Fail silently.
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

    @Override
    public void setPageAttachedToSceneListener(EbAttachListener listener) {
        page.setPageAttachedToSceneListener(listener);
    }
}
