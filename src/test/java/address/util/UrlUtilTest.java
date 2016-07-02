package address.util;

import junit.framework.TestCase;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Tests the UrlUtil methods.
 */
public class UrlUtilTest {
    
    private List<URL> listOfUrl;
    private final String GITHUB_ROOT_URL = "https://github.com/";
    private final int noOfFutureUrls = 2;

    public UrlUtilTest() throws MalformedURLException {
        listOfUrl = new ArrayList<>();
        listOfUrl.add(new URL(GITHUB_ROOT_URL + 1));
        listOfUrl.add(new URL(GITHUB_ROOT_URL + 2));
        listOfUrl.add(new URL(GITHUB_ROOT_URL + 3));
        listOfUrl.add(new URL(GITHUB_ROOT_URL + 4));
        listOfUrl.add(new URL(GITHUB_ROOT_URL + 5));
        listOfUrl.add(new URL(GITHUB_ROOT_URL));
        listOfUrl.add(new URL(GITHUB_ROOT_URL));
        listOfUrl.add(new URL(GITHUB_ROOT_URL));
        listOfUrl.add(new URL(GITHUB_ROOT_URL));
        listOfUrl.add(new URL(GITHUB_ROOT_URL + "a"));
        listOfUrl.add(new URL(GITHUB_ROOT_URL));
        listOfUrl.add(new URL(GITHUB_ROOT_URL));
        listOfUrl.add(new URL(GITHUB_ROOT_URL));
    }

    @Test
    public void testCompareBaseUrls_differentCapital_success() throws MalformedURLException {
        URL url1 = new URL("https://www.Google.com/a");
        URL url2 = new URL("https://www.google.com/A");
        assertTrue(UrlUtil.compareBaseUrls(url1, url2));
    }

    @Test
    public void testCompareBaseUrls_testWithAndWithoutWww_success() throws MalformedURLException {
        URL url1 = new URL("https://google.com/a");
        URL url2 = new URL("https://www.google.com/a");
        assertTrue(UrlUtil.compareBaseUrls(url1, url2));
    }

    @Test
    public void testCompareBaseUrls_differentSlashes_success() throws MalformedURLException {
        URL url1 = new URL("https://www.Google.com/a/acb/");
        URL url2 = new URL("https://www.google.com/A/acb");
        assertTrue(UrlUtil.compareBaseUrls(url1, url2));
    }

    @Test
    public void testCompareBaseUrls_differentUrl_fail() throws MalformedURLException {
        URL url1 = new URL("https://www.Google.com/a/ac_b/");
        URL url2 = new URL("https://www.google.com/A/acb");
        assertFalse(UrlUtil.compareBaseUrls(url1, url2));
    }


    @SuppressWarnings("unchecked")
    @Test
    public void testGetFutureUrls_listMoreThan3Person_nextTwoIndexPersonReturned() {
        List<URL> list = UrlUtil.getFutureUrls(listOfUrl, 0, noOfFutureUrls);
        assertTrue(list.contains(listOfUrl.get(1)));
        assertTrue(list.contains(listOfUrl.get(2)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetFutureUrls_NearTheEndOfList_resultOverlappedToLowerIndex() {
        List<URL> list = UrlUtil.getFutureUrls(listOfUrl.subList(0, 5), 3, noOfFutureUrls);
        assertTrue(list.contains(listOfUrl.get(4)));
        assertTrue(list.contains(listOfUrl.get(0)));
    }

    @Test
    public void testGetFutureUrls_listLessThan3Person_resultSizeBoundedToListSize() {
        List<URL> list = UrlUtil.getFutureUrls(listOfUrl.subList(0,2), 0, noOfFutureUrls);
        assertTrue(list.contains(listOfUrl.get(1)));
        TestCase.assertFalse(list.contains(listOfUrl.get(0)));
        TestCase.assertFalse(list.contains(listOfUrl.get(2)));
        TestCase.assertFalse(list.contains(listOfUrl.get(3)));
        TestCase.assertFalse(list.contains(listOfUrl.get(4)));
    }

    @Test
    public void testGetFutureUrls_listOnly1Person_resultSizeBoundedToListSize() {
        List<URL> list = UrlUtil.getFutureUrls(listOfUrl.subList(0,1), 0, noOfFutureUrls);
        assertEquals(list.size(), 0);
    }

    @Test
    public void testGetFutureUrls_listWithDuplicateUrls_ignoreDuplicateUrls() {
        List<URL> list = UrlUtil.getFutureUrls(listOfUrl.subList(5,13),
                0, noOfFutureUrls);
        assertEquals(list.size(), 1);
        assertTrue(list.contains(listOfUrl.get(9)));
        list = UrlUtil.getFutureUrls(listOfUrl.subList(5,13), 4, noOfFutureUrls);
        assertEquals(list.size(), 1);
        assertTrue(list.contains(listOfUrl.get(8)));
    }
}
