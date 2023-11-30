package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
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
            opInfo = message(optionsController.colorActionType.name()) + ": "
                    + optionsController.colorValue;
            PixelsOperation pixelsOperation = PixelsOperationFactory.createFX(
                    inImage, inScope,
                    optionsController.colorOperationType,
                    optionsController.colorActionType)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent());
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
    protected List<String> makeDemoFiles(Image inImage) {
        try {
            List<String> files = new ArrayList<>();
            BufferedImage demoImage = SwingFXUtils.fromFXImage(inImage, null);

            PixelsOperation pixelsOperation;
            BufferedImage bufferedImage;
            String tmpFile;

            scope = scope();

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.Color, ColorActionType.Set);
            pixelsOperation.setColorPara1(ColorConvertTools.converColor(Color.LIGHTPINK))
                    .setBoolPara1(true).setBoolPara2(false).setBoolPara3(false);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Color") + "_" + message("Filter"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.Brightness, ColorActionType.Increase);
            pixelsOperation.setFloatPara1(0.5f);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Brightness") + "_" + message("Increase"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.Hue, ColorActionType.Decrease);
            pixelsOperation.setFloatPara1(0.3f);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Hue") + "_" + message("Decrease"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.Saturation, ColorActionType.Increase);
            pixelsOperation.setFloatPara1(0.5f);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Saturation") + "_" + message("Increase"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.Opacity, ColorActionType.Decrease);
            pixelsOperation.setIntPara1(128);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Opacity") + "_" + message("Decrease"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.RGB, ColorActionType.Invert);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("RGB") + "_" + message("Invert"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.Red, ColorActionType.Filter);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Red") + "_" + message("Filter"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.Yellow, ColorActionType.Increase);
            pixelsOperation.setIntPara1(60);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Yellow") + "_" + message("Increase"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
                files.add(tmpFile);
                task.setInfo(tmpFile);
            }

            pixelsOperation = PixelsOperationFactory.create(demoImage,
                    scope, OperationType.Magenta, ColorActionType.Decrease);
            pixelsOperation.setIntPara1(60);
            bufferedImage = pixelsOperation.operate();
            tmpFile = FileTmpTools.generateFile(message("Magenta") + "_" + message("Decrease"), "png")
                    .getAbsolutePath();
            if (ImageFileWriters.writeImageFile(bufferedImage, tmpFile)) {
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
    public static ImageAdjustColorController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageAdjustColorController controller = (ImageAdjustColorController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageAdjustColorFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
