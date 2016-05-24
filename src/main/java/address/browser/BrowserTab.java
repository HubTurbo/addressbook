package address.browser;

import address.model.Person;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
import com.teamdev.jxbrowser.chromium.events.LoadListener;

/**
 * A browser tab of AddressBookBrowser.
 */
public class BrowserTab extends Browser {

    private Person person;

    public BrowserTab() {
        super();
    }

    public Person getPerson(){
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
    public synchronized void loadProfilePage(Person person) {
        this.person = person;
        super.loadURL(person.getProfilePageUrl());
    }

    public synchronized void unloadProfilePage(){
        assert person != null;
        person = null;
    }
}
