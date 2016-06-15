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

import java.net.MalformedURLException;
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
        this.filteredPersons.add(new ViewablePerson(new Person("John", "Smith")));
        this.filteredPersons.add(new ViewablePerson(new Person("John", "Peter")));
        this.filteredPersons.add(new ViewablePerson(new Person("Obama", "Smith")));
        this.filteredPersons.add(new ViewablePerson(new Person("Lala", "Lol")));
        this.filteredPersons.add(new ViewablePerson(new Person("Hehe", "Lala")));
    }

    @Test
    public void testNecessaryBrowserResources_resourcesNotNull() {
        BrowserManager manager = new BrowserManager(filteredPersons);
        assertNotNull(manager.getHyperBrowserView());
        Optional<Node> node = BrowserManagerUtil.getBrowserInitialScreen();
        assertTrue(node.isPresent());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetListOfPersonToLoadInFuture_listMoreThan3Person_nextTwoIndexPersonReturned() throws NoSuchFieldException, IllegalAccessException,
                                                                         MalformedURLException, InterruptedException {
        List<ReadOnlyViewablePerson> list = BrowserManagerUtil.getListOfPersonToLoadInFuture(filteredPersons, 0);
        assertTrue(list.contains(filteredPersons.get(1)));
        assertTrue(list.contains(filteredPersons.get(2)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetListOfPersonToLoadInFuture_NearTheEndOfList_resultOverlappedToLowerIndex() throws NoSuchFieldException,
                                                                                                IllegalAccessException,
                                                                                                MalformedURLException,
                                                                                                InterruptedException {
        List<ReadOnlyViewablePerson> list = BrowserManagerUtil.getListOfPersonToLoadInFuture(filteredPersons, 3);
        assertTrue(list.contains(filteredPersons.get(4)));
        assertTrue(list.contains(filteredPersons.get(0)));
    }

    @Test
    public void testGetListOfPersonToLoadInFuture_listLessThan3Person_resultSizeBoundedToListSize() {
        List<ReadOnlyViewablePerson> list = BrowserManagerUtil.getListOfPersonToLoadInFuture(filteredPersons.subList(0,2), 0);
        assertTrue(list.contains(filteredPersons.get(1)));
        assertFalse(list.contains(filteredPersons.get(0)));
        assertFalse(list.contains(filteredPersons.get(2)));
        assertFalse(list.contains(filteredPersons.get(3)));
        assertFalse(list.contains(filteredPersons.get(4)));
    }

    @Test
    public void testGetListOfPersonToLoadInFuture_listOnly1Person_resultSizeBoundedToListSize() {
        List<ReadOnlyViewablePerson> list = BrowserManagerUtil.getListOfPersonToLoadInFuture(filteredPersons.subList(0,1), 0);
        assertEquals(list.size(), 0);
    }

}
