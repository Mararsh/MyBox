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

    protected int threshold;

    @FXML
    protected ControlImageBinary binaryController;

    public ImageBlackWhiteController() {
        baseTitle = message("BlackOrWhite");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();

            binaryController.setParameters(editor.imageView);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected Image handleImage(Image inImage, ImageScope inScope) {
        try {
            operation = message("BlackOrWhite");
            opInfo = message("Threshold") + ": " + threshold;
            threshold = binaryController.threshold();
            ImageBinary imageBinary = new ImageBinary(inImage);
            imageBinary.setScope(inScope)
                    .setIntPara1(threshold)
                    .setIsDithering(binaryController.dither())
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent());
            return imageBinary.operateFxImage();
        } catch (Exception e) {
            displayError(e.toString());
            return null;
        }
    }

    @Override
    protected void makeDemoFiles(List<String> files, Image demoImage) {
        try {
            ImageBinary imageBinary = new ImageBinary(demoImage);
            imageBinary.setScope(scope())
                    .setExcludeScope(excludeScope())
                    .setSkipTransparent(skipTransparent());
            String prefix = message("BlackOrWhite");

            BufferedImage bufferedImage = imageBinary
                    .setIntPara1(-1)
                    .setIsDithering(true)
                    .operate();
            String tmpFile = FileTmpTools.generateFile(prefix + "_" + message("Default")
                    + "_" + message("Dithering"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }

            bufferedImage = imageBinary
                    .setIntPara1(-1)
                    .setIsDithering(false)
                    .operate();
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("Default"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }

            List<Integer> inputs = new ArrayList<>();
            inputs.addAll(Arrays.asList(64, 96, 112, 128, 144, 160, 176, 198, 228));
            int input = binaryController.threshold;
            if (input > 0 && input < 255 && !inputs.contains(input)) {
                inputs.add(input);
            }
            for (int v : inputs) {
                bufferedImage = imageBinary
                        .setIntPara1(v)
                        .setIsDithering(true)
                        .operate();
                tmpFile = FileTmpTools.generateFile(prefix + "_" + v
                        + "_" + message("Dithering"), "png").getAbsolutePath();
                if (ImageFileWriters.writeImageFile(bufferedImage, "png", tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }

                bufferedImage = imageBinary
                        .setIntPara1(v)
                        .setIsDithering(false)
                        .operate();
                tmpFile = FileTmpTools.generateFile(prefix + "_" + v, "png").getAbsolutePath();
                if (ImageFileWriters.writeImageFile(bufferedImage, "png", tmpFile)) {
                    files.add(tmpFile);
                    demoTask.setInfo(tmpFile);
                }
            }

            int otsu = ImageBinary.calculateThreshold(srcImage());
            bufferedImage = imageBinary
                    .setIntPara1(otsu)
                    .setIsDithering(true)
                    .operate();
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("OTSU")
                    + otsu + "_" + message("Dithering"), "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(bufferedImage, "png", tmpFile)) {
                files.add(tmpFile);
                demoTask.setInfo(tmpFile);
            }

            bufferedImage = imageBinary
                    .setIntPara1(otsu)
                    .setIsDithering(false)
                    .operate();
            tmpFile = FileTmpTools.generateFile(prefix + "_" + message("OTSU") + otsu, "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(bufferedImage, "png", tmpFile)) {
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
    public static ImageBlackWhiteController open(BaseImageController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageBlackWhiteController controller = (ImageBlackWhiteController) WindowTools.openChildStage(
                    parent.getMyWindow(), Fxmls.ImageBlackWhiteFxml, false);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
