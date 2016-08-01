package address.browser;

import address.MainApp;

import address.model.datatypes.person.ReadOnlyPerson;

import hubturbo.embeddedbrowser.BrowserType;
import hubturbo.embeddedbrowser.HyperBrowser;
import hubturbo.embeddedbrowser.page.Page;

import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.util.AppLogger;
import address.util.LoggerManager;
import commons.UrlUtil;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Manages the AddressBook browser.
 * To begin using this class: call start() once.
 */
public class BrowserManager {

    private static final String FXML_BROWSER_PLACE_HOLDER_SCREEN = "/view/DefaultBrowserPlaceHolderScreen.fxml";
    private static final String GITHUB_ROOT_URL = "https://github.com/";
    private static final String INVALID_GITHUB_USERNAME_MESSAGE = "Unparsable GitHub Username.";
    private static AppLogger logger = LoggerManager.getLogger(BrowserManager.class);
    private final BrowserType browserType;
    private final int browserNoOfPages;
    private ObservableList<ReadOnlyViewablePerson> filteredPersons;
    private HyperBrowser hyperBrowser;
    private StringProperty selectedPersonUsername;
    private ChangeListener<String> listener = (observable,  oldValue,  newValue) -> {
        try {
            URL url = new URL(GITHUB_ROOT_URL + newValue);
            if (!UrlUtil.compareBaseUrls(hyperBrowser.getDisplayedUrl(), url)) {
                List<Page> pages = hyperBrowser.loadUrl(url);
            }
        } catch (MalformedURLException e) {
            logger.warn("Malformed URL obtained, not attempting to load.");
            if (!newValue.equals("")) {
                hyperBrowser.loadHTML(INVALID_GITHUB_USERNAME_MESSAGE);
            }
        }
    };

    public BrowserManager(ObservableList<ReadOnlyViewablePerson> filteredPersons, int browserNoOfPages,
                          BrowserType browserType) {
        this.selectedPersonUsername = new SimpleStringProperty();
        this.filteredPersons = filteredPersons;
        this.browserNoOfPages = browserNoOfPages;
        this.browserType = browserType;
    }

    private static Optional<Node> getBrowserInitialScreen() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(FXML_BROWSER_PLACE_HOLDER_SCREEN));
            return Optional.ofNullable(loader.load());
        } catch (IOException e){
            return Optional.empty();
        }
    }

    /**
     * Starts the browser manager.
     */
    public void start() {
        logger.info("Initializing browser with {} pages", browserNoOfPages);
        hyperBrowser = new HyperBrowser(browserType, browserNoOfPages, getBrowserInitialScreen());
    }

    /**
     * Loads the person's profile page to the browser.
     * PreCondition: filteredModelPersons.size() >= 1
     */
    public synchronized void loadProfilePage(ReadOnlyViewablePerson person) {

        selectedPersonUsername.removeListener(listener);

        int indexOfPersonInListOfContacts = filteredPersons.indexOf(person);

        List<URL> listOfFutureUrl =
                UrlUtil.getFutureUrls(filteredPersons.stream()
                                                         .map(ReadOnlyPerson::profilePageUrl)
                                                         .collect(Collectors.toCollection(ArrayList::new)),
                                                                  indexOfPersonInListOfContacts,
                                                                  browserNoOfPages - 1);
        try {
            List<Page> pages = hyperBrowser.loadUrls(person.profilePageUrl(), listOfFutureUrl);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            assert false : "Preconditions of loadUrls is not fulfilled.";
        }

        selectedPersonUsername.unbind();
        selectedPersonUsername.bind(person.githubUsernameProperty());
        selectedPersonUsername.addListener(listener);
    }

    /**
     * Frees resources allocated to the browser.
     */
    public void freeBrowserResources() {
        hyperBrowser.dispose();
    }

    public AnchorPane getHyperBrowserView() {
        return hyperBrowser.getHyperBrowserView();
    }
}
