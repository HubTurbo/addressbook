package address.unittests;

import address.browser.AddressBookBrowser;
import address.model.Person;
import address.util.JavafxThreadingRule;
import address.util.TestUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import org.junit.Rule;
import org.junit.Test;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

/**
 * Test cases to test AddressBookBrowser
 */
public class AddressBookBrowserTest {

    @Rule
    /**
     * To run test cases on JavaFX thread.
     */
    public JavafxThreadingRule javafxRule = new JavafxThreadingRule();

    @Test
    public void testLoadProfilePage_oneTab_browserIsAssignedToLoadPerson(){
        ObservableList<Person> list = FXCollections.observableArrayList();
        list.setAll(TestUtil.generateSampleData());
        AddressBookBrowser browser = new AddressBookBrowser(1, list);

        browser.loadProfilePage(list.get(1));
        assertTrue(browser.getBrowserTabs().get(0).getPerson().equals(list.get(1)));

        browser.loadProfilePage(list.get(4));
        assertTrue(browser.getBrowserTabs().get(0).getPerson().equals(list.get(4)));
    }

    @Test
    public void testLoadProfilePage_twoTabs_browserIsAssignedToLoadPersons() {
        ObservableList<Person> list = FXCollections.observableArrayList();
        list.setAll(TestUtil.generateSampleData());
        AddressBookBrowser browser = new AddressBookBrowser(2, list);

        //Load list[i] and check if the browser is assigned to load the list[i] and list[i + 1]
        browser.loadProfilePage(list.get(3));
        assertTrue(browser.getBrowserTabs().get(0).getPerson().equals(list.get(3)));
        assertTrue(browser.getBrowserTabs().get(1).getPerson().equals(list.get(4)));
        assertEquals(browser.getAddressBookBrowserView().getSelectionModel().getSelectedIndex(), 0);

        //Person should be already loaded and browser tab will switch to index 1.
        browser.loadProfilePage(list.get(4));
        assertEquals(browser.getAddressBookBrowserView().getSelectionModel().getSelectedIndex(), 1);

    }

    @Test
    public void testLoadProfilePage_addingAndSelectingPersonsWithThreeTabs_browserShouldNotLoadMoreThanOnePerson() {
        List<Person> sampleList = TestUtil.generateSampleData();
        ObservableList<Person> list = FXCollections.observableArrayList();
        list.add(sampleList.get(0));
        AddressBookBrowser browser = new AddressBookBrowser(3, list);

        // Loads index 0 of list only.
        browser.loadProfilePage(list.get(0));
        assertTrue(list.get(0).equals(browser.getBrowserTabs().get(0).getPerson()));
        assertNull(browser.getBrowserTabs().get(1).getPerson());
        assertNull(browser.getBrowserTabs().get(2).getPerson());
    }

    @Test
    public void testLoadProfilePage_addingAndSelectingPersonsWithThreeTabs_circularReplacementAlgorithmWorks() {
        List<Person> sampleList = TestUtil.generateSampleData();
        ObservableList<Person> list = FXCollections.observableArrayList();
        list.add(sampleList.get(0));
        list.add(sampleList.get(1));
        list.add(sampleList.get(2));
        list.add(sampleList.get(3));
        AddressBookBrowser browser = new AddressBookBrowser(3, list);

        //Loads index 0, 1, 2
        browser.loadProfilePage(list.get(0));
        assertTrue(browser.getBrowserTabs().stream().filter(browserTab ->
                browserTab.getPerson().equals(list.get(0))).findAny().isPresent());
        assertTrue(browser.getBrowserTabs().stream().filter(browserTab ->
                browserTab.getPerson().equals(list.get(1))).findAny().isPresent());
        assertTrue(browser.getBrowserTabs().stream().filter(browserTab ->
                browserTab.getPerson().equals(list.get(2))).findAny().isPresent());

        //Loads index 2, 3, 0
        browser.loadProfilePage(list.get(2));
        assertTrue(browser.getBrowserTabs().stream().filter(browserTab ->
                browserTab.getPerson().equals(list.get(2))).findAny().isPresent());
        assertTrue(browser.getBrowserTabs().stream().filter(browserTab ->
                browserTab.getPerson().equals(list.get(3))).findAny().isPresent());
        assertTrue(browser.getBrowserTabs().stream().filter(browserTab ->
                browserTab.getPerson().equals(list.get(0))).findAny().isPresent());
    }

}
