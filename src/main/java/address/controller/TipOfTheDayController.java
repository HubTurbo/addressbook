package address.controller;

import address.MainApp;
import address.util.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.WritableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Controls what, when and how to show tip of the day
 */
public class TipOfTheDayController {
    private static final AppLogger logger = LoggerManager.getLogger(TipOfTheDayController.class);

    private static final String FXML_TIP_OF_THE_DAY = "/view/TipOfTheDay.fxml";
    private static final String TIPS_OF_THE_DAY_RESOURCE = "/tipsOfTheDay.json";

    private static final double POS_Y_OFFSET = 100;
    private static final double ANIM_SLIDE_IN_DISTANCE = 300;
    private static final double ANIM_SLIDE_IN_DURATION = 1500;

    private Stage owner;

    public TipOfTheDayController(Stage owner) {
        this.owner = owner;
    }

    public void start() {
        // TODO put scheduling of showing tip of the day here
        getTipOfTheDay().ifPresent(this::displayTipOfTheDay);
    }

    private void displayTipOfTheDay(String tipOfTheDay) {
        final String fxmlResourcePath = FXML_TIP_OF_THE_DAY;
        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(MainApp.class.getResource(fxmlResourcePath));
            VBox tipOfTheDayLayout = loader.load();

            Label content = (Label) tipOfTheDayLayout.lookup("#content");
            content.setText(tipOfTheDay);

            Popup tipOfTheDayPopup = new Popup();
            tipOfTheDayPopup.getContent().add(tipOfTheDayLayout);
            tipOfTheDayPopup.setX(owner.getX() + owner.getWidth());
            tipOfTheDayPopup.setY(owner.getY() + POS_Y_OFFSET);

            double screenRightEdge = owner.getX() + owner.getWidth();

            Timeline timeline = new Timeline();

            WritableValue<Double> writableWidth = new WritableValue<Double>() {
                @Override
                public Double getValue() {
                    return tipOfTheDayPopup.getWidth();
                }

                @Override
                public void setValue(Double value) {
                    tipOfTheDayPopup.setX(screenRightEdge - value);
                    tipOfTheDayPopup.setWidth(value);
                }
            };

            KeyValue kv = new KeyValue(writableWidth, ANIM_SLIDE_IN_DISTANCE);
            KeyFrame kf = new KeyFrame(Duration.millis(ANIM_SLIDE_IN_DURATION), kv);
            timeline.getKeyFrames().addAll(kf);
            timeline.play();

            tipOfTheDayPopup.show(owner);

            tipOfTheDayPopup.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> tipOfTheDayPopup.hide());
        } catch (IOException e) {
            assert false; // FXML should exist
        }
    }

    private Optional<String> getTipOfTheDay() {
        try {
            TipsOfTheDay tipsOfTheDay = JsonUtil.fromJsonString(
                    FileUtil.readFromInputStream(this.getClass().getResourceAsStream(TIPS_OF_THE_DAY_RESOURCE)),
                    TipsOfTheDay.class);
            return tipsOfTheDay.getATipOfTheDay();
        } catch (IOException e) {
            logger.debug("Failed to get tip of the day", e);
        }

        return Optional.empty();
    }
}
