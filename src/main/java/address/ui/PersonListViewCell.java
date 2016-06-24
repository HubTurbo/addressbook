package address.ui;

import address.controller.PersonCardController;
import address.model.datatypes.person.ReadOnlyViewablePerson;

import address.util.FxViewUtil;
import address.util.collections.ReorderedList;
import com.sun.javafx.scene.control.skin.VirtualScrollBar;
import javafx.collections.ObservableList;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ListCell;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import java.util.Optional;

public class PersonListViewCell extends ListCell<ReadOnlyViewablePerson> {

    public static final int SCROLL_AREA = 15;
    private HBox cellGraphic;

    public PersonListViewCell(ReorderedList<ReadOnlyViewablePerson> reorderedList) {

        setOnMouseClicked(event -> {
            if (getItem() == null) {
                getListView().getSelectionModel().clearSelection();
            }
        });

        setOnDragDetected(event -> {
            if (getItem() == null) {
                return;
            }

            Dragboard dragBoard = startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(getListView().getItems().indexOf(getItem())));
            dragBoard.setDragView(getGraphic().snapshot(new SnapshotParameters(), null));
            dragBoard.setContent(content);
            event.consume();
        });

        setOnDragOver(event -> {

            if (getItem() == null) {
                return;
            }

            if (event.getGestureSource() != this &&
                    event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                showDropLocationIndicator(event);

            }
            scrollIfPointerAtScrollArea(event);

            event.consume();
        });

        setOnDragEntered(event -> {

            if (getItem() == null) {
                return;
            }

            if (event.getGestureSource() != this &&
                    event.getDragboard().hasString()) {
                setOpacity(0.6);
            }
        });

        setOnDragExited(event -> {
            if (getItem() == null) {
                return;
            }

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
                moveCell(reorderedList, event.getSceneY(), Integer.valueOf(dragboard.getString()));
            }

            event.setDropCompleted(true);
            event.consume();
        });

        setOnDragDone(DragEvent::consume);

    }

    /**
     * Moves the cell from the drag source to the edge of the nearest cell .
     * @param sortedList The ReorderedList.
     * @param currentYPosition  The current Y position relative to the attached scene.
     * @param indexOfSourceCell The index of the cell to be moved to the new location.
     */
    private void moveCell(ReorderedList sortedList, double currentYPosition, int indexOfSourceCell) {
        ObservableList<ReadOnlyViewablePerson> list = getListView().getItems();

        ReadOnlyViewablePerson personToMove = list.get(indexOfSourceCell);
        int moveToIndex;
        double midPoint = this.localToScene(this.getBoundsInLocal()).getMinY() + this.getHeight() /2 ;

        if (currentYPosition < midPoint) {
            moveToIndex = list.indexOf(getItem());
        } else {
            moveToIndex = list.indexOf(getItem()) + 1;
        }

        int moveFromIndex = list.indexOf(personToMove);
        if (moveFromIndex != moveToIndex && moveFromIndex + 1 != moveToIndex) {
            sortedList.moveElement(moveFromIndex, moveToIndex);
            getListView().getSelectionModel().clearAndSelect(list.indexOf(personToMove));
        }
    }

    /**
     * Shows where the drag cell will be placed by showing an indicator on the listview.
     * @param event
     */
    private void showDropLocationIndicator(DragEvent event) {
        double midPoint = this.localToScene(this.getBoundsInLocal()).getMinY() + this.getHeight() /2 ;
        double pointerY = event.getSceneY();
        if (pointerY < midPoint) {
            setDropLocationIndicator("top");
        } else {
            setDropLocationIndicator("bottom");
        }
    }

    /**
     * Scrolls up or down if pointer reaches the edge(top and bottom) of the listview.
     * @param event
     */
    private void scrollIfPointerAtScrollArea(DragEvent event) {
        double maxY = getListView().localToScene(getListView().getBoundsInLocal()).getMaxY();
        double minY = getListView().localToScene(getListView().getBoundsInLocal()).getMinY();
        Optional<VirtualScrollBar> scrollbar = FxViewUtil.getScrollBarFromListView(getListView());
        if (scrollbar.isPresent()) {
            if (event.getSceneY() > maxY - SCROLL_AREA && event.getSceneY() < maxY) {
                scrollbar.get().increment();
            } else if (event.getSceneY() < minY + SCROLL_AREA && event.getSceneY() > minY) {
                scrollbar.get().decrement();
            }
        }
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
    protected void updateItem(ReadOnlyViewablePerson person, boolean empty) {
        super.updateItem(person, empty);

        if (empty || person == null) {
            setGraphic(null);
            setText(null);
        } else {
            setGraphic(new PersonCardController(person).getLayout());
        }
    }
}
