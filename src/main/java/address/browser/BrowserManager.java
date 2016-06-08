package address.browser;

import address.browser.page.GithubProfilePage;
import address.events.EventManager;
import address.events.LocalModelChangedEvent;
import address.model.datatypes.Person;

import com.google.common.eventbus.Subscribe;

import com.teamdev.jxbrowser.chromium.BrowserCore;
import com.teamdev.jxbrowser.chromium.LoggerProvider;
import com.teamdev.jxbrowser.chromium.internal.Environment;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

import java.util.*;
import java.util.logging.Level;

/**
 * Manages the AddressBook browser.
 */
public class BrowserManager {

    public static final int NUMBER_OF_PRELOADED_PAGE = 3;
    public static final int PERSON_NOT_FOUND = -1;

    private ObservableList<Person> filteredPersons;

    public Optional<AddressBookPagePool> addressBookPagePool;

    private AnchorPane browserPlaceHolder;
    private Node browserDefaultScreen;

    public BrowserManager(ObservableList<Person> filteredPersons, AnchorPane browserPlaceHolder,
                          Node browserDefaultScreen) {
        this.browserPlaceHolder = browserPlaceHolder;
        this.browserDefaultScreen = browserDefaultScreen;
        this.filteredPersons = filteredPersons;
        String headlessProperty = System.getProperty("testfx.headless");
        if (headlessProperty != null && headlessProperty.equals("true")) {
            addressBookPagePool = Optional.empty();
            return;
        }
        EventManager.getInstance().registerHandler(this);
        addressBookPagePool = Optional.of(new AddressBookPagePool(NUMBER_OF_PRELOADED_PAGE));
    }

    @Subscribe
    public void handleLocalModelChangedEvent(LocalModelChangedEvent event){

        if (!addressBookPagePool.isPresent()) {
            return;
        }
        updateBrowserContent();
    }

    /**
     * Updates the browser contents.
     */
    private synchronized void updateBrowserContent() {
        ArrayList<Person> pagesPerson = addressBookPagePool.get().getActivePagesPerson();
        pagesPerson.stream().forEach(person -> {
                if (filteredPersons.indexOf(person) == PERSON_NOT_FOUND){
                    Optional<GithubProfilePage> page = addressBookPagePool.get().clearPageNotRequired(person);
                    browserPlaceHolder.getChildren().remove(page.get().getBrowser().getBrowserView());

                    if (browserPlaceHolder.getChildren().size() == 0) {
                        browserPlaceHolder.getChildren().add(browserDefaultScreen);
                    }
                } else {
                    int indexOfContact = filteredPersons.indexOf(person);
                    Person updatedPerson = filteredPersons.get(indexOfContact);

                    if (!updatedPerson.getGithubUserName().equals(person.getGithubUserName())){
                        addressBookPagePool.get().clearPageNotRequired(person);
                        EmbeddedBrowser browser = addressBookPagePool.get().loadPersonPage(updatedPerson);
                        replaceBrowserView(browser.getBrowserView());
                    }
                }
            });
    }

    public static void initializeBrowser() {
        if (Environment.isMac()) {
            BrowserCore.initialize();
        }
        LoggerProvider.setLevel(Level.SEVERE);
    }

    /**
     * Loads the person's profile page to the browser.
     * PreCondition: filteredModelPersons.size() >= 1
     */
    public synchronized void loadProfilePage(Person person) {
        if (!addressBookPagePool.isPresent()) return;

        int indexOfPersonInListOfContacts = filteredPersons.indexOf(person);

        ArrayList<Person> listOfRequiredPerson = getListOfRequiredPerson(filteredPersons,
                                                                         indexOfPersonInListOfContacts);
        addressBookPagePool.get().clearPagesNotRequired(listOfRequiredPerson);

        EmbeddedBrowser browserView = addressBookPagePool.get().loadPersonPage(person);

        replaceBrowserView(browserView.getBrowserView());
        listOfRequiredPerson.remove(person); //person has already been loaded.
        preloadAdditionalPersonProfile(listOfRequiredPerson);
    }

    /**
     * Pre-loads a list of person's profile page into the pool of pages.
     * @param listOfPerson The list of person whose profile pages are to be preloaded to the pool of browsers.
     */
    private void preloadAdditionalPersonProfile(ArrayList<Person> listOfPerson) {
        listOfPerson.stream().forEach(p -> addressBookPagePool.get().loadPersonPage(p));
    }

    private void replaceBrowserView(Node browserView) {
        if (browserPlaceHolder.getChildren().size() >= 1){
            browserPlaceHolder.getChildren().removeAll(browserPlaceHolder.getChildren());
        }
        browserPlaceHolder.getChildren().add(browserView);
    }

    /**
     * Gets a list of person that are needed to be loaded to the browser.
     */
    private ArrayList<Person> getListOfRequiredPerson(List<Person> filteredPersons, int indexOfPerson) {
        ArrayList<Person> listOfPersonToBeLoaded = new ArrayList<>();

        for (int i = 0; i < NUMBER_OF_PRELOADED_PAGE && i < filteredPersons.size(); i++){
            listOfPersonToBeLoaded.add(new Person(filteredPersons.get((indexOfPerson + i) % filteredPersons.size())));
        }
        return listOfPersonToBeLoaded;
    }

    /**
     * Frees resources allocated to the browser.
     */
    public void freeBrowserResources() {
        if (!addressBookPagePool.isPresent()) return;
        addressBookPagePool.get().dispose();
    }

}
