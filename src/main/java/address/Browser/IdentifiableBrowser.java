package address.browser;

import address.model.Person;
import com.teamdev.jxbrowser.chromium.Browser;

/**
 * The normal browser with the capability of tracking whether whose(the address book contacts) page is loaded.
 */
public class IdentifiableBrowser extends Browser {

    private Person person;

    public IdentifiableBrowser() {
        super();
    }

    public Person getPerson(){
        return this.person;
    }

    /**
     * Checks if browser is assigned to load a person's GitHub profile page
     * @return
     */
    public boolean isAssignedToLoadAPersonGitHubProfilePage(){
        return person != null;
    }

    public synchronized void loadURL(String s, Person person) {
        this.person = person;
        super.loadURL(s);
    }
}
