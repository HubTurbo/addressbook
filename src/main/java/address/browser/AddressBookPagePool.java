package address.browser;

import address.model.datatypes.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Contains a pool of page, where each displaying non-identical content.
 * The caching mechanism provides overall faster loading by:
 * 1) Returning browser instance immediately by preventing re-loading of pages if any of the browser in the pool is
 *    found containing displaying the same page(Identification by URL).
 * TODO: Implement a rule such that after a certain period, the algorithm may reload the browser that contains the same page, as it may be outdated.
 */
public class AddressBookPagePool {

    private List<EmbeddedBrowserGithubProfilePage> pages;

    public AddressBookPagePool(int noOfPages){
        pages = new ArrayList<>(noOfPages);
    }

    /**
     * Needs to call clearUnneededPersonPage before calling this method.
     * @param person
     * @return
     */
    public EmbeddedBrowser loadPersonPage(Person person){
        //TODO: browser instance is re-created here, need to check performance issue.
        Optional<EmbeddedBrowserGithubProfilePage> foundPage = pages.stream().filter(embeddedBrowserGithubProfilePage
                                            -> embeddedBrowserGithubProfilePage.getPerson().equals(person)).findAny();

        if (foundPage.isPresent()) {
            return foundPage.get().getBrowser();
        }

        EmbeddedBrowserGithubProfilePage newPage;
        EmbeddedBrowser browser = new JxBrowserAdapter();
        browser.loadPage(person.profilePageUrl());
        newPage = new EmbeddedBrowserGithubProfilePage(browser, person);
        pages.add(newPage);

        return browser;
    }

    public void clearUnneededPersonPage(ArrayList<Person> neededPersons) {
        pages = pages.stream().filter(embeddedBrowserGithubProfilePage
                -> neededPersons.contains(embeddedBrowserGithubProfilePage.getPerson())).collect(Collectors.toList());
    }
}
