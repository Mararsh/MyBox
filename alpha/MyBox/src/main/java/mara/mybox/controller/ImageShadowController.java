package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.paint.Color;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.ValidationTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-5
 * @License Apache License Version 2.0
 */
public class ImageShadowController extends BaseImageEditController {

    protected int size;

    @FXML
    protected ComboBox sizeSelector;
    @FXML
    protected ControlColorSet colorController;

    public ImageShadowController() {
        baseTitle = message("Shadow");
    }

    @Override
    protected void initMore() {
        try {
            operation = message("Shadow");

            colorController.init(this, baseName + "Color", Color.BLACK);

            sizeSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        int v = Integer.parseInt(newValue);
                        if (v >= 0) {
                            size = v;
                            UserConfig.setString("ImageShadowSize", newValue);
                            ValidationTools.setEditorNormal(sizeSelector);
                        } else {
                            ValidationTools.setEditorBadStyle(sizeSelector);
                        }
                    } catch (Exception e) {
                        ValidationTools.setEditorBadStyle(sizeSelector);
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    public boolean afterImageLoaded() {
        try {
            if (!super.afterImageLoaded() || image == null) {
                return false;
            }
            size = UserConfig.getInt(baseName + "Size", 20);
            if (size < 0) {
                size = 0;
            }
            int width = (int) imageView.getImage().getWidth();
            List<String> values = new ArrayList<>();
            values.addAll(Arrays.asList(
                    width / 100 + "",
                    width / 50 + "",
                    width / 200 + "",
                    width / 30 + "",
                    "10", "5", "15", "3", "8", "1", "6", "20", "25"));
            String v = size + "";
            if (!values.contains(v)) {
                values.add(0, v);
            }
            isSettingValues = true;
            sizeSelector.getItems().setAll(values);
            sizeSelector.setValue(v);
            isSettingValues = false;

            return true;
        } catch (Exception e) {
            MyBoxLog.debug(e);
            return false;
        }
    }

    @Override
    protected void handleImage() {
        Color c = colorController.color();
        opInfo = size + " " + c;
        handledImage = FxImageTools.addShadowAlpha(task, srcImage(), size, c);
    }

    /*
        static methods
     */
    public static ImageShadowController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageShadowController controller = (ImageShadowController) WindowTools.branch(
                    parent.getMyWindow(), Fxmls.ImageShadowFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
