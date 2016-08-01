package address.browser;

import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.util.AppLogger;
import address.util.LoggerManager;
import commons.UrlUtil;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.web.WebView;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Manages the AddressBook browser.
 * To begin using this class: call start() once.
 */
public class BrowserManager {

    private static final String GITHUB_ROOT_URL = "https://github.com/";
    private static final String INVALID_GITHUB_USERNAME_MESSAGE = "Unparsable GitHub Username.";
    private static AppLogger logger = LoggerManager.getLogger(BrowserManager.class);
    private ObservableList<ReadOnlyViewablePerson> filteredPersons;
    private WebView browser;
    private StringProperty selectedPersonUsername;
    private ChangeListener<String> listener = (observable,  oldValue,  newValue) -> {
        try {
            URL url = new URL(GITHUB_ROOT_URL + newValue);
            if (!UrlUtil.compareBaseUrls(new URL(browser.getEngine().getLocation()), url)) {
                browser.getEngine().load(url.toExternalForm());
            }
        } catch (MalformedURLException e) {
            logger.warn("Malformed URL obtained, not attempting to load.");
            if (!newValue.equals("")) {
                browser.getEngine().loadContent(INVALID_GITHUB_USERNAME_MESSAGE);
            }
        }
    };

    public BrowserManager(ObservableList<ReadOnlyViewablePerson> filteredPersons) {
        this.selectedPersonUsername = new SimpleStringProperty();
        this.filteredPersons = filteredPersons;
    }

    /**
     * Starts the browser manager.
     */
    public void start() {
        logger.info("Initializing browser");
        browser = new WebView();
    }

    /**
     * Loads the person's profile page to the browser.
     * PreCondition: filteredModelPersons.size() >= 1
     */
    public synchronized void loadProfilePage(ReadOnlyViewablePerson person) {

        selectedPersonUsername.removeListener(listener);

        browser.getEngine().load(person.profilePageUrl().toExternalForm());

        selectedPersonUsername.unbind();
        selectedPersonUsername.bind(person.githubUsernameProperty());
        selectedPersonUsername.addListener(listener);
    }

    /**
     * Frees resources allocated to the browser.
     */
    public void freeBrowserResources() {
        browser = null;
    }

    public Node getHyperBrowserView() {
        return browser;
    }
}
