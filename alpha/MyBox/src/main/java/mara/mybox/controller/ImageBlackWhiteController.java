package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import mara.mybox.bufferedimage.ImageBinary;
import mara.mybox.bufferedimage.ImageScope;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
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
public class ImageBlackWhiteController extends BasePixelsController {

    protected ImageBinary imageBinary;

    @FXML
    protected ControlImageBinary binaryController;

    public ImageBlackWhiteController() {
        baseTitle = message("BlackOrWhite");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();

            operation = message("BlackOrWhite");

            binaryController.setParameters(imageController.imageView);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions()) {
            return false;
        }
        imageBinary = binaryController.pickValues();
        if (imageBinary == null) {
            return false;
        }
        if (imageBinary.getAlgorithm() != ImageBinary.BinaryAlgorithm.Default) {
            opInfo = message("Threshold") + ": " + imageBinary.getIntPara1();
        }
        return true;
    }

    @Override
    protected Image handleImage(FxTask currentTask, Image inImage, ImageScope inScope) {
        try {
            imageBinary.setImage(inImage)
                    .setScope(inScope)
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent())
                    .setTask(currentTask);
            return imageBinary.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    @Override
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
        try {
            ImageBinary binary = new ImageBinary(demoImage);
            binary.setScope(scope())
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent());
            String prefix = message("BlackOrWhite");

            BufferedImage bufferedImage = binary
                    .setAlgorithm(ImageBinary.BinaryAlgorithm.Default)
                    .setIsDithering(true)
                    .operate();
            String tmpFile = FileTmpTools.generateFile(prefix + "_" + message("Default")
                    + "_" + message("Dithering"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(currentTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                currentTask.setInfo(tmpFile);
            }
            if (currentTask == null || !currentTask.isWorking()) {
                return;
            }

            bufferedImage = binary
                    .setAlgorithm(ImageBinary.BinaryAlgorithm.Default)
                    .setIsDithering(false)
                    .operate();
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("Default"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(currentTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                currentTask.setInfo(tmpFile);
            }
            if (currentTask == null || !currentTask.isWorking()) {
                return;
            }

            List<Integer> inputs = new ArrayList<>();
            inputs.addAll(Arrays.asList(64, 96, 112, 128, 144, 160, 176, 198, 228));
            int input = binaryController.threshold;
            if (input > 0 && input < 255 && !inputs.contains(input)) {
                inputs.add(input);
            }
            for (int v : inputs) {
                if (currentTask == null || !currentTask.isWorking()) {
                    return;
                }
                bufferedImage = binary
                        .setAlgorithm(ImageBinary.BinaryAlgorithm.Threshold)
                        .setIntPara1(v)
                        .setIsDithering(true)
                        .setTask(currentTask)
                        .operate();
                tmpFile = FileTmpTools.generateFile(prefix + "_" + message("Threshold") + v
                        + "_" + message("Dithering"), "png").getAbsolutePath();
                if (ImageFileWriters.writeImageFile(currentTask, bufferedImage, "png", tmpFile)) {
                    files.add(tmpFile);
                    currentTask.setInfo(tmpFile);
                }

                bufferedImage = binary
                        .setAlgorithm(ImageBinary.BinaryAlgorithm.Threshold)
                        .setIntPara1(v)
                        .setIsDithering(false)
                        .setTask(currentTask)
                        .operate();
                tmpFile = FileTmpTools.generateFile(prefix + "_" + message("Threshold") + v, "png").getAbsolutePath();
                if (ImageFileWriters.writeImageFile(currentTask, bufferedImage, "png", tmpFile)) {
                    files.add(tmpFile);
                    currentTask.setInfo(tmpFile);
                }
            }

            int otsu = ImageBinary.threshold(currentTask, srcImage());
            bufferedImage = binary
                    .setAlgorithm(ImageBinary.BinaryAlgorithm.Threshold)
                    .setIntPara1(otsu)
                    .setIsDithering(true)
                    .setTask(currentTask)
                    .operate();
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("OTSU")
                    + otsu + "_" + message("Dithering"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(currentTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                currentTask.setInfo(tmpFile);
            }
            if (currentTask == null || !currentTask.isWorking()) {
                return;
            }

            bufferedImage = binary
                    .setAlgorithm(ImageBinary.BinaryAlgorithm.Threshold)
                    .setIntPara1(otsu)
                    .setIsDithering(false)
                    .setTask(currentTask)
                    .operate();
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("OTSU") + otsu, "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(currentTask, bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                currentTask.setInfo(tmpFile);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }


    /*
        static methods
     */
    public static ImageBlackWhiteController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageBlackWhiteController controller = (ImageBlackWhiteController) WindowTools.branchStage(
                    parent, Fxmls.ImageBlackWhiteFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
