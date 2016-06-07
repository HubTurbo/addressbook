package address.browser;

import address.model.datatypes.Person;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.EditorCommand;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventType;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
import com.teamdev.jxbrowser.chromium.internal.URLUtil;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A browser for displaying AddressBook contacts' profile page.
 */
public class AddressBookBrowser {

    private static final String INSTRUCTION_PAGE_HTML_CODE =
            "<h3><center><font color=\"grey\">To view contact's web page, click on the contact on the left.</font></center></h3></body></html>";

    private ArrayList<BrowserTab> browserTabs;
    private ObservableList<Person> filteredPersons;
    private TabPane addressBookBrowserView;

    private int noOfTabs;

    public AddressBookBrowser(int noOfTabs, ObservableList<Person> filteredPersons) {
        this.noOfTabs = noOfTabs;
        addressBookBrowserView = new TabPane();
        this.filteredPersons = filteredPersons;
        this.browserTabs = new ArrayList<>();
        for (int i = 0; i < noOfTabs; i++) {
            browserTabs.add(new BrowserTab());
            Tab tab = createBrowserTabView(browserTabs.get(i));
            addressBookBrowserView.getTabs().add(tab);
        }
        hideTabs();
        loadInstruction();
    }

    private void hideTabs() {
        addressBookBrowserView.setVisible(false);
        addressBookBrowserView.setTabMaxHeight(0);
    }

    private ArrayList<BrowserTab> getBrowserTabs(){
        return browserTabs;
    }

    /**
     * Loads instruction page on browser
     */
    public void loadInstruction() {
        browserTabs.get(0).loadHTML(INSTRUCTION_PAGE_HTML_CODE);
    }

    /**
     * Creates the UI view of the BrowserTab.
     */
    private Tab createBrowserTabView(BrowserTab page) {
        BrowserView browserView = new BrowserView(page);
        Tab tab = new Tab();
        tab.setContent(browserView);
        return tab;
    }

    /**
     * Registers page finish loading event listener to run tasks required after a page has finished
     * loading successfully.
     */
    public void registerListeners() {
        for (BrowserTab browserTab : browserTabs) {
            browserTab.addLoadListener(new LoadAdapter() {
                @Override
                public void onFinishLoadingFrame(FinishLoadingEvent finishLoadingEvent) {
                    if (finishLoadingEvent.isMainFrame()) {
                        runPageLoadedTasks(browserTab);
                    }
                }
            });
        }
    }

    /**
     * Runs the tasks required after a page has finished loading successfully.
     * @param browserTab The browser instance that has finished loaded its page.
     */
    private void runPageLoadedTasks(BrowserTab browserTab) {
        automateClickingAndScrolling(browserTab);
    }

    /**
     * Returns the UI view of the AddressBookBrowser.
     */
    public TabPane getAddressBookBrowserView(){
        return addressBookBrowserView;
    }

    /**
     * Dispose the resources used by this browser. Once called, the browser is no longer usable.
     */
    public void dispose(){
        browserTabs.forEach(Browser::dispose);
    }

    /**
     * Loads the person's profile page to the browser.
     * It also performs caching of other person profile page.
     * PreCondition: filteredModelPersons().size() >= 1
     */
    public void loadProfilePage(Person person) {

        int indexOfPersonInListOfContacts = filteredPersons.indexOf(person);

        ArrayList<Person> listOfPersonsToBeLoaded = getListOfPersonsToBeLoaded(filteredPersons,
                                                                               indexOfPersonInListOfContacts);
        unloadProfilePages(listOfPersonsToBeLoaded);
        ArrayList<Person> listOfAlreadyLoadedPerson = getListOfAlreadyLoadedPerson(listOfPersonsToBeLoaded);
        listOfPersonsToBeLoaded.removeAll(listOfAlreadyLoadedPerson);
        loadProfilePages(listOfPersonsToBeLoaded);
        selectTabAssignedForPerson(person);
    }

    /**
     * Gets a list of Person objects that are already loaded in the browser cache.
     * @param listOfPersonsToBeLoaded A list of person whose profile pages are to be loaded to the browser.
     * @return a list of Person objects that are already loaded in the browser cache.
     */
    private ArrayList<Person> getListOfAlreadyLoadedPerson(List<Person> listOfPersonsToBeLoaded) {

        ArrayList<Person> listOfAlreadyLoadedPerson = new ArrayList<>();
        browserTabs.stream().forEach(browserTab -> {
                if (browserTab.getPerson() != null && listOfPersonsToBeLoaded.contains(browserTab.getPerson())) {
                    listOfAlreadyLoadedPerson.add(browserTab.getPerson());
                }
            });
        return listOfAlreadyLoadedPerson;
    }

    /**
     * Unloads profile pages that are unneeded from the cache of the browser.
     * @param listOfPersonsToLoad A list of person that are to be <b>loaded</b> to the browser cache.
     */
    private void unloadProfilePages(List<Person> listOfPersonsToLoad) {

        ArrayList<BrowserTab> tabsToBeUnLoaded = (ArrayList<BrowserTab>) browserTabs
                .stream()
                .filter(browserTab -> browserTab.getPerson() != null &&
                                      !listOfPersonsToLoad.contains(browserTab.getPerson()))
                .collect(Collectors.toList());
        tabsToBeUnLoaded.stream().forEach(BrowserTab::unloadProfilePage);

    }

    /**
     * Unloads profile page that are unneeded from the cache of the browser.
     * @param person The person to be unloaded from the browser cache
     */
    public void unloadProfilePage(Person person) {
        Optional<BrowserTab> tab = browserTabs.stream().filter(browserTab -> browserTab.getPerson() != null &&
                                                               person.equals(browserTab.getPerson())).findAny();
        tab.ifPresent(BrowserTab::unloadProfilePage);
    }

    /**
     * Selects the tab that is used to load the profile page of the person.
     */
    private void selectTabAssignedForPerson(Person person) {
        browserTabs.stream()
                   .filter(browserTab -> person.equals(browserTab.getPerson()))
                   .forEach(browserTab -> selectTab(browserTabs.indexOf(browserTab)));
    }

    /**
     * Loads profile pages into the browser.
     */
    private void loadProfilePages(ArrayList<Person> listOfPersonsToBeLoaded) {

        ArrayList<BrowserTab> notAssignedTabs = (ArrayList<BrowserTab>) browserTabs.stream()
                                                                                   .filter(page -> !page.isAssigned())
                                                                                   .collect(Collectors.toList());

        if (notAssignedTabs.size() == 0 && listOfPersonsToBeLoaded.size() == 1){
            browserTabs.get(0).loadProfilePage(listOfPersonsToBeLoaded.remove(0));
            return;
        }
        listOfPersonsToBeLoaded.stream().forEach(person -> notAssignedTabs.remove(0).loadProfilePage(person));
    }

     /**
     * Gets a list of person that are needed to be loaded to the browser.
     */
    private ArrayList<Person> getListOfPersonsToBeLoaded(List<Person> filteredPersons, int indexOfPerson) {
        ArrayList<Person> listOfPersonsToBeLoaded = new ArrayList<>();

        for (int i = 0; i < noOfTabs && i < filteredPersons.size(); i++){
            listOfPersonsToBeLoaded.add(new Person(filteredPersons.get((indexOfPerson + i) % filteredPersons.size())));
        }
        return listOfPersonsToBeLoaded;
    }

    /**
     * Automates clicking on the Repositories tab and scrolling to the bottom of the page.
     */
    private static void automateClickingAndScrolling(Browser browser) {
        DOMElement repoContainer = browser.getDocument().findElement(By.id("js-pjax-container"));
        DOMElement repoLink = browser.getDocument().findElement(By.className("octicon octicon-repo"));

        DOMElement userRepoList = browser.getDocument().findElement(By.className("repo-list js-repo-list"));
        DOMElement organizationRepoList = browser.getDocument().findElement(By.id("org-repositories"));

        if (isRepoElementExist(userRepoList, organizationRepoList)) {
            browser.executeCommand(EditorCommand.SCROLL_TO_END_OF_DOCUMENT);
            return;
        }

        if (isElementFoundToNavigateToRepoPage(repoContainer, repoLink)) {
            repoContainer.addEventListener(DOMEventType.OnLoad, e ->
                            browser.executeCommand(EditorCommand.SCROLL_TO_END_OF_DOCUMENT), true);
            repoLink.click();
        }
    }

    private static boolean isRepoElementExist(DOMElement userRepoList, DOMElement organizationRepoList) {
        return userRepoList != null || organizationRepoList != null;
    }

    private static boolean isElementFoundToNavigateToRepoPage(DOMElement container, DOMElement link) {
        return link != null && container != null;
    }

    /**
     * Selects the tab of the browser.
     */
    private void selectTab(int indexOfTab){
        addressBookBrowserView.getSelectionModel().select(indexOfTab);
    }

    /**
     * Gets the current list of person that are displayed and stored in the browser.
     * @return a current list of person that are displayed and stored in the browser.
     */
    public List<Person> getPersonsLoadedInCache(){
        List<Person> list = new ArrayList<>();
        List<BrowserTab> filteredBrowserTabs = browserTabs.stream().filter(browserTab ->
                                                        browserTab.getPerson() != null).collect(Collectors.toList());
        filteredBrowserTabs.stream().forEach(browserTab -> list.add(browserTab.getPerson()));
        return list;
    }

}
