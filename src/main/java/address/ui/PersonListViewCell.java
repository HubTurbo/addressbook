package address.ui;

import address.controller.PersonCardController;
import address.model.datatypes.person.ReadOnlyViewablePerson;

import address.util.DragContainer;
import address.util.FxViewUtil;
import address.util.collections.ReorderedList;
import com.sun.javafx.scene.control.skin.VirtualScrollBar;
import javafx.collections.ObservableList;

import javafx.geometry.Rectangle2D;

import javafx.scene.SnapshotParameters;
import javafx.scene.control.ListCell;
import javafx.scene.input.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PersonListViewCell extends ListCell<ReadOnlyViewablePerson> {

    public static final int SCROLL_AREA = 15;

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
            DragContainer container = new DragContainer();
            container.addAllData(getListView().getSelectionModel().getSelectedItems().stream().map(p
                                                        -> p.getId()).collect(Collectors.toCollection(ArrayList::new)));
            content.put(DragContainer.ADDRESS_BOOK_PERSON_UUID, container);
            SnapshotParameters para = new SnapshotParameters();
            double lvOffset = this.getListView().localToParent(this.getListView().getBoundsInLocal()).getMinY();
            double cardOffset = this.localToParent(this.getBoundsInLocal()).getMinY();
            double snapShotWidth = this.getWidth();
            double snapShotHeight = this.getHeight() * getListView().getSelectionModel().getSelectedIndices().size();
            para.setViewport(new Rectangle2D(0, lvOffset + cardOffset, snapShotWidth, snapShotHeight));
            //dragBoard.setDragView(this.getListView().snapshot(para, null));
            dragBoard.setDragView(FxViewUtil.getDragView(this.getListView().getSelectionModel().getSelectedItems()));
            dragBoard.setContent(content);
            event.consume();
        });

        setOnDragOver(event -> {

            if (getItem() == null) {
                return;
            }

            if (event.getGestureSource() != this &&
                    event.getDragboard().hasContent(DragContainer.ADDRESS_BOOK_PERSON_UUID)) {
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
                    event.getDragboard().hasContent(DragContainer.ADDRESS_BOOK_PERSON_UUID)) {
                setOpacity(0.6);
            }
        });

        setOnDragExited(event -> {
            if (getItem() == null) {
                return;
            }

            if (event.getGestureSource() != this &&
                    event.getDragboard().hasContent(DragContainer.ADDRESS_BOOK_PERSON_UUID)) {
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
            if (dragboard.hasContent(DragContainer.ADDRESS_BOOK_PERSON_UUID)) {
                DragContainer container = (DragContainer) dragboard.getContent(DragContainer.ADDRESS_BOOK_PERSON_UUID);
                ObservableList<ReadOnlyViewablePerson> listsOfPerson = getListView().getItems();
                List<ReadOnlyViewablePerson> listOfDragPersons = listsOfPerson.stream().filter(p -> container.getData().contains(p.getId())).collect(Collectors.toCollection(ArrayList::new));
                moveCell(reorderedList, event.getSceneY(), listOfDragPersons);
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
     */
    private void moveCell(ReorderedList sortedList, double currentYPosition, List<ReadOnlyViewablePerson> listOfDragPersons) {
        ObservableList<ReadOnlyViewablePerson> list = getListView().getItems();

        //TODO: rewrite moveElement to handle multiple item moves.
        // Just collect a list of item(order matters) to move, then call the moveElements().
        listOfDragPersons.stream().forEach(personToMove -> {
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
        });

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
