package address.browser;

import address.events.EventManager;
import address.events.LocalModelChangedEvent;
import address.exceptions.IllegalArgumentSizeException;
import address.model.datatypes.Person;

import address.util.UrlUtil;
import com.google.common.eventbus.Subscribe;

import com.teamdev.jxbrowser.chromium.BrowserCore;
import com.teamdev.jxbrowser.chromium.LoggerProvider;
import com.teamdev.jxbrowser.chromium.internal.Environment;

import javafx.collections.ObservableList;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Manages the AddressBook browser.
 */
public class BrowserManager {

    public static final int NUMBER_OF_PRELOADED_PAGE = 3;
    public static final int PERSON_NOT_FOUND = -1;

    private ObservableList<Person> filteredPersons;

    public Optional<HyperBrowser> hyperBrowser;

    public BrowserManager(ObservableList<Person> filteredPersons) {
        this.filteredPersons = filteredPersons;
        String headlessProperty = System.getProperty("testfx.headless");
        if (headlessProperty != null && headlessProperty.equals("true")) {
            hyperBrowser = Optional.empty();
            return;
        }
        EventManager.getInstance().registerHandler(this);
        hyperBrowser = Optional.of(new HyperBrowser(NUMBER_OF_PRELOADED_PAGE));
    }

    @Subscribe
    public void handleLocalModelChangedEvent(LocalModelChangedEvent event){

        if (!hyperBrowser.isPresent()) {
            return;
        }
        updateBrowserContent();
    }

    /**
     * Updates the browser contents.
     */
    private synchronized void updateBrowserContent() {
        ArrayList<URL> pagesPerson = hyperBrowser.get().getActivePagesUrl();
        pagesPerson.stream().forEach(personUrl -> {
                Optional<Person> personFound = filteredPersons.stream().filter(person
                        -> UrlUtil.compareBaseUrls(person.profilePageUrl(), personUrl)).findAny();

                if (!personFound.isPresent()){
                    hyperBrowser.get().clearPage(personUrl);
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
        if (!hyperBrowser.isPresent()) return;

        int indexOfPersonInListOfContacts = filteredPersons.indexOf(person);

        ArrayList<Person> listOfPersonToLoadInFuture = getListOfPersonToLoadInFuture(filteredPersons,
                                                                         indexOfPersonInListOfContacts);
        try {
            ArrayList<URL> listOfFutureUrl = listOfPersonToLoadInFuture.stream()
                                                                     .map(p -> p.profilePageUrl())
                                                                     .collect(Collectors.toCollection(ArrayList::new));
            hyperBrowser.get().loadPersonPage(person.profilePageUrl(), listOfFutureUrl);
        } catch (IllegalArgumentSizeException e) {
            e.printStackTrace();
            //Will never go into here if preconditions of loadPersonPage is fulfilled.
        }
    }

    /**
     * Pre-loads a list of person's profile page into the pool of pages.
     * @param listOfPerson The list of person whose profile pages are to be preloaded to the pool of browsers.
     */
    /*
    private void preloadAdditionalPersonProfile(ArrayList<Person> listOfPerson) {
        listOfPerson.stream().forEach(p -> hyperBrowser.get().loadPersonPage(p));
    }
    */

    /**
     * Gets a list of person that are needed to be loaded to the browser in future.
     */
    private ArrayList<Person> getListOfPersonToLoadInFuture(List<Person> filteredPersons, int indexOfPerson) {
        ArrayList<Person> listOfRequiredPerson = new ArrayList<>();

        for (int i = 1; i < NUMBER_OF_PRELOADED_PAGE && i < filteredPersons.size(); i++){
            listOfRequiredPerson.add(new Person(filteredPersons.get((indexOfPerson + i) % filteredPersons.size())));
        }
        return listOfRequiredPerson;
    }

    /**
     * Frees resources allocated to the browser.
     */
    public void freeBrowserResources() {
        if (!hyperBrowser.isPresent()) return;
        hyperBrowser.get().dispose();
    }

    public AnchorPane getHyperBrowserView(){
        if (!hyperBrowser.isPresent()){
            return new AnchorPane();
        }
        return hyperBrowser.get().getHyperBrowserView();
    }

}
