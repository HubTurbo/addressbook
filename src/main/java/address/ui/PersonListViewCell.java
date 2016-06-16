package address.ui;

import address.controller.PersonCardController;
import address.model.datatypes.person.ReadOnlyViewablePerson;

import address.util.ReorderedList;
import javafx.collections.ObservableList;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ListCell;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;

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
            }
            event.consume();
        });

        setOnDragEntered(event -> {
            if (event.getGestureSource() != this &&
                    event.getDragboard().hasString()) {
                setOpacity(0.7);
            }
        });

        setOnDragExited(event -> {
            if (event.getGestureSource() != this &&
                    event.getDragboard().hasString()) {
                setOpacity(1);
            }
        });

        setOnDragDropped(event -> {
            if (getItem() == null) {
                return;
            }

            Dragboard dragboard = event.getDragboard();

            if (dragboard.hasString()) {
                ObservableList<ReadOnlyViewablePerson> list = getListView().getItems();
                try {
                    sortedList.moveElement(Integer.valueOf(dragboard.getString()), list.indexOf(getItem()));
                } catch (UnsupportedOperationException e) {
                    e.printStackTrace();
                }
            }

            event.setDropCompleted(true);

            event.consume();
        });

        setOnDragDone(DragEvent::consume);

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
