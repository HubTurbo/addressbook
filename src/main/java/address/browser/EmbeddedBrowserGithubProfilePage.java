package address.browser;

import address.model.datatypes.Person;

/**
 * Created by YL Lim on 7/6/2016.
 */
public class EmbeddedBrowserGithubProfilePage extends EmbeddedBrowserPage{

    private Person person;

    public EmbeddedBrowserGithubProfilePage(EmbeddedBrowser browser, Person person) {
        super(browser);
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }
}
