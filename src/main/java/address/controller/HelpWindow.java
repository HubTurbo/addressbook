package address.controller;

import address.util.AppLogger;
import address.util.AppUtil;
import address.util.LoggerManager;
import commons.FxViewUtil;
import hubturbo.EmbeddedBrowser;
import hubturbo.embeddedbrowser.fxbrowser.FxBrowserAdapter;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * The Help Window UI Part.
 */
public class HelpWindow extends BaseUiPart {
    private static final AppLogger logger = LoggerManager.getLogger(HelpWindow.class);
    private static final String ICON = "/images/help_icon.png";
    public static final String FXML = "HelpWindow.fxml";
    public static final String TITLE = "Help";

    private AnchorPane pane;
    private Stage dialogStage;

    @FXML
    private AnchorPane mainPane;

    public HelpWindow() {
    }

    public static HelpWindow load(Stage primaryStage) {
        logger.debug("Showing help page about the application.");
        HelpWindow helpWindow = UiPartLoader.loadUiPart(primaryStage, new HelpWindow());
        helpWindow.configure();
        return helpWindow;
    }

    @FXML
    public void initialize() {
        EmbeddedBrowser browser = new FxBrowserAdapter(new WebView());
        browser.loadUrl(AppUtil.getResourceUrl("/help_html/index.html").toExternalForm());
        FxViewUtil.applyAnchorBoundaryParameters(browser.getBrowserView(), 0.0, 0.0, 0.0, 0.0);
        mainPane.getChildren().add(browser.getBrowserView());
    }

    private void configure(){
        Scene scene = new Scene(pane);
        //Null passed as the parent stage to make it non-modal.
        dialogStage = createDialogStage(TITLE, null, scene);
        dialogStage.setMaximized(true); //TODO: set a more appropriate initial size
        setIcon(dialogStage, ICON);
    }

    @Override
    public void setNode(Node node) {
        pane = (AnchorPane)node;
    }

    @Override
    public String getFxmlPath() {
        return FXML;
    }

    public void show() {
        dialogStage.showAndWait();
    }

}
