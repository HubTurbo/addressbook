package address.browser;

import address.model.datatypes.ReadableViewablePerson;
import com.teamdev.jxbrowser.chromium.Browser;

/**
 * A browser tab of AddressBookBrowser.
 */
public class BrowserTab extends Browser {

    private ReadableViewablePerson person;

    public BrowserTab() {
        super();
    }

    public ReadableViewablePerson getPerson(){
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
    public synchronized void loadProfilePage(ReadableViewablePerson person) {
        this.person = person;
        super.loadURL(person.githubProfilePageUrl());
    }

    public synchronized void unloadProfilePage(){
        assert person != null;
        person = null;
    }
}
