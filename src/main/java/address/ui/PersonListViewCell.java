package address.ui;

import address.controller.PersonCardController;
import address.model.datatypes.person.ReadOnlyViewablePerson;

import address.util.ReorderedList;
import javafx.collections.ObservableList;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ListCell;
import javafx.scene.input.*;
import javafx.scene.layout.*;

public class PersonListViewCell extends ListCell<ReadOnlyViewablePerson> {

    private HBox cellGraphic;

    ReorderedList sortedList;

    public PersonListViewCell(ReorderedList sortedList) {
        this.sortedList = sortedList;
        setOnDragDetected(event -> {
            if (getItem() == null) {
                return;
            }

            Dragboard dragBoard = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(getListView().getItems().indexOf(getItem())));
            dragBoard.setDragView(cellGraphic.snapshot(new SnapshotParameters(), null));
            dragBoard.setContent(content);
            event.consume();
        });

        setOnDragOver(event -> {

            if (event.getGestureSource() != this &&
                    event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                double midPoint = this.localToScene(this.getBoundsInLocal()).getMaxY()
                                        + this.getHeight() /2 ;
                double pointerY = event.getScreenY();
                System.out.println("cellHeight: " + this.getHeight() + " midPoint: " + midPoint + " pointer: " + pointerY);
                if (pointerY < midPoint) {
                    setDropLocationIndicator("top");
                } else {
                    setDropLocationIndicator("bottom");
                }
            }
            event.consume();
        });

        setOnDragEntered(event -> {
            if (event.getGestureSource() != this &&
                    event.getDragboard().hasString()) {
                setOpacity(0.6);
            }
        });

        setOnDragExited(event -> {
            if (event.getGestureSource() != this &&
                    event.getDragboard().hasString()) {
                clearDropLocationIndicator();
                setOpacity(1);
            }
        });

        setOnDragDropped(event -> {
            if (getItem() == null) {
                return;
            }
            clearDropLocationIndicator();
            Dragboard dragboard = event.getDragboard();

            if (dragboard.hasString()) {
                ObservableList<ReadOnlyViewablePerson> list = getListView().getItems();

                ReadOnlyViewablePerson personToMove = list.get(Integer.valueOf(dragboard.getString()));
                int moveToIndex;

                double midPoint = this.localToScene(this.getBoundsInLocal()).getMaxY() + this.getHeight()/2;
                double pointerY = event.getScreenY();
                if (pointerY < midPoint) {
                    moveToIndex = list.indexOf(getItem());
                } else {
                    moveToIndex = list.indexOf(getItem()) + 1;
                }
                sortedList.moveElement(list.indexOf(personToMove), moveToIndex);
                getListView().getSelectionModel().select(list.indexOf(personToMove));
            }

            event.setDropCompleted(true);
            event.consume();
        });

        setOnDragDone(DragEvent::consume);

    }

    private void setDropLocationIndicator(String location) {
        if (location.equals("top")) {
            this.setStyle(this.getGraphic().getStyle() + " -fx-border-color: #0645AD; -fx-border-width: 2.0 0.0 0.0 0.0;");
        } else if (location.equals("bottom")) {
            this.setStyle(this.getGraphic().getStyle() + "-fx-border-color: #0645AD; -fx-border-width: 0.0 0.0 2.0 0.0;");
        }

    }

    private void clearDropLocationIndicator() {
        this.setStyle("");
    }

    @Override
    public void updateItem(ReadOnlyViewablePerson person, boolean empty) {
        super.updateItem(person, empty);

        if (empty || person == null) {
            setGraphic(null);
            setText(null);
        } else {
            cellGraphic = new PersonCardController(person).getLayout();
            setGraphic(cellGraphic);
        }
    }
}
