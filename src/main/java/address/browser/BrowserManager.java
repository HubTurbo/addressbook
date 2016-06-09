package address.browser;

import address.MainApp;
import address.browser.page.GithubProfilePage;
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
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Manages the AddressBook browser.
 */
public class BrowserManager {

    private static final String FXML_BROWSER_PLACE_HOLDER_SCREEN = "/view/DefaultBrowserPlaceHolderScreen.fxml";

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
        hyperBrowser = Optional.of(new HyperBrowser(HyperBrowser.NUMBER_OF_PRELOADED_PAGE, getBrowserInitialScreen()));
    }

    @Subscribe
    public void handleLocalModelChangedEvent(LocalModelChangedEvent event){

        if (!hyperBrowser.isPresent()) {
            return;
        }
        updateBrowserContent();
    }

    private Optional<Node> getBrowserInitialScreen(){
        String fxmlResourcePath = FXML_BROWSER_PLACE_HOLDER_SCREEN;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            return Optional.ofNullable(loader.load());
        } catch (IOException e){
            return Optional.empty();
        }
    }

    /**
     * Updates the browser contents.
     */
    private synchronized void updateBrowserContent() {
        ArrayList<URL> pagesPerson = hyperBrowser.get().getCachedPagesUrl();
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
            GithubProfilePage page = (GithubProfilePage) hyperBrowser.get().loadUrls(person.profilePageUrl(),
                                                                                     listOfFutureUrl);
        } catch (IllegalArgumentSizeException e) {
            e.printStackTrace();
            assert false : "Will never go into here if preconditions of loadUrls is fulfilled.";
        }
    }

    /**
     * Gets a list of person that are needed to be loaded to the browser in future.
     */
    private ArrayList<Person> getListOfPersonToLoadInFuture(List<Person> filteredPersons, int indexOfPerson) {
        ArrayList<Person> listOfRequiredPerson = new ArrayList<>();

        for (int i = 1; i < HyperBrowser.NUMBER_OF_PRELOADED_PAGE && i < filteredPersons.size(); i++){
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
