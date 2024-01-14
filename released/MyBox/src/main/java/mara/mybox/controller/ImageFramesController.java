package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-7-19
 * @License Apache License Version 2.0
 */
public class ImageFramesController extends BaseChildController {

    protected BaseImageController imageController;

    @FXML
    protected ComboBox<String> frameSelector;
    @FXML
    protected Label framesLabel;
    @FXML
    protected Button nextFrameButton, previousFrameButton, playFramesButton;

    public ImageFramesController() {
        baseTitle = message("Frames");
    }

    public void setParameters(BaseImageController parent) {
        try {
            if (parent == null) {
                close();
                return;
            }
            imageController = parent;
            baseName = imageController.baseName;

            update();

            frameSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (isSettingValues) {
                            return;
                        }
                        int v = Integer.parseInt(frameSelector.getValue());
                        if (v < 1 || v > imageController.framesNumber) {
                            frameSelector.getEditor().setStyle(UserConfig.badStyle());
                        } else {
                            frameSelector.getEditor().setStyle(null);
                            imageController.loadFrame(v - 1);
                        }
                    } catch (Exception e) {
                        frameSelector.getEditor().setStyle(UserConfig.badStyle());
                    }
                }
            });

            imageController.loadNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    update();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void update() {
        try {
            isSettingValues = true;
            List<String> frames = new ArrayList<>();
            for (int i = 1; i <= imageController.framesNumber; i++) {
                frames.add(i + "");
            }
            frameSelector.getItems().setAll(frames);
            frameSelector.setValue((imageController.frameIndex + 1) + "");
            framesLabel.setText("/" + imageController.framesNumber);
            nextFrameButton.setDisable(imageController.framesNumber < 2);
            previousFrameButton.setDisable(imageController.framesNumber < 2);
            playFramesButton.setDisable(imageController.framesNumber < 2);
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void editFrames() {
        imageController.editFrames();
    }

    @FXML
    @Override
    public void playAction() {
        imageController.playAction();
    }

    @FXML
    public void nextFrame() {
        imageController.loadFrame(imageController.frameIndex + 1);
    }

    @FXML
    public void previousFrame() {
        imageController.loadFrame(imageController.frameIndex - 1);
    }

    /*
        static methods
     */
    public static ImageFramesController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageFramesController controller = (ImageFramesController) WindowTools.branchStage(
                    parent, Fxmls.ImageFramesFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
