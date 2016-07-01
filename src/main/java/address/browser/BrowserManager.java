package address.browser;

import address.MainApp;
import address.browser.page.GithubProfilePage;

import address.model.datatypes.person.ReadOnlyPerson;

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
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Manages the AddressBook browser.
 * To begin using this class: call start() once.
 */
public class BrowserManager {

    private final BrowserType browserType;

    private static final String FXML_BROWSER_PLACE_HOLDER_SCREEN = "/view/DefaultBrowserPlaceHolderScreen.fxml";

    private static final String GITHUB_ROOT_URL = "https://github.com/";
    private static final String INVALID_GITHUB_USERNAME_MESSAGE = "Unparsable GitHub Username.";

    private static AppLogger logger = LoggerManager.getLogger(BrowserManager.class);

    private ObservableList<ReadOnlyViewablePerson> filteredPersons;

    private Optional<HyperBrowser> hyperBrowser;

    private StringProperty selectedPersonUsername;

    private final int browserNoOfPages;

    private ChangeListener<String> listener = (observable,  oldValue,  newValue) -> {
        try {
            URL url = new URL(GITHUB_ROOT_URL + newValue);
            if (!UrlUtil.compareBaseUrls(hyperBrowser.get().getDisplayedUrl(), url)) {
                List<Page> pages = hyperBrowser.get().loadUrl(url);
                configureGithubPageTasks(pages);
            }
        } catch (MalformedURLException e) {
            logger.warn("Malformed URL obtained, not attempting to load.");
            if (!newValue.equals("")) {
                hyperBrowser.get().loadHTML(INVALID_GITHUB_USERNAME_MESSAGE);
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

    /**
     * Initialize the browser managed by the browser manager.
     * This must be called in a non-ui thread.
     */
    public void initBrowser(){
        if (browserType == BrowserType.FULL_FEATURE_BROWSER) {
            if (Environment.isMac()) {
                BrowserCore.initialize();
            }
            logger.debug("Suppressing browser logs");
            LoggerProvider.setLevel(Level.SEVERE);
        }
    }

    /**
     * Starts the browser manager.
     */
    public void start() {
        String headlessProperty = System.getProperty("testfx.headless");
        if (headlessProperty != null && headlessProperty.equals("true")) {
            logger.info("Headless mode detected, not initializing HyperBrowser.");
            hyperBrowser = Optional.empty();
        } else {
            logger.info("Initializing browser with {} pages", browserNoOfPages);
            hyperBrowser = Optional.of(new HyperBrowser(
                    browserType,
                    browserNoOfPages,
                    getBrowserInitialScreen()));
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

        List<URL> listOfFutureUrl =
                UrlUtil.getFutureUrls(filteredPersons.stream()
                                                         .map(ReadOnlyPerson::profilePageUrl)
                                                         .collect(Collectors.toCollection(ArrayList::new)),
                                                                  indexOfPersonInListOfContacts,
                                                                  browserNoOfPages - 1);
        try {
            List<Page> pages = hyperBrowser.get().loadUrls(person.profilePageUrl(), listOfFutureUrl);
            configureGithubPageTasks(pages);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            assert false : "Preconditions of loadUrls is not fulfilled.";
        }

        selectedPersonUsername.unbind();
        selectedPersonUsername.bind(person.githubUsernameProperty());
        selectedPersonUsername.addListener(listener);
    }

    private void configureGithubPageTasks(List<Page> pages) {
        pages.stream().map(GithubProfilePage::new).forEach(GithubProfilePage::setupPageAutomation);
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

    private static Optional<Node> getBrowserInitialScreen(){
        String fxmlResourcePath = FXML_BROWSER_PLACE_HOLDER_SCREEN;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            return Optional.ofNullable(loader.load());
        } catch (IOException e){
            return Optional.empty();
        }
    }
}
