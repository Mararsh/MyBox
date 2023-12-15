package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.bufferedimage.PixelsOperation;
import mara.mybox.bufferedimage.PixelsOperationFactory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-2
 * @License Apache License Version 2.0
 */
public class ImageSepiaController extends BasePixelsController {

    protected PixelsOperation pixelsOperation;

    @FXML
    protected ControlImageSepia sepiaController;

    public ImageSepiaController() {
        baseTitle = message("Sepia");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Sepia");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        pixelsOperation = sepiaController.pickValues();
        return pixelsOperation != null;
    }

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            pixelsOperation.setImage(inImage)
                    .setScope(inScope)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(task);
            opInfo = message("Intensity") + ": " + sepiaController.intensity;
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
            BufferedImage bufferedImage;
            String tmpFile;
            PixelsOperation pixelsOperation = PixelsOperationFactory.create(
                    demoImage, scope(), PixelsOperation.OperationType.Sepia)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(demoTask);
            List<Integer> values = Arrays.asList(60, 80, 20, 50, 10, 5, 100, 15, 20);
            for (int v : values) {
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                bufferedImage = pixelsOperation.setIntPara1(v).operate();
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                tmpFile = FileTmpTools.generateFile(message("Sepia") + "_" + message("Intensity") + v, "png")
                        .getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
            ImageSepiaController controller = (ImageSepiaController) WindowTools.branchStage(
                    parent, Fxmls.ImageSepiaFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
