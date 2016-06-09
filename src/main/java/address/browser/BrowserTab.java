package address.browser;

import address.model.datatypes.person.ReadOnlyViewablePerson;
import com.teamdev.jxbrowser.chromium.Browser;

/**
 * A browser tab of AddressBookBrowser.
 */
public class BrowserTab extends Browser {

    private ReadOnlyViewablePerson person;

    public BrowserTab() {
        super();
    }

    public ReadOnlyViewablePerson getPerson(){
        return this.person;
    }

    /**
     * Checks if this browser tab is assigned to load a person's GitHub profile page
     */
    public boolean isAssigned(){
        return person != null;
    }

    /**
     * Loads the person's profile page to the browser tab.
     */
    public synchronized void loadProfilePage(ReadOnlyViewablePerson person) {
        this.person = person;
        super.loadURL(person.githubProfilePageUrl());
    }

    public synchronized void unloadProfilePage(){
        assert person != null;
        person = null;
    }
}
