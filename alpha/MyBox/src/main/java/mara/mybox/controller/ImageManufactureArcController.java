package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.controller.ImageManufactureController_Image.ImageOperation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.ValidationTools;
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
    protected ControlColorSet colorSetController;

    @Override
    public void initPane() {
        try {
            super.initPane();

            colorSetController.init(this, baseName + "Color");

            arc = UserConfig.getInt(baseName + "Arc", 20);
            int width = (int) imageView.getImage().getWidth();
            arcBox.getItems().addAll(Arrays.asList(
                    width / 6 + "", width / 8 + "", width / 4 + "", width / 10 + "",
                    "0", "15", "30", "50", "150", "300", "10", "3"));
            arcBox.setValue(arc + "");
            arcBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            arc = v;
                            UserConfig.setInt(baseName + "Arc", arc);
                            ValidationTools.setEditorNormal(arcBox);
                        } else {
                            ValidationTools.setEditorBadStyle(arcBox);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(arcBox);
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
        imageController.imageTab();
    }

    @FXML
    @Override
    public void okAction() {
        if (arc <= 0) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

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
        start(task);
    }
}
