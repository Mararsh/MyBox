package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageSepiaController extends BasePixelsController {

    protected int intensity;

    @FXML
    protected ComboBox<String> intensitySelector;

    public ImageSepiaController() {
        baseTitle = message("Grey");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            if (editor == null) {
                close();
                return;
            }
            intensity = UserConfig.getInt(baseName + "Intensity", 60);
            if (intensity <= 0 || intensity >= 255) {
                intensity = 60;
            }
            intensitySelector.getItems().addAll(Arrays.asList("60", "80", "20", "50", "10", "5", "100", "15", "20"));
            intensitySelector.getSelectionModel().select(intensity + "");
            intensitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0 && v < 255) {
                            intensity = v;
                            UserConfig.setInt(baseName + "Intensity", intensity);
                            ValidationTools.setEditorNormal(intensitySelector);
                        } else {
                            ValidationTools.setEditorBadStyle(intensitySelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(intensitySelector);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        if (intensity > 0) {
            return true;
        } else {
            popError(message("InvalidParameter"));
            return false;
        }
    }

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            PixelsOperation pixelsOperation = PixelsOperationFactory.createFX(
                    inImage, inScope,
                    PixelsOperation.OperationType.Sepia)
                    .setIntPara1(intensity)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent());
            operation = message("Sepia");
            opInfo = message("Intensity") + ": " + intensity;
            return pixelsOperation.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    /*
        static methods
     */
    public static ImageSepiaController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageSepiaController controller = (ImageSepiaController) WindowTools.branch(
                    parent.getMyWindow(), Fxmls.ImageSepiaFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
