package address.browser;

import address.model.ModelManager;
import address.model.Person;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.EditorCommand;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventType;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
import javafx.scene.control.TabPane;

import java.util.List;

/**
 * Manages the browser.
 */
public class BrowserManager {

    private static final int PERSON_DOES_NOT_EXISTS_IN_BROWSER = -1;
    private static final int NUMBER_OF_PRELOADED_PAGE = 3;
    public static final String INITIAL_PAGE_HTML_CODE =
            "<html><body><h3>To view contact's web page, click on the contact on the left.</h3></body></html>";

    private TabBrowser browser;

    private ModelManager modelManager;

    public BrowserManager(ModelManager modelManager) {
        this.modelManager = modelManager;
        browser = new TabBrowser(NUMBER_OF_PRELOADED_PAGE);
        registerListeners();
    }

    private void registerListeners() {
        for (int i = 0; i < browser.getNumberOfPages(); i++) {
            browser.getPage(i).addLoadListener(new LoadAdapter() {
                @Override
                public void onFinishLoadingFrame(FinishLoadingEvent finishLoadingEvent) {
                    if (finishLoadingEvent.isMainFrame()) {
                        automateClickingAndScrolling(finishLoadingEvent.getBrowser());
                        System.out.println("finish loading frame");
                    }
                }
            });
        }
    }

    /**
     * Initialise browser to initial state.
     */
    public void initBrowser() {
        for (int i = 0; i < browser.getNumberOfPages(); i++){
            browser.getPage(i).loadHTML(INITIAL_PAGE_HTML_CODE);
        }
    }

    /**
     * Loads the person's GitHub profile page to the browser.
     * PreCondition: ModelManager getFilteredPersons method returns more than one person.
     * @param person
     */
    public void loadGitHubProfilePage(Person person) {

        List<Person> filteredPersons = modelManager.getFilteredPersons();
        int indexOfContactsInFilteredListView = filteredPersons.size();

        int indexOfSelectedTabBrowserPage = findIndexOfTabBrowserThatContainsPersonGitHubWebPage(person);

        if (indexOfSelectedTabBrowserPage == PERSON_DOES_NOT_EXISTS_IN_BROWSER) {
            indexOfSelectedTabBrowserPage = findASuitableBrowserTabAndLoadThePersonGitHubProfilePage(
                                            person, filteredPersons, indexOfContactsInFilteredListView);
        }
        browser.selectTab(indexOfSelectedTabBrowserPage);

        updateOtherBrowserTabs(filteredPersons, indexOfSelectedTabBrowserPage);
    }

    /**
     * Returns the index of the tab in the TabBrowser that contains the person's GitHub profile page.
     * @param person
     * @return the index of the tab in the TabBrowser that contains the person's GitHub profile page
     *         or -1 if the tabs of the TabBrowser do not contain the person's GitHub profile page.
     */
    private int findIndexOfTabBrowserThatContainsPersonGitHubWebPage(Person person) {
        int indexOfSelectedTabBrowserPage = PERSON_DOES_NOT_EXISTS_IN_BROWSER;
        for (TabBrowserPage page : browser.getAllPages()) {
            if (!page.isAssignedToLoadAPersonGitHubProfilePage()) {
                continue;
            }
            if (page.getPerson().equals(person)) {
                indexOfSelectedTabBrowserPage = browser.indexOf(page);
                break;
            }
        }
        return indexOfSelectedTabBrowserPage;
    }

    private int findASuitableBrowserTabAndLoadThePersonGitHubProfilePage(Person person, List<Person> persons,
                                                                         int indexOfContactsInFilteredListView) {

        int indexOfSelectedTabBrowserPage = 0;

        for (TabBrowserPage page : browser.getAllPages()) {
            if (!page.isAssignedToLoadAPersonGitHubProfilePage()) {
                continue;
            }
            int pagePersonIndex = persons.indexOf(page.getPerson());
            if (pagePersonIndex < indexOfContactsInFilteredListView) {
                indexOfContactsInFilteredListView = pagePersonIndex;
                indexOfSelectedTabBrowserPage = browser.indexOf(page);
            }
        }
        browser.getAllPages().get(indexOfSelectedTabBrowserPage).loadURL(person.getPersonGitHubProfilePageUrl(),
                person);
        return indexOfSelectedTabBrowserPage;
    }

    private void updateOtherBrowserTabs(List<Person> persons, int indexOfSelectedTabBrowserPage) {
        int indexOfNextTabBrowserPage = (indexOfSelectedTabBrowserPage + 1) % browser.getNumberOfPages();
        int indexOfNextPersonInFilteredList = (persons.indexOf(
                                               browser.getPage(indexOfSelectedTabBrowserPage).getPerson()) + 1)
                                               % persons.size();

        do {
            TabBrowserPage nextTabBrowserPage = browser.getPage(indexOfNextTabBrowserPage);
            Person nextPersonInFilteredList = persons.get(indexOfNextPersonInFilteredList);
            if (!nextTabBrowserPage.isAssignedToLoadAPersonGitHubProfilePage()
                || !nextTabBrowserPage.getPerson().equals(nextPersonInFilteredList)) {
                nextTabBrowserPage.loadURL(nextPersonInFilteredList.getPersonGitHubProfilePageUrl(),
                                           nextPersonInFilteredList);
            }
            indexOfNextTabBrowserPage = (indexOfNextTabBrowserPage + 1) % browser.getNumberOfPages();
            indexOfNextPersonInFilteredList = (indexOfNextPersonInFilteredList + 1) % persons.size();
        } while (indexOfNextTabBrowserPage != indexOfSelectedTabBrowserPage);
    }

    private void automateClickingAndScrolling(Browser browser) {
        DOMElement repoContainer = browser.getDocument().findElement(By.id("js-pjax-container"));
        DOMElement repoLink = browser.getDocument().findElement(By.className("octicon octicon-repo"));

        DOMElement userRepoList = browser.getDocument().findElement(By.className("repo-list js-repo-list"));
        DOMElement organizationRepoList = browser.getDocument().findElement(By.id("org-repositories"));

        if (isRepoElementsExist(userRepoList, organizationRepoList)) {
            browser.executeCommand(EditorCommand.SCROLL_TO_END_OF_DOCUMENT);
            return;
        }

        if (isElementsFoundToNavigateToRepoPage(repoContainer, repoLink)) {
            repoContainer.addEventListener(DOMEventType.OnLoad, e ->
                            browser.executeCommand(EditorCommand.SCROLL_TO_END_OF_DOCUMENT)
                    , true);
            repoLink.click();
        }
    }

    private boolean isRepoElementsExist(DOMElement userRepoList, DOMElement organizationRepoList) {
        return userRepoList != null || organizationRepoList != null;
    }

    private boolean isElementsFoundToNavigateToRepoPage(DOMElement container, DOMElement link) {
        return link != null && container != null;
    }

    public TabPane getTabBrowser() {
        return this.browser;
    }

    /**
     * Frees resources allocated to the browser.
     */
    public void freeBrowserResources() {
        browser.dispose();
    }
}
