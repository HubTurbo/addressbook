package address.ui;

import address.animation.AnimationPack;
import address.controller.PersonCardController;

import address.model.datatypes.Person;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;

import javafx.scene.control.ListCell;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.concurrent.TimeUnit;

public class PersonListViewCell extends ListCell<Person> {


    protected AnimationPack anim;

    @Override
    public void updateItem(Person person, boolean empty) {
        super.updateItem(person, empty);

        if (empty || person == null) {
            setGraphic(null);
            setText(null);
            this.setVisible(false);
        }
        else
        {
            AnchorPane pane = new PersonCardController(person).getLayout();
            setGraphic(pane);
           pane.prefHeightProperty().bind(this.prefHeightProperty());
            pane.prefWidthProperty().bind(this.widthProperty());
        }
    }



    private void animate() {
        if (anim != null && anim.getKeyFrames().size() >= 0
                && (anim.getTimeline().getStatus() == Timeline.Status.STOPPED
                || anim.getTimeline().getStatus() == Timeline.Status.PAUSED)) {
            anim.getTimeline().playFromStart();
//            if (oldIndex < getIndex() && Math.abs(oldIndex - getIndex()) == 1) {
//                anim.getTimeline().playFromStart();
//            }
        }
    }

}

