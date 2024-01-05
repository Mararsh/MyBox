package mara.mybox.controller;

import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsOperation;
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
import mara.mybox.fximage.ColorDemos;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
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
    protected void initMore() {
        try {
            super.initMore();

            operation = message("AdjustColor");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected Image handleImage(FxTask currentTask, Image inImage, ImageScope inScope) {
        try {
            opInfo = message(optionsController.colorOperationType.name()) + " "
                    + message(optionsController.colorActionType.name());
            if (optionsController.needValue()) {
                opInfo += ": " + optionsController.colorValue;
            }
            PixelsOperation pixelsOperation = PixelsOperationFactory.createFX(
                    inImage, inScope,
                    optionsController.colorOperationType,
                    optionsController.colorActionType)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(currentTask);
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
            return pixelsOperation.startFx();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    @Override
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image inImage) {
        ColorDemos.adjustColor(currentTask, files,
                SwingFXUtils.fromFXImage(inImage, null), scope(), srcFile());
    }

    /*
        static methods
     */
    public static ImageAdjustColorController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageAdjustColorController controller = (ImageAdjustColorController) WindowTools.branchStage(
                    parent, Fxmls.ImageAdjustColorFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
