package address.browser;

import address.browser.page.GithubProfilePage;
import hubturbo.embeddedbrowser.BrowserType;
import hubturbo.embeddedbrowser.HyperBrowser;
import hubturbo.embeddedbrowser.page.Page;

import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.util.AppLogger;
import address.util.LoggerManager;
import address.util.UrlUtil;

import com.teamdev.jxbrowser.chromium.BrowserCore;
import com.teamdev.jxbrowser.chromium.LoggerProvider;
import com.teamdev.jxbrowser.chromium.internal.Environment;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.layout.AnchorPane;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Manages the AddressBook browser.
 */
public class BrowserManager {

    private static final String GITHUB_ROOT_URL = "https://github.com/";
    private static final String INVALID_GITHUB_USERNAME_MESSAGE = "Unparsable GitHub Username.";

    private static AppLogger logger = LoggerManager.getLogger(BrowserManager.class);

    private ObservableList<ReadOnlyViewablePerson> filteredPersons;

    private Optional<HyperBrowser> hyperBrowser;

    private StringProperty selectedPersonUsername;

    private ChangeListener<String> listener = (observable,  oldValue,  newValue) -> {
        try {
            URL url = new URL(GITHUB_ROOT_URL + newValue);
            if (!UrlUtil.compareBaseUrls(hyperBrowser.get().getDisplayedUrl(), url)) {
                hyperBrowser.get().loadUrl(url);
            }
        } catch (MalformedURLException e) {
            logger.warn("Malformed URL obtained, not attempting to load.");
            if (!newValue.equals("")) {
                hyperBrowser.get().loadHTML(INVALID_GITHUB_USERNAME_MESSAGE);
            }
        }
    };

    public BrowserManager(ObservableList<ReadOnlyViewablePerson> filteredPersons) {
        initializeBrowser();
        this.selectedPersonUsername = new SimpleStringProperty();
        this.filteredPersons = filteredPersons;
    }

    /**
     * Initialize the application to use jxBrowser.
     * This must be called in a non-ui thread.
     */
    public static void initializeJxBrowserEnvironment(){
        if (Environment.isMac()) {
            BrowserCore.initialize();
        }
        logger.debug("Suppressing browser logs");
        LoggerProvider.setLevel(Level.SEVERE);
    }

    public void initializeBrowser() {
        String headlessProperty = System.getProperty("testfx.headless");
        if (headlessProperty != null && headlessProperty.equals("true")) {
            logger.info("Headless mode detected, not initializing HyperBrowser.");
            hyperBrowser = Optional.empty();
        } else {
            logger.info("Initializing browser with {} pages", HyperBrowser.RECOMMENDED_NUMBER_OF_PAGES);
            hyperBrowser = Optional.of(new HyperBrowser(
                    BrowserType.FULL_FEATURE_BROWSER,
                    HyperBrowser.RECOMMENDED_NUMBER_OF_PAGES,
                    BrowserManagerUtil.getBrowserInitialScreen()));
        }
    }

    /**
     * Loads the person's profile page to the browser.
     * PreCondition: filteredModelPersons.size() >= 1
     */
    public synchronized void loadProfilePage(ReadOnlyViewablePerson person) {
        if (!hyperBrowser.isPresent()) return;

        selectedPersonUsername.removeListener(listener);
        
        int indexOfPersonInListOfContacts = filteredPersons.indexOf(person);

        ArrayList<ReadOnlyViewablePerson> listOfPersonToLoadInFuture =
                BrowserManagerUtil.getListOfPersonToLoadInFuture(filteredPersons, indexOfPersonInListOfContacts);
        ArrayList<URL> listOfFutureUrl = listOfPersonToLoadInFuture.stream()
                                                                    .map(ReadOnlyViewablePerson::profilePageUrl)
                                                                    .collect(Collectors.toCollection(ArrayList::new));
        try {
            Page page = hyperBrowser.get().loadUrls(person.profilePageUrl(), listOfFutureUrl);
            GithubProfilePage gPage = new GithubProfilePage(page);
            gPage.setPageLoadFinishListener(b -> Platform.runLater(() -> gPage.executePageLoadedTasks()));

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
        if (!hyperBrowser.isPresent()) return;
        hyperBrowser.get().dispose();
    }

    public AnchorPane getHyperBrowserView() {
        if (!hyperBrowser.isPresent()) {
            return new AnchorPane();
        }
        return hyperBrowser.get().getHyperBrowserView();
    }
}
