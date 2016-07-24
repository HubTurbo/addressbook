package address.controller;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * The View class for the {@link HelpWindowUiPart}.
 */
public class HelpWindowView extends BaseView{
    private static final String ICON = "/images/help_icon.png";
    public static final String FXML = "HelpWindow.fxml";
    public static final String TITLE = "Help";
    AnchorPane pane;
    Stage dialogStage;

    @Override
    String getFxmlFileName() {
        return FXML;
    }

    public HelpWindowView(Stage primaryStage) {
        super(primaryStage);
        pane = (AnchorPane) mainNode;
        Scene scene = new Scene(pane);
        dialogStage = loadDialogStage(TITLE, null, scene);
        setIcon(dialogStage, ICON);
        dialogStage.setMaximized(true);
    }

    public void show() {
        dialogStage.showAndWait();
    }


}
