package address.exceptions;

/**
 * Thrown when the expected url and future urls to load is more than its paging capability.
 */
public class IllegalArgumentSizeException extends Exception {

    private String msg;

    public IllegalArgumentSizeException(int noOfUrl, int expectedNoOfUrl) {
        msg = "The HyperBrowser can not load " + noOfUrl + "URLs. "
                + "The HyperBrowser is configured to load a maximum of " + expectedNoOfUrl + "URL.";
    }

    @Override
    public String getMessage() {
        return msg;
    }
}
