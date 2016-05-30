package address.unittests;

import address.browser.AddressBookBrowser;
import address.browser.BrowserTab;
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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
    public void testLoadProfilePage_oneTab_browserIsAssignedToLoadPerson() throws NoSuchMethodException,
                                                                                  InvocationTargetException,
                                                                                  IllegalAccessException {
        ObservableList<Person> list = FXCollections.observableArrayList();
        list.setAll(TestUtil.generateSampleData());

        AddressBookBrowser browser = new AddressBookBrowser(1, list);
        ArrayList<BrowserTab> tabs = getBrowserTabs(browser);

        browser.loadProfilePage(list.get(1));
        assertTrue(tabs.get(0).getPerson().equals(list.get(1)));

        browser.loadProfilePage(list.get(4));
        assertTrue(tabs.get(0).getPerson().equals(list.get(4)));
    }

    @Test
    public void testLoadProfilePage_twoTabs_browserIsAssignedToLoadPersons() throws NoSuchMethodException,
                                                                                    InvocationTargetException,
                                                                                    IllegalAccessException {
        ObservableList<Person> list = FXCollections.observableArrayList();
        list.setAll(TestUtil.generateSampleData());
        AddressBookBrowser browser = new AddressBookBrowser(2, list);
        ArrayList<BrowserTab> browserTabs = getBrowserTabs(browser);

        //Load list[i] and check if the browser is assigned to load the list[i] and list[i + 1]
        browser.loadProfilePage(list.get(3));
        assertTrue(browserTabs.get(0).getPerson().equals(list.get(3)));
        assertTrue(browserTabs.get(1).getPerson().equals(list.get(4)));
        assertEquals(browser.getAddressBookBrowserView().getSelectionModel().getSelectedIndex(), 0);

        //Person should be already loaded and browser tab will switch to index 1.
        browser.loadProfilePage(list.get(4));
        assertEquals(browser.getAddressBookBrowserView().getSelectionModel().getSelectedIndex(), 1);

    }

    @Test
    public void testLoadProfilePage_addingAndSelectingPersonsWithThreeTabs_browserShouldNotLoadMoreThanOnePerson()
                                                                                           throws NoSuchMethodException,
                                                                                           InvocationTargetException,
                                                                                           IllegalAccessException {
        List<Person> sampleList = TestUtil.generateSampleData();
        ObservableList<Person> list = FXCollections.observableArrayList();
        list.add(sampleList.get(0));
        AddressBookBrowser browser = new AddressBookBrowser(3, list);
        ArrayList<BrowserTab> browserTabs = getBrowserTabs(browser);

        // Loads index 0 of list only.
        browser.loadProfilePage(list.get(0));
        assertTrue(list.get(0).equals(browserTabs.get(0).getPerson()));
        assertNull(browserTabs.get(1).getPerson());
        assertNull(browserTabs.get(2).getPerson());
    }

    @Test
    public void testLoadProfilePage_addingAndSelectingPersonsWithThreeTabs_circularReplacementAlgorithmWorks()
                                                                                        throws NoSuchMethodException,
                                                                                        InvocationTargetException,
                                                                                        IllegalAccessException {
        List<Person> sampleList = TestUtil.generateSampleData();
        ObservableList<Person> list = FXCollections.observableArrayList();
        list.add(sampleList.get(0));
        list.add(sampleList.get(1));
        list.add(sampleList.get(2));
        list.add(sampleList.get(3));
        AddressBookBrowser browser = new AddressBookBrowser(3, list);
        ArrayList<BrowserTab> browserTabs = getBrowserTabs(browser);

        //Loads index 0, 1, 2
        browser.loadProfilePage(list.get(0));
        assertTrue(browserTabs.stream().filter(browserTab ->
                browserTab.getPerson().equals(list.get(0))).findAny().isPresent());
        assertTrue(browserTabs.stream().filter(browserTab ->
                browserTab.getPerson().equals(list.get(1))).findAny().isPresent());
        assertTrue(browserTabs.stream().filter(browserTab ->
                browserTab.getPerson().equals(list.get(2))).findAny().isPresent());

        //Loads index 2, 3, 0
        browser.loadProfilePage(list.get(2));
        assertTrue(browserTabs.stream().filter(browserTab ->
                browserTab.getPerson().equals(list.get(2))).findAny().isPresent());
        assertTrue(browserTabs.stream().filter(browserTab ->
                browserTab.getPerson().equals(list.get(3))).findAny().isPresent());
        assertTrue(browserTabs.stream().filter(browserTab ->
                browserTab.getPerson().equals(list.get(0))).findAny().isPresent());
    }

    /**
     * Gets the browserTabs of the browser.
     * @param browser
     * @return the browserTabs of the browser.
     * @throws NoSuchMethodException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    private ArrayList<BrowserTab> getBrowserTabs(AddressBookBrowser browser) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method getBrowserTabsMethod = browser.getClass().getDeclaredMethod("getBrowserTabs");
        getBrowserTabsMethod.setAccessible(true);
        return (ArrayList<BrowserTab>)getBrowserTabsMethod.invoke(browser);
    }
}
