package address.browser;

import address.browser.page.GithubProfilePage;
import address.model.datatypes.Person;
import address.util.FxViewUtil;
import com.teamdev.jxbrowser.chromium.Browser;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages a pool of activePages, where each has a non-identical content.
 *
 * To ensure correct functionality and consistency of the pool of pages:
 * 1) To use loadPersonPage(), calling clearPagesNotRequired() or clearPageNotRequired() first is required.
 *    - Call clearPageNotRequired(parameter) when only a single person page (non-identical with the pool of pages)
 *      is to be loaded using the loadPersonPage() method.
 *    - Call clearPagesNotRequired(parameter) when multiple person page (non-identical with the pool of pages)
 *      are to be loaded using the loadPersonPage() method.
 */
public class AddressBookPagePool {

    private ArrayList<GithubProfilePage> activePages;

    /**
     * For recycling of browser instance.
     */
    private Stack<EmbeddedBrowser> inActiveBrowserStack;

    public AddressBookPagePool(int noOfPages){
        activePages = new ArrayList<>(noOfPages);
        inActiveBrowserStack = new Stack<>();

        for (int i=0; i<noOfPages; i++){
            EmbeddedBrowser browser = new JxBrowserAdapter(new Browser());
            FxViewUtil.applyAnchorBoundaryParameters(browser.getBrowserView(), 0.0, 0.0, 0.0, 0.0);
            inActiveBrowserStack.push(browser);
        }
    }

    /**
     * Loads the person's profile page.
     * Precondition: Method clearPagesNotRequired() needs to be called first to ensure the correctness of this
     *               method.
     * @param person The person whose profile page is to be loaded.
     * @return The browser assigned to load the person's profile page.
     */
    public synchronized EmbeddedBrowser loadPersonPage(Person person) {
        Optional<GithubProfilePage> foundPage = activePages.stream().filter(embeddedBrowserGithubProfilePage
                -> embeddedBrowserGithubProfilePage.getPerson().equals(person)).findAny();
        if (foundPage.isPresent()) {
            if (!foundPage.get().getPerson().getGithubUserName().equals(person.getGithubUserName())){
                foundPage.get().getBrowser().loadPage(person.profilePageUrl());
            }
            return foundPage.get().getBrowser();
        }
        GithubProfilePage newPage;
        EmbeddedBrowser browser;

        assert !inActiveBrowserStack.isEmpty();
        browser = inActiveBrowserStack.pop();

        browser.loadPage(person.profilePageUrl());
        newPage = new GithubProfilePage(browser, person);
        activePages.add(newPage);

        return browser;
    }

    /**
     * Clears pages from the pool of pages that are not required anymore.
     * @param requiredPersons The persons whose pages are to be remained in the pool of pages.
     *                        Preconditions: If loadPersonPage(Person person) method is going to be called after this
     *                                       method, the requiredPersons must contain the person that will be used in
     *                                       loadPersonPage() method.
     * @return An arraylist of pages that are cleared from the pool of pages.
     */
    public synchronized ArrayList<GithubProfilePage> clearPagesNotRequired(ArrayList<Person> requiredPersons) {
        ArrayList<GithubProfilePage> listOfNotRequiredPage = activePages.stream().filter(embeddedBrowserGithubProfilePage
              -> !requiredPersons.contains(embeddedBrowserGithubProfilePage.getPerson()))
                                           .collect(Collectors.toCollection(ArrayList::new));
        listOfNotRequiredPage.stream().forEach(page -> {
            inActiveBrowserStack.push(page.getBrowser());
            activePages.remove(page);
        });
        assert activePages.size() + inActiveBrowserStack.size() == BrowserManager.NUMBER_OF_PRELOADED_PAGE;
        return listOfNotRequiredPage;
    }

    /**
     * Clears page from the pool of activePages that are not required anymore.
     * @param person The person whose page is to be cleared, if exists.
     * @return An optional page that is cleared from the pool of pages.
     */
    public synchronized Optional<GithubProfilePage> clearPageNotRequired(Person person) {
        Optional<GithubProfilePage> page = activePages.stream().filter(embeddedBrowserGithubProfilePage
                -> person.equals(embeddedBrowserGithubProfilePage.getPerson())).findAny();

        if (page.isPresent()){
            inActiveBrowserStack.push(page.get().getBrowser());
            activePages.remove(page.get());
        }
        assert activePages.size() + inActiveBrowserStack.size() == BrowserManager.NUMBER_OF_PRELOADED_PAGE;
        return page;
    }


    public void dispose() {
        inActiveBrowserStack.stream().forEach(embeddedBrowser -> embeddedBrowser.dispose());
    }

    /**
     * Gets the person instance from the list of active pages.
     * @return An array list of person instance.
     */
    public ArrayList<Person> getActivePagesPerson(){
        return activePages.stream().map(page -> page.getPerson()).collect(Collectors.toCollection(ArrayList::new));
    }
}
