package address.controller;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


/**
 * View part of the Person Edit Dialog.
 */
public class PersonEditDialogView extends BaseView {
    private static final String ICON = "/images/edit.png";
    public static final String TITLE = "Edit Person";
    public static final String FXML = "PersonEditDialog.fxml";
    AnchorPane pane;
    Stage dialogStage;

    public PersonEditDialogView(Stage primaryStage) {
        super(primaryStage);
        pane = (AnchorPane)mainNode;
        Scene scene = new Scene(pane);
        dialogStage = loadDialogStage(TITLE, primaryStage, scene);
        setIcon(dialogStage, ICON);
        setEscKeyToDismiss(scene);
    }

    private void setEscKeyToDismiss(Scene scene) { //TODO: move to a new parent class BaseDialogView
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                dialogStage.close();
            }
        });
    }

    @Override
    String getFxmlFileName() {
        return FXML;
    } //TODO: move to parent class

    public void showAndWait(){
        dialogStage.showAndWait();
    }

    public Stage getDialogStage() {
        return dialogStage;
    }

    public void close() {
        dialogStage.close();
    }
}
