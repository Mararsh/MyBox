package mara.mybox.controller;

import java.util.List;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fximage.FxImageTools;
import mara.mybox.fximage.ImageDemos;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
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
        if (!super.checkOptions()) {
            return false;
        }
        if (!roundController.pickValues()) {
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
        opInfo = message("RoundWidth") + ":" + w + " "
                + message("RoundHeight") + ":" + h + " "
                + message("Color") + ":" + color;
        return true;
    }

    @Override
    protected void handleImage(FxTask currentTask) {
        handledImage = FxImageTools.setRound(currentTask, srcImage(), w, h, color);
    }

    @Override
    protected void makeDemoFiles(FxTask currentTask, List<String> files, Image demoImage) {
        ImageDemos.round(currentTask, files,
                SwingFXUtils.fromFXImage(demoImage, null),
                roundController.awtColor(), prefix());
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
