package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import mara.mybox.controller.ImageManufactureController.ImageOperation;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.fxml.FxmlImageManufacture;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;

/**
 * @Author Mara
 * @CreateDate 2019-9-5
 * @License Apache License Version 2.0
 */
public class ImageManufactureArcController extends ImageManufactureOperationController {

    protected int arc;

    @FXML
    protected ComboBox arcBox;
    @FXML
    protected ColorSetController colorSetController;

    @Override
    public void initPane() {
        try {
            colorSetController.init(this, baseName + "Color");

            arcBox.getItems().clear();
            int width = (int) imageView.getImage().getWidth();
            arcBox.getItems().addAll(Arrays.asList(
                    width / 6 + "", width / 8 + "", width / 4 + "", width / 10 + "",
                    "0", "15", "30", "50", "150", "300", "10", "3"));
            arcBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            arc = v;
                            FxmlControl.setEditorNormal(arcBox);
                            AppVariables.setUserConfigValue("ImageArcSize", newValue);
                        } else {
                            arc = 0;
                            FxmlControl.setEditorBadStyle(arcBox);
                        }
                    } catch (Exception e) {
                        arc = 0;
                        FxmlControl.setEditorBadStyle(arcBox);
                    }
                }
            });
            arcBox.getSelectionModel().select(AppVariables.getUserConfigInt("ImageArcSize", 20) + "");

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @Override
    protected void paneExpanded() {
        imageController.showImagePane();
        imageController.hideScopePane();
    }

    @FXML
    @Override
    public void okAction() {
        if (arc <= 0) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit() ) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = FxmlImageManufacture.addArc(imageView.getImage(), arc, (Color) colorSetController.rect.getFill());
                    if (task == null || isCancelled()) {
                        return false;
                    }
                    return newImage != null;
                }

                @Override
                protected void whenSucceeded() {
                    imageController.popSuccessful();
                    imageController.updateImage(ImageOperation.Arc, arc + "", null, newImage, cost);
                }

            };
            imageController.openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }
}
