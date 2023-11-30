package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
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
import mara.mybox.fxml.WindowTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-1
 * @License Apache License Version 2.0
 */
public class ImageReplaceColorController extends BasePixelsController {

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

            hueCheck.setSelected(UserConfig.getBoolean(baseName + "ReplaceHue", true));
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

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            PixelsOperation pixelsOperation = PixelsOperationFactory.createFX(
                    inImage, inScope, PixelsOperation.OperationType.Color)
                    .setColorPara1(colorController.awtColor())
                    .setBoolPara1(hueCheck.isSelected())
                    .setBoolPara2(saturationCheck.isSelected())
                    .setBoolPara3(brightnessCheck.isSelected())
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent());
            operation = message("ReplaceColor");
            opInfo = colorController.css();
            return pixelsOperation.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    @Override
    protected List<String> makeDemoFiles(Image demoImage) {
        try {
            List<String> files = new ArrayList<>();
            PixelsOperation pixelsOperation = PixelsOperationFactory.createFX(
                    demoImage, scope(), PixelsOperation.OperationType.Color)
                    .setColorPara1(colorController.awtColor())
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent());
            String prefix = message("ReplaceColor") + "_" + colorController.css();

            BufferedImage bufferedImage = pixelsOperation
                    .setBoolPara1(true).setBoolPara2(false).setBoolPara3(false).operate();
            String tmpFile = FileTmpTools.generateFile(prefix + "_" + message("Hue"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }

            bufferedImage = pixelsOperation
                    .setBoolPara1(false).setBoolPara2(true).setBoolPara3(false).operate();
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("Saturation"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }

            bufferedImage = pixelsOperation
                    .setBoolPara1(false).setBoolPara2(false).setBoolPara3(true).operate();
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("Brightness"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }

            bufferedImage = pixelsOperation
                    .setBoolPara1(false).setBoolPara2(true).setBoolPara3(false).operate();
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("All"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }

            return files;
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
