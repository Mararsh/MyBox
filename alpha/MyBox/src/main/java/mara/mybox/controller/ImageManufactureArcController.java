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
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.UserConfig;

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
    protected ColorSet colorSetController;

    @Override
    public void initPane() {
        try {
            colorSetController.init(this, baseName + "Color");

            arc = UserConfig.getUserConfigInt(baseName + "Arc", 20);
            int width = (int) imageView.getImage().getWidth();
            arcBox.getItems().addAll(Arrays.asList(
                    width / 6 + "", width / 8 + "", width / 4 + "", width / 10 + "",
                    "0", "15", "30", "50", "150", "300", "10", "3"));
            arcBox.setValue(arc + "");
            arcBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.valueOf(newValue);
                        if (v >= 0) {
                            arc = v;
                            UserConfig.setUserConfigInt(baseName + "Arc", arc);
                            NodeTools.setEditorNormal(arcBox);
                        } else {
                            NodeTools.setEditorBadStyle(arcBox);
                        }
                    } catch (Exception e) {
                        NodeTools.setEditorBadStyle(arcBox);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @Override
    protected void paneExpanded() {
        imageController.showRightPane();
        imageController.resetImagePane();
        imageController.hideScopePane();
        imageController.showImagePane();
    }

    @FXML
    @Override
    public void okAction() {
        if (arc <= 0) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private Image newImage;

                @Override
                protected boolean handle() {
                    newImage = FxImageTools.addArc(imageView.getImage(), arc, (Color) colorSetController.rect.getFill());
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
            imageController.handling(task);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }
}
