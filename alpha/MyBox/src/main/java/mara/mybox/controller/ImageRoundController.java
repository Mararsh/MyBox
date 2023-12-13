package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxColorTools;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.imagefile.ImageFileWriters;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2019-8-13
 * @License Apache License Version 2.0
 */
public class ImageRoundController extends BaseImageEditController {

    protected int w, h;
    protected Color color;

    @FXML
    protected ControlImageRound roundController;

    public ImageRoundController() {
        baseTitle = message("Round");
    }

    @Override
    protected void initMore() {
        try {
            super.initMore();
            operation = message("Round");

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected boolean checkOptions() {
        if (!super.checkOptions() || !roundController.pickValues()) {
            return false;
        }
        if (roundController.wPercenatge()) {
            w = (int) (srcImage().getWidth() * roundController.wPer / 100);
        } else {
            w = roundController.w;
        }
        if (roundController.hPercenatge()) {
            h = (int) (srcImage().getWidth() * roundController.hPer / 100);
        } else {
            h = roundController.h;
        }
        color = roundController.color();
        operation = message("Round");
        opInfo = message("RoundWidth") + ":" + w + " "
                + message("RoundHeight") + ":" + h + " "
                + message("Color") + ":" + color;
        return true;
    }

    @Override
    protected void handleImage() {
        handledImage = FxImageTools.setRound(task, srcImage(), w, h, color);
    }

    @Override
    protected void makeDemoFiles(List<String> files, Image demoImage) {
        try {
            BufferedImage srcImage = SwingFXUtils.fromFXImage(srcImage(), null);
            int width = srcImage.getWidth();
            java.awt.Color acolor = FxColorTools.toAwtColor(color);
            String prefix = message("Round") + "_" + acolor + "_" + message("Radius");

            List<Integer> values = new ArrayList<>();
            values.addAll(Arrays.asList(width / 6, width / 8, width / 4, width / 10,
                    width / 20, width / 30));
            for (int r : values) {
                if (demoTask == null || !demoTask.isWorking()) {
                    return;
                }
                BufferedImage bufferedImage = BufferedImageTools.setRound(
                        demoTask, srcImage, r, r, acolor);
                String tmpFile = FileTmpTools.generateFile(prefix + r, "png").getAbsolutePath();
                if (ImageFileWriters.writeImageFile(demoTask, bufferedImage, "png", tmpFile)) {
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
    public static ImageRoundController open(ImageEditorController parent) {
        try {
            if (parent == null) {
                return null;
            }
            ImageRoundController controller = (ImageRoundController) WindowTools.branchStage(
                    parent, Fxmls.ImageRoundFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
