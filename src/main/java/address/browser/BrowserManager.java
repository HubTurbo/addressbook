package address.browser;

import address.browser.page.GithubProfilePage;
import address.browser.page.Page;

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
    private static AppLogger logger = LoggerManager.getLogger(BrowserManager.class);

    private ObservableList<ReadOnlyViewablePerson> filteredPersons;

    private Optional<HyperBrowser> hyperBrowser;

    private StringProperty selectedPersonUsername;

    private ChangeListener<String> listener = (observable,  oldValue,  newValue) -> {
        try {
            URL url = new URL("https://github.com/" + newValue);
            if (!UrlUtil.compareBaseUrls(hyperBrowser.get().getDisplayedUrl(), url)) {
                hyperBrowser.get().loadUrl(url);
            }
        } catch (MalformedURLException e) {
            logger.warn("Malformed URL obtained, not attempting to load.");
            // TODO handle instead of simply logging a message
        }
    };

    public BrowserManager(ObservableList<ReadOnlyViewablePerson> filteredPersons) {
        this.selectedPersonUsername = new SimpleStringProperty();
        this.filteredPersons = filteredPersons;
        String headlessProperty = System.getProperty("testfx.headless");
        if (headlessProperty != null && headlessProperty.equals("true")) {
            logger.info("Headless mode detected, not initializing HyperBrowser.");
            hyperBrowser = Optional.empty();
        } else {
            logger.info("Initializing browser with {} pages", HyperBrowser.RECOMMENDED_NUMBER_OF_PAGES);
            hyperBrowser = Optional.of(new HyperBrowser(HyperBrowser.FULL_FEATURE_BROWSER,
                                       HyperBrowser.RECOMMENDED_NUMBER_OF_PAGES,
                                       BrowserManagerUtil.getBrowserInitialScreen()));
        }
    }

    public static void initializeBrowser() {
        if (Environment.isMac()) {
            BrowserCore.initialize();
        }
        logger.debug("Suppressing browser logs");
        LoggerProvider.setLevel(Level.SEVERE);
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
            gPage.setPageLoadFinishListener(b -> Platform.runLater(() -> gPage.automateClickingAndScrolling()));

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            assert false : "Will never go into here if preconditions of loadUrls is fulfilled.";
        }

        selectedPersonUsername.unbind();
        selectedPersonUsername.bind(person.githubUserNameProperty());
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
