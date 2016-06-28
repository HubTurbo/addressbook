package hubturbo;

import hubturbo.embeddedbrowser.EbAttachListener;
import hubturbo.embeddedbrowser.EbDocument;
import hubturbo.embeddedbrowser.EbLoadListener;
import javafx.scene.Node;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * An interface for different type of browser engine.
 */
public interface EmbeddedBrowser {

    /**
     * Loads a web page located by the URL. This method starts asynchronous and returns immediately.
     * @param url The URL of the web page.
     */
    void loadUrl(String url);

    /**
     * Loads HTML content directly to the browser.
     * @param htmlCode The HTML markup code to be dispalyed on the browser.
     */
    void loadHTML(String htmlCode);

    /**
     * Gets graphical view of the browser.
     * @return The graphical view of the browser casted in Node form.
     */
    Node getBrowserView();

    /**
     * Checks whether the browser is loading a web page.
     * @return true if browser is loading a web page
     *         false if browser is not loading a web page.
     */
    boolean isLoading();

    /**
     * Dispose the browser. Frees up memory allocated to the browser.
     * Once called, the browser cannot be used anymore.
     */
    void dispose();

    /**
     * Gets the current displayed URL.
     * @return The current displayed URL in the form of URL object.
     * @throws MalformedURLException If the URL is unparsable.
     */
    URL getUrl() throws MalformedURLException;

    /**
     * Gets the current displayed URL.
     * @return The current displayed URL in the form of String object.
     */
    String getUrlString();

    /**
     * Gets the URL that was passed in the loadUrl method.
     * @return The URL in String form.
     */
    String getOriginUrlString();

    /**
     * Gets the URL that was passed in the loadUrl method.
     * @returnThe URL in URL form.
     * @throws MalformedURLException If the URL is unparsable.
     */
    URL getOriginUrl() throws MalformedURLException;

    /**
     * Gets the DOM model of the displayed webpage.
     * @return The DOM model.
     */
    EbDocument getDomElement();

    /**
     * Executes browser command.
     * @param command The command. For e.g. To scroll to the end of the document
     *                                      Use EbEditorCommand.SCROLL_TO_END_OF_DOCUMENT
     */
    void executeCommand(int command);

    /**
     * Adds a listener to listen to web page loaded successfully.
     * @param listener An EbLoadListener interface.
     */
    void setLoadListener(EbLoadListener listener);

    /**
     * Adds a listener to listen when the browser is (re)attached to a scene.
     * @param listener An EbAttachListener interface.
     */
    void setAttachListener(EbAttachListener listener);

}
