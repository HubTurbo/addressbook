package address;

import address.browser.BrowserManagerTest;
import address.browser.PageTest;
import address.guitests.FullSystemTest;
import address.model.AddPersonCommandTest;
import address.storage.StorageManagerTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        StorageManagerTest.class,
        FullSystemTest.class
})

public class OrderedTests {
    // the class remains empty,
    // used only as a holder for the above annotations
}