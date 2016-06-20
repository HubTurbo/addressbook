package address.browser.page;

import address.browser.embeddedbrowser.*;
import address.browser.jxbrowser.JxDomEventListenerAdapter;

/**
 * A github profile page
 */
public class GithubProfilePage implements PageInterface{

    Page page;
    EmbeddedBrowser browser;

    public GithubProfilePage(Page page) {
        this.page = page;
        this.browser = page.getBrowser();
    }

    public boolean isValidGithubProfilePage(){
        EbElement repoContainer = browser.getDomElement().findElementByClass("js-pjax-container");
        EbElement repoLink = browser.getDomElement().findElementByClass("octicon octicon-repo");

        EbElement userRepoList = browser.getDomElement().findElementByClass("repo-list js-repo-list");
        EbElement organizationRepoList = browser.getDomElement().findElementByClass("org-repositories");

        return isElementFoundToNavigateToRepoPage(repoContainer, repoLink)
                || isRepoElementExist(userRepoList, organizationRepoList);
    }

    /**
     * Automates clicking on the Repositories tab and scrolling to the bottom of the page.
     */
    public void automateClickingAndScrolling() {
        synchronized (this) {
            try {
                EbElement repoContainer = browser.getDomElement().findElementById("js-pjax-container");
                EbElement repoLink = browser.getDomElement().findElementByClass("octicon octicon-repo");
                EbElement userRepoList = browser.getDomElement().findElementByClass("repo-list js-repo-list");
                EbElement organizationRepoList = browser.getDomElement().findElementById("org-repositories");

                if (isRepoElementExist(userRepoList, organizationRepoList)) {
                    browser.executeCommand(EbEditorCommand.SCROLL_TO_END_OF_DOCUMENT);
                    return;
                }

                if (isElementFoundToNavigateToRepoPage(repoContainer, repoLink)) {
                    repoContainer.addEventListener(EbDomEventType.ON_LOAD, new JxDomEventListenerAdapter(e ->
                            browser.executeCommand(EbEditorCommand.SCROLL_TO_END_OF_DOCUMENT)), true);
                    repoLink.click();
                }
            } catch (NullPointerException e) {
                //Page not supported as element not found in the page. Fail silently
            } catch (IllegalStateException e) {
                //Element not found. Fail silently.
            }
        }
    }

    private static boolean isRepoElementExist(EbElement userRepoList, EbElement organizationRepoList) {
        return userRepoList != null || organizationRepoList != null;
    }

    private static boolean isElementFoundToNavigateToRepoPage(EbElement container, EbElement link) {
        return link != null && container != null;
    }

    @Override
    public boolean isPageLoading() {
        return page.isPageLoading();
    }

    public void setPageLoadFinishListener(EbLoadListener listener){
        this.browser.addLoadListener(listener);
    }

}
