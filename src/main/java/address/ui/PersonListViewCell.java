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
import javafx.util.Duration;

import java.util.concurrent.TimeUnit;

public class PersonListViewCell extends ListCell<Person> {


    protected AnimationPack anim;

    @Override
    public void updateItem(Person person, boolean empty) {
        super.updateItem(person, empty);

        if (getHeight() == 0.0){
            this.setMaxHeight(80.0);
            this.setMinHeight(80.0);
            this.setPrefHeight(80.0);
        }

        if (empty || person == null) {
/*
            FadeTransition ft = new FadeTransition(Duration.millis(3000), this);
            ft.setFromValue(1.0);
            ft.setToValue(0.1);
            ft.setCycleCount(1);
            ft.play();
            ft.setOnFinished(value -> {
                setGraphic(null);
                setText(null);
            });
*/

            //setEffect(new BoxBlur(10,10,3));
            setGraphic(null);
            setText(null);

        }
        else
        {
            //this.setMaxHeight(80.0);
            //this.setMinHeight(80.0);
           // this.setPrefHeight(80.0);
            setGraphic(new PersonCardController(person, this).getLayout());
            //setText(null);
            //animate();
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

