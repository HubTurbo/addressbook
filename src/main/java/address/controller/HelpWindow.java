package address.controller;

import address.MainApp;
import address.util.AppUtil;
import commons.FxViewUtil;
import hubturbo.EmbeddedBrowser;
import hubturbo.embeddedbrowser.fxbrowser.FxBrowserAdapter;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;

import static javax.accessibility.AccessibleRole.ICON;

/**
 * Controller for a help page
 */
public class HelpWindow extends BaseUiController{
    private static final String ICON = "/images/help_icon.png";
    public static final String FXML = "HelpWindow.fxml";
    public static final String TITLE = "Help";
    private AnchorPane pane;
    private Stage dialogStage;

    @FXML
    private AnchorPane mainPane;

    public HelpWindow() {
    }

    @Override
    public void init(){
        Scene scene = new Scene(pane);
        dialogStage = loadDialogStage(TITLE, null, scene);
        dialogStage.setMaximized(true);
        setIcon(dialogStage, ICON);
    }

    @FXML
    public void initialize() {
        EmbeddedBrowser browser = new FxBrowserAdapter(new WebView());
        browser.loadUrl(AppUtil.getResourceUrl("/help_html/index.html").toExternalForm());
        FxViewUtil.applyAnchorBoundaryParameters(browser.getBrowserView(), 0.0, 0.0, 0.0, 0.0);
        mainPane.getChildren().add(browser.getBrowserView());
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
