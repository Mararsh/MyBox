package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageMosaic;
import mara.mybox.bufferedimage.ImageScope;
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
public class ImageGlassController extends BasePixelsController {

    protected int intensity;

    @FXML
    protected ComboBox<String> intensitySelector;

    public ImageGlassController() {
        baseTitle = message("FrostedGlass");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            if (editor == null) {
                close();
                return;
            }
            intensity = UserConfig.getInt(baseName + "Intensity", 80);
            if (intensity <= 0) {
                intensity = 80;
            }
            intensitySelector.getItems().addAll(Arrays.asList("80", "20", "50", "10", "5", "100", "15", "20", "60"));
            intensitySelector.getSelectionModel().select(intensity + "");
            intensitySelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v > 0) {
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
            ImageMosaic mosaic = ImageMosaic.create(
                    inImage, inScope,
                    ImageMosaic.MosaicType.FrostedGlass, intensity);
            mosaic.setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent());
            operation = message("FrostedGlass");
            opInfo = message("Intensity") + ": " + intensity;
            return mosaic.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    /*
        static methods
     */
    public static ImageGlassController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageGlassController controller = (ImageGlassController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageGlassFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
