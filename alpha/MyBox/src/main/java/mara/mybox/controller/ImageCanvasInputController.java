package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.image.FxImageTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-10-25
 * @License Apache License Version 2.0
 */
public class ImageCanvasInputController extends BaseInputController {

    protected int width, height;
    protected Image canvas;

    @FXML
    protected TextField widthInput, heightInput;
    @FXML
    protected ControlColorSet colorController;

    @Override
    public void setParameters(BaseController parent, String title) {
        try {
            super.setParameters(parent, title);

            width = UserConfig.getInt(baseName + "CanvasWidth", 500);
            if (width <= 0) {
                width = 500;
            }
            widthInput.setText(width + "");
            widthInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> vv, String ov, String nv) {
                    try {
                        int v = Integer.parseInt(nv);
                        if (v > 0) {
                            width = v;
                            widthInput.setStyle(null);
                            UserConfig.setInt(baseName + "CanvasWidth", width);
                        } else {
                            widthInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        widthInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

            height = UserConfig.getInt(baseName + "CanvasHeight", 500);
            if (height <= 0) {
                height = 500;
            }
            heightInput.setText(height + "");
            heightInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> vv, String ov, String nv) {
                    try {
                        int v = Integer.parseInt(nv);
                        if (v > 0) {
                            height = v;
                            heightInput.setStyle(null);
                            UserConfig.setInt(baseName + "CanvasHeight", height);
                        } else {
                            heightInput.setStyle(UserConfig.badStyle());
                        }
                    } catch (Exception e) {
                        heightInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

            colorController.init(this, baseName + "CanvasColor");

            okButton.disableProperty().bind(widthInput.styleProperty().isEqualTo(UserConfig.badStyle())
                    .or(heightInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e);

        }
    }

    @Override
    public boolean checkInput() {
        canvas = FxImageTools.createImage(width, height, (Color) colorController.rect.getFill());
        return true;
    }

    public Image getCanvas() {
        return canvas;
    }

    public static ImageCanvasInputController open(BaseController parent, String title) {
        try {
            ImageCanvasInputController controller = (ImageCanvasInputController) WindowTools.childStage(
                    parent, Fxmls.ImageCanvasInputFxml);
            controller.setParameters(parent, title);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
