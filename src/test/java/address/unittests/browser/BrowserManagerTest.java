package address.unittests.browser;

import address.browser.BrowserManager;
import address.browser.BrowserManagerUtil;
import address.model.datatypes.person.Person;
import address.model.datatypes.person.ReadOnlyViewablePerson;
import address.model.datatypes.person.ViewablePerson;
import address.util.JavafxThreadingRule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import org.junit.Rule;
import org.junit.Test;

import java.net.URL;
import java.util.List;
import java.util.Optional;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the BrowserManager behaviours and functionality.
 */
public class BrowserManagerTest {

    @Rule
    /**
     * To run test cases on JavaFX thread.
     */
    public JavafxThreadingRule javafxRule = new JavafxThreadingRule();

    private ObservableList<ReadOnlyViewablePerson> filteredPersons;

    public BrowserManagerTest() {
        this.filteredPersons = FXCollections.observableArrayList();
        this.filteredPersons.add(ViewablePerson.fromBacking(createPersonWithGithubUsername("John", "Smith", -1, "1")));
        this.filteredPersons.add(ViewablePerson.fromBacking(createPersonWithGithubUsername("John", "Peter", -2, "2")));
        this.filteredPersons.add(ViewablePerson.fromBacking(createPersonWithGithubUsername("Obama", "Smith", -3, "3")));
        this.filteredPersons.add(ViewablePerson.fromBacking(createPersonWithGithubUsername("Lala", "Lol", -4, "4")));
        this.filteredPersons.add(ViewablePerson.fromBacking(createPersonWithGithubUsername("Hehe", "Lala", -5, "5")));
        this.filteredPersons.add(ViewablePerson.fromBacking(new Person("Hehe", "Lala1", -6)));
        this.filteredPersons.add(ViewablePerson.fromBacking(new Person("Hehe", "Lala2", -7)));
        this.filteredPersons.add(ViewablePerson.fromBacking(new Person("Hehe", "Lala3", -8)));
        this.filteredPersons.add(ViewablePerson.fromBacking(new Person("Hehe", "Lalaa", -9)));
        this.filteredPersons.add(ViewablePerson.fromBacking(createPersonWithGithubUsername("Hehe", "Lala4", -10, "a")));
        this.filteredPersons.add(ViewablePerson.fromBacking(new Person("Hehe", "Lala5", -11)));
        this.filteredPersons.add(ViewablePerson.fromBacking(new Person("Hehe", "Lala6", -12)));
        this.filteredPersons.add(ViewablePerson.fromBacking(new Person("Hehe", "Lala7", -12)));

    }

    private Person createPersonWithGithubUsername(String john, String smith, int id, String username) {
        Person person = new Person(john, smith, id);
        person.setGithubUsername(username);
        return person;
    }

    @Test
    public void testNecessaryBrowserResources_resourcesNotNull() {
        BrowserManager manager = new BrowserManager(filteredPersons);
        manager.initializeBrowser();
        assertNotNull(manager.getHyperBrowserView());
        Optional<Node> node = BrowserManagerUtil.getBrowserInitialScreen();
        assertTrue(node.isPresent());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetListOfPersonToLoadInFuture_listMoreThan3Person_nextTwoIndexPersonReturned() {
        List<URL> list = BrowserManagerUtil.getListOfPersonUrlToLoadInFuture(filteredPersons, 0);
        assertTrue(list.contains(filteredPersons.get(1).profilePageUrl()));
        assertTrue(list.contains(filteredPersons.get(2).profilePageUrl()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetListOfPersonToLoadInFuture_NearTheEndOfList_resultOverlappedToLowerIndex() {
        List<URL> list = BrowserManagerUtil.getListOfPersonUrlToLoadInFuture(filteredPersons.subList(0, 5), 3);
        assertTrue(list.contains(filteredPersons.get(4).profilePageUrl()));
        assertTrue(list.contains(filteredPersons.get(0).profilePageUrl()));
    }

    @Test
    public void testGetListOfPersonToLoadInFuture_listLessThan3Person_resultSizeBoundedToListSize() {
        List<URL> list = BrowserManagerUtil.getListOfPersonUrlToLoadInFuture(filteredPersons.subList(0,2), 0);
        assertTrue(list.contains(filteredPersons.get(1).profilePageUrl()));
        assertFalse(list.contains(filteredPersons.get(0).profilePageUrl()));
        assertFalse(list.contains(filteredPersons.get(2).profilePageUrl()));
        assertFalse(list.contains(filteredPersons.get(3).profilePageUrl()));
        assertFalse(list.contains(filteredPersons.get(4).profilePageUrl()));
    }

    @Test
    public void testGetListOfPersonToLoadInFuture_listOnly1Person_resultSizeBoundedToListSize() {
        List<URL> list = BrowserManagerUtil.getListOfPersonUrlToLoadInFuture(filteredPersons.subList(0,1), 0);
        assertEquals(list.size(), 0);
    }

    @Test
    public void testGetListOfPersonToLoadInFuture_listWithDuplicateUrls_ignoreDuplicateUrls() {
        List<URL> list = BrowserManagerUtil.getListOfPersonUrlToLoadInFuture(filteredPersons.subList(5,13), 0);
        assertEquals(list.size(), 1);
        assertTrue(list.contains(filteredPersons.get(9).profilePageUrl()));
        list = BrowserManagerUtil.getListOfPersonUrlToLoadInFuture(filteredPersons.subList(5,13), 4);
        assertEquals(list.size(), 1);
        assertTrue(list.contains(filteredPersons.get(8).profilePageUrl()));
    }

}
