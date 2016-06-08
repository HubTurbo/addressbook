package address.browser.page;

import address.browser.EmbeddedBrowser;
import address.model.datatypes.Person;

/**
 * A github profile page
 */
public class GithubProfilePage extends Page {

    private Person person;

    public GithubProfilePage(EmbeddedBrowser browser, Person person) {
        super(browser);
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }


}
