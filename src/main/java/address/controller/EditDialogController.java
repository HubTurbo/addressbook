package address.controller;

import javafx.stage.Stage;

abstract class EditDialogController {
    protected Stage dialogStage;
    protected boolean isOkClicked = false;

    /**
     * Sets the stage of this dialog.
     *
     * @param dialogStage
     */
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    /**
     * Returns the confirmation status of user
     * @return
     */
    public boolean isOkClicked() {
        return isOkClicked;
    }

    /**
     * Called when the user clicks cancel.
     */
    protected abstract void handleCancel();

    /**
     * Called when the user clicks ok.
     */
    protected abstract void handleOk();

}
