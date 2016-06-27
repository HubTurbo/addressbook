package address.ui;

import address.controller.PersonCardController;
import address.image.ImageManager;
import address.model.datatypes.person.ReadOnlyViewablePerson;

import address.util.DragContainer;
import address.util.FxViewUtil;
import address.util.collections.ReorderedList;
import com.sun.javafx.scene.control.skin.VirtualScrollBar;
import javafx.collections.ObservableList;

import javafx.scene.SnapshotParameters;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collection;
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
            container.addAllData(getListView().getSelectionModel()
                                              .getSelectedItems()
                                              .stream()
                                              .map(p -> p.getId()).collect(Collectors.toCollection(ArrayList::new)));
            content.put(DragContainer.ADDRESS_BOOK_PERSON_UUID, container);
            dragBoard.setDragView(getDragView(this.getListView().getSelectionModel().getSelectedItems()));
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
                showDragDropIndicator(event);
            }
            edgeScroll(event);

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
                List<ReadOnlyViewablePerson> listOfDragPersons =
                        listsOfPerson.stream()
                                     .filter(p -> container.getData()
                                                           .contains(p.getId()))
                                                           .collect(Collectors.toCollection(ArrayList::new));
                int moveToIndex = computeMoveToIndex(event.getSceneY(), listOfDragPersons);
                Collection<Integer> movedIndexes = reorderedList.moveElements(listOfDragPersons, moveToIndex);
                selectIndexes(movedIndexes);
            }
            event.setDropCompleted(true);
            event.consume();
        });
        setOnDragDone(DragEvent::consume);
    }

    private Image getDragView(List<ReadOnlyViewablePerson> draggedPersons) {
        HBox container = new HBox(5);
        draggedPersons.stream().forEach(p -> {
            Optional<String> profilePicUrl = p.githubProfilePicUrl();
            ImageView imageView;

            if (profilePicUrl.isPresent()) {
                imageView = new ImageView(ImageManager.getInstance().getImage(profilePicUrl.get()));
            } else {
                imageView = new ImageView(ImageManager.getDefaultProfileImage());
            }

            imageView.setFitHeight(50.0);
            imageView.setFitWidth(50.0);
            FxViewUtil.configureCircularImageView(imageView);
            container.getChildren().add(imageView);
        });
        SnapshotParameters para = new SnapshotParameters();
        para.setFill(Color.TRANSPARENT);
        return container.snapshot(para, null);
    }

    /**
     * Select this indices in the list view selection model.
     * @param movedIndexes
     */
    private void selectIndexes(Collection<Integer> movedIndexes) {
        movedIndexes.stream().forEach(index -> getListView().getSelectionModel().select(index));
    }

    /**
     * Computes the edge index(nearest edge based on the current Y Position)
     * of the listview to insert the dragged persons.
     * @param currentYPosition The current Y position relative to the attached scene.
     * @param listOfDragPersons The list of persons who are currently dragged.
     * @return
     */
    private int computeMoveToIndex(double currentYPosition, List<ReadOnlyViewablePerson> listOfDragPersons) {
        int moveToIndex;
        ObservableList<ReadOnlyViewablePerson> list = this.getListView().getItems();
        double midPoint = this.localToScene(this.getBoundsInLocal()).getMinY() + this.getHeight() /2 ;
        getListView().getSelectionModel().clearSelection();

        if (currentYPosition < midPoint) {
            moveToIndex = list.indexOf(getItem());
        } else {
            moveToIndex = list.indexOf(getItem()) + 1;
        }

        while (moveToIndex < list.size() && listOfDragPersons.contains(list.get(moveToIndex))) {
            moveToIndex++; //Move to the next index if the current index contains one of the dragged person.
        }
        return moveToIndex;
    }

    /**
     * Shows where the drag cell will be placed by showing an indicator on the listview.
     * @param event
     */
    private void showDragDropIndicator(DragEvent event) {
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
    private void edgeScroll(DragEvent event) {
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
