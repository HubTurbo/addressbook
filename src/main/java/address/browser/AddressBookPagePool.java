package address.browser;

import address.browser.page.GithubProfilePage;
import address.model.datatypes.Person;
import address.util.FxViewUtil;
import com.teamdev.jxbrowser.chromium.Browser;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages and perform caching for a pool of activePages, where each having a non-identical content.
 *
 * The caching mechanism provides faster loading by:
 * 1) Upon calling on loadPersonPage(personToBeLoaded), It caches the next n person in the displayed list starting from
 *    the personToBeLoaded index in the displayed list.
 * 2) Returning page's browser instance immediately by preventing re-loading of pages if any of the page in the pool is
 *    found containing displaying the same page.
 *
 * To ensure correct functionality and consistency of the pool of pages:
 * 1) To use loadPersonPage(), calling clearPagesNotRequired() or clearPersonPage() first is required.
 *    - Call clearPersonPage() when only a single person page (non-identical with the pool of pages) is to be loaded
 *      using the loadPersonPage() method.
 *    - Call clearPagesNotRequired() when multiple person page (non-identical with the pool of pages) are to be loaded
 *      using the loadPersonPage() method.
 */
public class AddressBookPagePool {

    private ArrayList<GithubProfilePage> activePages;

    /**
     * For recycling of browser instance.
     */
    private Stack<EmbeddedBrowser> nonActiveBrowserStack;

    public AddressBookPagePool(int noOfPages){
        activePages = new ArrayList<>(noOfPages);
        nonActiveBrowserStack = new Stack<>();

        for (int i=0; i<noOfPages; i++){
            EmbeddedBrowser browser = new JxBrowserAdapter(new Browser());
            FxViewUtil.applyAnchorBoundaryParameters(browser.getBrowserView(), 0.0, 0.0, 0.0, 0.0);
            nonActiveBrowserStack.push(browser);
        }
    }

    /**
     * Loads the person's profile page.
     * Precondition: Method clearPagesNotRequired() is needed to be called first to ensure the correctness of this
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

        assert !nonActiveBrowserStack.isEmpty();
        browser = nonActiveBrowserStack.pop();

        browser.loadPage(person.profilePageUrl());
        newPage = new GithubProfilePage(browser, person);
        activePages.add(newPage);

        return browser;
    }

    /**
     * Clears pages from the pool of pages that are not required anymore.
     * @param requiredPersons The persons whose pages are <u><b>required</b></u> to be remained in the pool of pages.
     * @return An arraylist of pages that are cleared from the pool of pages.
     */
    public synchronized ArrayList<GithubProfilePage> clearPagesNotRequired(ArrayList<Person> requiredPersons) {
        ArrayList<GithubProfilePage> listOfNotRequiredPage = activePages.stream().filter(embeddedBrowserGithubProfilePage
              -> !requiredPersons.contains(embeddedBrowserGithubProfilePage.getPerson())).collect(Collectors.toCollection(ArrayList::new));
        listOfNotRequiredPage.stream().forEach(page -> {
            nonActiveBrowserStack.push(page.getBrowser());
            activePages.remove(page);
        });
        assert activePages.size() + nonActiveBrowserStack.size() == BrowserManager.NUMBER_OF_PRELOADED_PAGE;
        return listOfNotRequiredPage;
    }

    /**
     * Clears page from the pool of activePages that are not required anymore.
     * @param person The person whose page is to be cleared, if exists.
     * @return An optional page that is cleared from the pool of pages.
     */
    public synchronized Optional<GithubProfilePage> clearPersonPage(Person person) {
        Optional<GithubProfilePage> page = activePages.stream().filter(embeddedBrowserGithubProfilePage
                -> person.equals(embeddedBrowserGithubProfilePage.getPerson())).findAny();

        if (page.isPresent()){
            nonActiveBrowserStack.push(page.get().getBrowser());
            activePages.remove(page.get());
        }
        assert activePages.size() + nonActiveBrowserStack.size() == BrowserManager.NUMBER_OF_PRELOADED_PAGE;
        return page;
    }


    public void dispose() {
        nonActiveBrowserStack.stream().forEach(embeddedBrowser -> embeddedBrowser.dispose());
    }

    /**
     * Gets the person instance from the list of active pages.
     * @return A array list of person instance.
     */
    public ArrayList<Person> getActivePagesPerson(){
        return activePages.stream().map(p -> p.getPerson()).collect(Collectors.toCollection(ArrayList::new));
    }
}
