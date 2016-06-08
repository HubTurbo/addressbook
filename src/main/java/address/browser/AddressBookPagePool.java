package address.browser;

import address.model.datatypes.Person;
import address.util.FxViewUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
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
    private Stack<EmbeddedBrowser> browserStack;

    public AddressBookPagePool(int noOfPages){
        pages = new ArrayList<>(noOfPages);
        browserStack = new Stack<>();

        for (int i=0; i<noOfPages; i++){
            EmbeddedBrowser browser = new JxBrowserAdapter();
            FxViewUtil.applyAnchorBoundaryParameters(browser.getBrowserView(), 0.0, 0.0, 0.0, 0.0);
            browserStack.push(browser);
        }

    }

    /**
     * Needs to call clearNotRequiredPages before calling this method.
     * @param person
     * @return
     */
    public EmbeddedBrowser loadPersonPage(Person person){
        //TODO: browser instance is re-created here, need to check performance issue.
        Optional<EmbeddedBrowserGithubProfilePage> foundPage = pages.stream().filter(embeddedBrowserGithubProfilePage
                -> embeddedBrowserGithubProfilePage.getPerson().equals(person)).findAny();
        if (foundPage.isPresent()) {
            if (!foundPage.get().getPerson().getGithubUserName().equals(person.getGithubUserName())){
                foundPage.get().getBrowser().loadPage(person.profilePageUrl());
            }
            return foundPage.get().getBrowser();
        }
        EmbeddedBrowserGithubProfilePage newPage;
        EmbeddedBrowser browser = browserStack.pop();
        browser.loadPage(person.profilePageUrl());
        newPage = new EmbeddedBrowserGithubProfilePage(browser, person);
        pages.add(newPage);

        return browser;
    }

    /**
     * Clears pages that are not required anymore from the pool of pages.
     * @param requiredPersons The persons whose pages are <u><b>required</b></u> to be remained in the pool of pages.
     * @return
     */
    public ArrayList<EmbeddedBrowserGithubProfilePage> clearNotRequiredPages(ArrayList<Person> requiredPersons) {
        ArrayList<EmbeddedBrowserGithubProfilePage> listOfNotRequiredPage= pages.stream().filter(embeddedBrowserGithubProfilePage
              -> !requiredPersons.contains(embeddedBrowserGithubProfilePage.getPerson())).collect(Collectors.toCollection(ArrayList::new));
        listOfNotRequiredPage.stream().forEach(page -> {
            browserStack.push(page.getBrowser());
            pages.remove(page);
        });
        return listOfNotRequiredPage;
    }

    public Optional<EmbeddedBrowserGithubProfilePage> clearPersonPage(Person person) {
        Optional<EmbeddedBrowserGithubProfilePage> page= pages.stream().filter(embeddedBrowserGithubProfilePage
                -> person.equals(embeddedBrowserGithubProfilePage.getPerson())).findAny();

        if (page.isPresent()){
            browserStack.push(page.get().getBrowser());
            pages.remove(page);
        }
        return page;
    }


    public void dispose() {
        browserStack.stream().forEach(embeddedBrowser -> embeddedBrowser.dispose());
    }

    public ArrayList<Person> getPersonsLoadedInCache(){
        return pages.stream().map(p -> p.getPerson()).collect(Collectors.toCollection(ArrayList::new));
    }
}
