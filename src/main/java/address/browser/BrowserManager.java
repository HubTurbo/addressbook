package address.browser;

import address.model.ModelPerson;
import address.model.Person;

import javafx.collections.ObservableList;
import javafx.scene.control.TabPane;

/**
 * Manages the browser.
 */
public class BrowserManager {


    public static final int NUMBER_OF_PRELOADED_PAGE = 3;

    private AddressBookBrowser browser;

    private ObservableList<ModelPerson> filteredModelPersons;

    public BrowserManager(ObservableList<ModelPerson> filteredModelPersons) {
        this.filteredModelPersons = filteredModelPersons;
        browser = new AddressBookBrowser(NUMBER_OF_PRELOADED_PAGE, this.filteredModelPersons);
        browser.registerListeners();
    }

    /**
     * Loads the person's profile page to the browser.
     * PreCondition: filteredModelPersons.size() >= 1
     * @param person
     */
    public void loadProfilePage(Person person){
        browser.loadProfilePage(person);
    }

    /**
     * Returns the UI view of the browser.
     * @return
     */
    public TabPane getBrowserView() {
        return browser.getAddressBookBrowserView();
    }

    /**
     * Frees resources allocated to the browser.
     */
    public void freeBrowserResources() {
        browser.dispose();
    }
}
