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
    AnchorPane page;
    Stage dialogStage;

    public PersonEditDialogView(Stage primaryStage) {
        super(primaryStage);
        page = (AnchorPane)mainNode;
        Scene scene = new Scene(page);
        dialogStage = loadDialogStage("Edit Person", primaryStage, scene);
        setIcon(dialogStage, ICON);
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                dialogStage.close();
            }
        });
    }

    @Override
    String getFxmlFileName() {
        return "PersonEditDialog.fxml";
    }

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
