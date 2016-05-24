package address.browser;

import address.model.ModelPerson;
import address.model.Person;

import javafx.collections.ObservableList;
import javafx.scene.control.TabPane;

import java.util.Optional;

/**
 * Manages the browser.
 */
public class BrowserManager {


    public static final int NUMBER_OF_PRELOADED_PAGE = 3;

    private Optional<AddressBookBrowser> browser;

    private ObservableList<ModelPerson> filteredModelPersons;

    public BrowserManager(ObservableList<ModelPerson> filteredModelPersons) {
        this.filteredModelPersons = filteredModelPersons;

        String headlessProperty = System.getProperty("testfx.headless");
        if (headlessProperty != null && headlessProperty.equals("true")) {
            browser = Optional.empty();
            return;
        }
        browser = Optional.of(new AddressBookBrowser(NUMBER_OF_PRELOADED_PAGE, this.filteredModelPersons));
        browser.get().registerListeners();
    }

    /**
     * Loads the person's profile page to the browser.
     * PreCondition: filteredModelPersons.size() >= 1
     * @param person
     */
    public void loadProfilePage(Person person){
        if (!browser.isPresent()) return;
        browser.get().loadProfilePage(person);
    }

    /**
     * Returns the UI view of the browser.
     * @return
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
}
