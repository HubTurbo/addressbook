package address.browser;

import address.events.EventManager;
import address.events.LocalModelChangedEvent;
import address.model.Person;

import com.google.common.eventbus.Subscribe;

import com.teamdev.jxbrowser.chromium.BrowserCore;
import com.teamdev.jxbrowser.chromium.LoggerProvider;
import com.teamdev.jxbrowser.chromium.internal.Environment;

import javafx.collections.ObservableList;
import javafx.scene.control.TabPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Manages the browser.
 */
public class BrowserManager {


    public static final int NUMBER_OF_PRELOADED_PAGE = 3;
    public static final int PERSON_NOT_FOUND = -1;

    private Optional<AddressBookBrowser> browser;

    private ObservableList<Person> filteredPersons;

    public BrowserManager(ObservableList<Person> filteredPersons) {
        this.filteredPersons = filteredPersons;
        String headlessProperty = System.getProperty("testfx.headless");
        if (headlessProperty != null && headlessProperty.equals("true")) {
            browser = Optional.empty();
            return;
        }
        browser = Optional.of(new AddressBookBrowser(NUMBER_OF_PRELOADED_PAGE, this.filteredPersons));
        browser.get().registerListeners();
        EventManager.getInstance().registerHandler(this);
    }

    @Subscribe
    public void handleLocalModelChangedEvent(LocalModelChangedEvent event){
        ArrayList<BrowserTab> browserTabs = browser.get().getBrowserTabs();

        for (BrowserTab browserTab: browserTabs){
            Optional<Person> browserTabPerson = Optional.ofNullable(browserTab.getPerson());
            if (!browserTabPerson.isPresent()){
                continue;
            }

            int indexOfContact = filteredPersons.indexOf(browserTabPerson.get());

            if (indexOfContact == PERSON_NOT_FOUND){
                browserTab.unloadProfilePage();
                continue;
            }

            Person updatedPerson = filteredPersons.get(indexOfContact);

            if (!updatedPerson.getGithubUserName().equals(browserTabPerson.get().getGithubUserName())){
                browserTab.loadProfilePage(updatedPerson);
            }
        }
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
    public void loadProfilePage(Person person){
        if (!browser.isPresent()) return;
        browser.get().loadProfilePage(person);
    }

    /**
     * Returns the UI view of the browser.
     */
    public Optional<TabPane> getBrowserView() {
        if (!browser.isPresent()) return Optional.empty();
        return Optional.ofNullable(browser.get().getAddressBookBrowserView());
    }

    /**
     * Frees resources allocated to the browser.
     */
    public void freeBrowserResources() {
        if (!browser.isPresent()) return;
        browser.get().dispose();
    }

    public Optional<AddressBookBrowser> getBrowser() {
        return browser;
    }
}
