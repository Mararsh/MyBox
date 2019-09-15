package mara.mybox.controller;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2019-9-5
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageController extends ImageViewerController {

    @FXML
    protected CheckBox topCheck;

    public ImageController() {
        baseTitle = AppVariables.message("Image");
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            topCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    myStage.setAlwaysOnTop(topCheck.isSelected());
                }
            });

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void toFront() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        myStage.toFront();
                        imageView.requestFocus();
                    }
                });
            }
        }, 1000);
    }

}
