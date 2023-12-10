package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.ColorConvertTools;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperation.ColorActionType;
import mara.mybox.bufferedimage.PixelsOperation.OperationType;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Blue;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Brightness;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Cyan;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Green;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Hue;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Magenta;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Opacity;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.RGB;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Red;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Saturation;
import static mara.mybox.bufferedimage.PixelsOperation.OperationType.Yellow;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-1
 * @License Apache License Version 2.0
 */
public class ImageAdjustColorController extends BasePixelsController {

    @FXML
    protected ControlImageAdjustColor optionsController;

    public ImageAdjustColorController() {
        baseTitle = message("AdjustColor");
    }

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            operation = message("AdjustColor");
            opInfo = message(optionsController.colorActionType.name());
            if (optionsController.needValue()) {
                opInfo += ": " + optionsController.colorValue;
            }
            PixelsOperation pixelsOperation = PixelsOperationFactory.createFX(
                    inImage, inScope,
                    optionsController.colorOperationType,
                    optionsController.colorActionType)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(task);
            switch (optionsController.colorOperationType) {
                case Hue:
                    pixelsOperation.setFloatPara1(optionsController.colorValue / 360.0f);
                    break;
                case Brightness:
                case Saturation:
                    pixelsOperation.setFloatPara1(optionsController.colorValue / 100.0f);
                    break;
                case Red:
                case Green:
                case Blue:
                case Yellow:
                case Cyan:
                case Magenta:
                case RGB:
                case Opacity:
                    pixelsOperation.setIntPara1(optionsController.colorValue);
                    break;
            }
            return pixelsOperation.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    @Override
    protected void makeDemoFiles(List<String> files, Image inImage) {
        try {
            BufferedImage demoImage = SwingFXUtils.fromFXImage(inImage, null);

            PixelsOperation pixelsOperation;
            BufferedImage bufferedImage;
            String tmpFile;

            scope = scope();

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.Color, ColorActionType.Set);
            pixelsOperation.setColorPara1(ColorConvertTools.converColor(Color.LIGHTPINK))
                    .setBoolPara1(true).setBoolPara2(false).setBoolPara3(false)
                    .setTask(demoTask);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Color") + "_" + message("Filter"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.Brightness, ColorActionType.Increase)
                    .setFloatPara1(0.5f).setTask(demoTask);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Brightness") + "_" + message("Increase"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.Hue, ColorActionType.Decrease)
                    .setFloatPara1(0.3f).setTask(demoTask);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Hue") + "_" + message("Decrease"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.Saturation, ColorActionType.Increase)
                    .setFloatPara1(0.5f).setTask(demoTask);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Saturation") + "_" + message("Increase"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.Opacity, ColorActionType.Decrease)
                    .setIntPara1(128).setTask(demoTask);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Opacity") + "_" + message("Decrease"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (demoTask == null || !demoTask.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.RGB, ColorActionType.Invert)
                    .setTask(demoTask);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("RGB") + "_" + message("Invert"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (demoTask != null && !demoTask.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.Red, ColorActionType.Filter)
                    .setTask(demoTask);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Red") + "_" + message("Filter"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (demoTask != null && !demoTask.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.Yellow, ColorActionType.Increase)
                    .setIntPara1(60).setTask(demoTask);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Yellow") + "_" + message("Increase"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }
            if (demoTask != null && !demoTask.isWorking()) {
                return;
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.Magenta, ColorActionType.Decrease)
                    .setIntPara1(60).setTask(demoTask);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Magenta") + "_" + message("Decrease"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    /*
        static methods
     */
    public static ImageAdjustColorController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageAdjustColorController controller = (ImageAdjustColorController) WindowTools.branch(
                    parent.getMyWindow(), Fxmls.ImageAdjustColorFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
