package address.browser;

import address.events.EventManager;
import address.events.LocalModelChangedEvent;
import address.model.ModelManager;
import address.model.ModelPerson;
import address.model.Person;

import com.google.common.eventbus.Subscribe;
import javafx.collections.ObservableList;
import javafx.scene.control.TabPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages the browser.
 */
public class BrowserManager {


    public static final int NUMBER_OF_PRELOADED_PAGE = 3;
    public static final int PERSON_NOT_FOUND = -1;

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
        EventManager.getInstance().registerHandler(this);
    }

    @Subscribe
    public void handleLocalModelChangedEvent(LocalModelChangedEvent event){
        ArrayList<BrowserTab> browserTabs = browser.get().getBrowserTabs();

        for (BrowserTab browserTab: browserTabs){
            List<Person> listOfContactsDisplayed = ModelManager.convertToPersons(filteredModelPersons);
            Optional<Person> browserTabPerson = Optional.ofNullable(browserTab.getPerson());
            if (!browserTabPerson.isPresent()){
                continue;
            }

            int indexOfContact = listOfContactsDisplayed.indexOf(browserTabPerson.get());

            if(indexOfContact == PERSON_NOT_FOUND){
                browserTab.unloadProfilePage();
                continue;
            }

            Person updatedPerson = listOfContactsDisplayed.get(indexOfContact);

            if (!updatedPerson.getGithubUserName().equals(browserTabPerson.get().getGithubUserName())){
                browserTab.loadProfilePage(updatedPerson);
            }
        }
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
