package mara.mybox.controller;

import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.image.ColorDemos;
import mara.mybox.image.data.ImageScope;
import mara.mybox.image.data.PixelsBlend;
import mara.mybox.image.data.PixelsOperation;
import mara.mybox.image.data.PixelsOperationFactory;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-1
 * @License Apache License Version 2.0
 */
public class ImageBlendColorController extends BasePixelsController {

    protected PixelsBlend blend;

    @FXML
    protected ControlColorSet colorController;
    @FXML
    protected ControlColorsBlend blendController;

    public ImageBlendColorController() {
        baseTitle = message("BlendColor");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();

            operation = message("BlendColor");

            colorController.init(this, baseName + "Color");

            blendController.setParameters(this);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        if (!super.checkOptions()) {
            close();
            return false;
        }
        blend = blendController.pickValues(-1f);
        opInfo = colorController.css();
        return blend != null;
    }

    @Override
    protected Image handleImage(FxTask currentTask, Image inImage, ImageScope inScope) {
        try {
            PixelsOperation pixelsOperation = PixelsOperationFactory.createFX(
                    inImage, inScope, PixelsOperation.OperationType.Blend)
                    .setColorPara1(colorController.awtColor())
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(currentTask);
            ((PixelsOperationFactory.BlendColor) pixelsOperation).setBlender(blend);
            return pixelsOperation.startFx();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    @Override
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
        ColorDemos.blendColor(currentTask, files,
                SwingFXUtils.fromFXImage(demoImage, null),
                colorController.color(), srcFile());
    }


    /*
        static methods
     */
    public static ImageBlendColorController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageBlendColorController controller = (ImageBlendColorController) WindowTools.referredStage(
                    parent, Fxmls.ImageBlendColorFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
