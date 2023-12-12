package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-9-5
 * @License Apache License Version 2.0
 */
public class ImageShadowController extends BaseImageEditController {

    protected int w, h;
    protected Color color;
    protected boolean blur;

    @FXML
    protected ControlImageShadow shadowController;

    public ImageShadowController() {
        baseTitle = message("Shadow");
    }

    @Override
    protected void initMore() {
        try {
            operation = message("Shadow");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions() || !shadowController.pickValues()) {
            return false;
        }
        if (shadowController.wPercenatge()) {
            w = (int) (srcImage().getWidth() * shadowController.wPer / 100);
        } else {
            w = shadowController.w;
        }
        if (shadowController.hPercenatge()) {
            h = (int) (srcImage().getWidth() * shadowController.hPer / 100);
        } else {
            h = shadowController.h;
        }
        blur = shadowController.blur();
        color = shadowController.color();
        operation = message("Shadow");
        opInfo = message("HorizontalOffset") + ":" + w + " "
                + message("VerticalOffset") + ":" + h + " "
                + message("Color") + ":" + color + " "
                + message("Blur") + ":" + blur;
        return true;
    }

    @Override
    protected void handleImage() {
        handledImage = FxImageTools.addShadow(task, srcImage(), w, h, color, blur);
    }

    @Override
    protected void makeDemoFiles(List<String> files, Image demoImage) {
        try {
            BufferedImage srcImage = SwingFXUtils.fromFXImage(demoImage, null);
            int offsetX = Math.max(30, srcImage.getWidth() / 20);
            int offsetY = Math.max(30, srcImage.getHeight() / 20);

            makeDemoFile(files, srcImage, offsetX, offsetY, true);
            makeDemoFile(files, srcImage, -offsetX, offsetY, true);
            makeDemoFile(files, srcImage, offsetX, -offsetY, true);
            makeDemoFile(files, srcImage, -offsetX, -offsetY, true);
            makeDemoFile(files, srcImage, offsetX, offsetY, false);
            makeDemoFile(files, srcImage, -offsetX, offsetY, false);
            makeDemoFile(files, srcImage, offsetX, -offsetY, false);
            makeDemoFile(files, srcImage, -offsetX, -offsetY, false);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void makeDemoFile(List<String> files, BufferedImage srcImage,
            int offsetX, int offsetY, boolean blur) {
        try {
            BufferedImage bufferedImage = BufferedImageTools.addShadow(demoTask, srcImage,
                    -offsetX, -offsetY, shadowController.awtColor(), blur);
            String tmpFile = FileTmpTools.generateFile(message("Shadow") + "_" + color
                    + "_x-" + offsetX + "_y-" + offsetY
                    + (blur ? ("_" + message("Blur")) : ""),
                    "png").getAbsolutePath();
            if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
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
    public static ImageShadowController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageShadowController controller = (ImageShadowController) WindowTools.branch(
                    parent.getMyWindow(), Fxmls.ImageShadowFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
