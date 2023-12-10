package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.TransformTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageShearController extends BaseImageEditController {

    protected float shearX, shearY;

    @FXML
    protected ComboBox xSelector, ySelector;

    public ImageShearController() {
        baseTitle = message("Shear");
    }

    @Override
    protected void initMore() {
        try {
            operation = message("Shear");

            shearX = UserConfig.getFloat("ImageShearX", 0.5f);
            xSelector.getItems().addAll(Arrays.asList(
                    "0.5", "-0.5", "0", "0.4", "-0.4", "0.2", "-0.2", "0.1", "-0.1",
                    "0.7", "-0.7", "0.9", "-0.9", "0.8", "-0.8", "1", "-1",
                    "1.5", "-1.5", "2", "-2"));
            xSelector.setValue(shearX + "");
            xSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        shearX = Float.parseFloat(newValue);
                        UserConfig.setFloat("ImageShearX", shearX);
                        ValidationTools.setEditorNormal(xSelector);
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(xSelector);
                    }
                }
            });

            shearY = UserConfig.getFloat("ImageShearY", 0f);
            ySelector.getItems().addAll(Arrays.asList(
                    "0", "0.5", "-0.5", "0.4", "-0.4", "0.2", "-0.2", "0.1", "-0.1",
                    "0.7", "-0.7", "0.9", "-0.9", "0.8", "-0.8", "1", "-1",
                    "1.5", "-1.5", "2", "-2"));
            ySelector.setValue(shearY + "");
            ySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        shearY = Float.parseFloat(newValue);
                        UserConfig.setFloat("ImageShearY", shearY);
                        ValidationTools.setEditorNormal(ySelector);
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(ySelector);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    protected void handleImage() {
        handledImage = TransformTools.shearImage(task, imageView.getImage(), shearX, shearY);
    }

    /*
        static methods
     */
    public static ImageShearController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageShearController controller = (ImageShearController) WindowTools.branch(
                    parent.getMyWindow(), Fxmls.ImageShearFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
