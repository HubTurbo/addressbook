package address.unittests.browser;

import address.browser.BrowserManager;
import address.browser.HyperBrowser;
import address.browser.page.Page;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.model.datatypes.person.ViewablePerson;
import address.util.UrlUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Test;


import java.net.MalformedURLException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the BrowserManager behaviours and functionality.
 */
public class BrowserManagerTest {

    private ObservableList<ReadOnlyViewablePerson> filteredPersons;

    public BrowserManagerTest() {
        this.filteredPersons = FXCollections.observableArrayList();
        Person person;
        person = new Person("John", "Smith");
        person.setGithubUserName("1");
        this.filteredPersons.add(new ViewablePerson(new Person(person)));
        person = new Person("John", "Peter");
        person.setGithubUserName("2");
        this.filteredPersons.add(new ViewablePerson(new Person(person)));
        person = new Person("Obama", "Smith");
        this.filteredPersons.add(new ViewablePerson(new Person(person)));
        person = new Person("Lala", "Lol");
        this.filteredPersons.add(new ViewablePerson(new Person(person)));
        person = new Person("Hehe", "Lala");
        this.filteredPersons.add(new ViewablePerson(person));
    }

    @Test
    public void testNecessaryBrowserResources_resourcesNotNull() {
        BrowserManager manager = new BrowserManager(filteredPersons);
        assertNotNull(manager.getHyperBrowserView());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLoadProfilePage_loadMultipleUrls_urlsLoaded() throws NoSuchFieldException, IllegalAccessException, MalformedURLException {
        BrowserManager manager = new BrowserManager(filteredPersons);
        manager.loadProfilePage(filteredPersons.get(0));
        Optional<HyperBrowser> browser = (Optional<HyperBrowser>) manager.getClass().getDeclaredField("hyperBrowser")
                                                                         .get(manager);

        List<Page> pages = (List<Page>) browser.get().getClass().getDeclaredField("pages").get(browser.get());
        assertTrue(UrlUtil.compareBaseUrls(pages.get(0).getBrowser().getUrl(), filteredPersons.get(0).profilePageUrl()));
        assertTrue(UrlUtil.compareBaseUrls(pages.get(1).getBrowser().getUrl(), filteredPersons.get(1).profilePageUrl()));
        assertTrue(UrlUtil.compareBaseUrls(pages.get(2).getBrowser().getUrl(), filteredPersons.get(2).profilePageUrl()));
    }

}
