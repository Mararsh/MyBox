package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-1
 * @License Apache License Version 2.0
 */
public class ImageReplaceColorController extends ImageSelectScopeController {

    @FXML
    protected CheckBox hueCheck, saturationCheck, brightnessCheck;
    @FXML
    protected ControlColorSet colorController;

    public ImageReplaceColorController() {
        baseTitle = message("ReplaceColor");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();

            colorController.init(this, baseName + "NewColor", Color.PINK);

            hueCheck.setSelected(UserConfig.getBoolean(baseName + "ReplaceHue", false));
            hueCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                    UserConfig.setBoolean(baseName + "ReplaceHue", hueCheck.isSelected());
                }
            });

            saturationCheck.setSelected(UserConfig.getBoolean(baseName + "ReplaceSaturation", false));
            saturationCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                    UserConfig.setBoolean(baseName + "ReplaceSaturation", saturationCheck.isSelected());
                }
            });

            brightnessCheck.setSelected(UserConfig.getBoolean(baseName + "ReplaceBrightness", false));
            brightnessCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) {
                    UserConfig.setBoolean(baseName + "ReplaceBrightness", brightnessCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @FXML
    @Override
    public void okAction() {
        if (!checkOptions()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            private ImageScope scope;

            @Override
            protected boolean handle() {
                try {
                    scope = scope();
                    PixelsOperation pixelsOperation = PixelsOperationFactory.create(
                            editor.imageView.getImage(),
                            scope,
                            PixelsOperation.OperationType.Color,
                            PixelsOperation.ColorActionType.Set)
                            .setColorPara1(colorController.awtColor())
                            .setBoolPara1(hueCheck.isSelected())
                            .setBoolPara2(saturationCheck.isSelected())
                            .setBoolPara3(brightnessCheck.isSelected())
                            .setExcludeScope(excludeRadio.isSelected())
                            .setSkipTransparent(ignoreTransparentCheck.isSelected());
                    handledImage = pixelsOperation.operateFxImage();
                    return handledImage != null;
                } catch (Exception e) {
                    MyBoxLog.debug(e);
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                editor.updateImage("ReplaceColor", null, scope, handledImage, cost);
                if (closeAfterCheck.isSelected()) {
                    close();
                }
            }
        };
        start(task);
    }

    @Override
    protected Image makeDemo(BufferedImage dbf, ImageScope scope) {
        try {
            PixelsOperation pixelsOperation = PixelsOperationFactory.create(
                    dbf, scope,
                    PixelsOperation.OperationType.Color,
                    PixelsOperation.ColorActionType.Set)
                    .setColorPara1(colorController.awtColor())
                    .setBoolPara1(true)
                    .setBoolPara2(false)
                    .setBoolPara3(false)
                    .setSkipTransparent(ignoreTransparentCheck.isSelected());
            return pixelsOperation.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    /*
        static methods
     */
    public static ImageReplaceColorController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageReplaceColorController controller = (ImageReplaceColorController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageReplaceColorFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
