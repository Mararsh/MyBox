package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.image.tools.BufferedImageTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.image.ImageDemos;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-25
 * @License Apache License Version 2.0
 */
public class ImageRoundBatchController extends BaseImageEditBatchController {

    @FXML
    protected ControlImageRound roundController;

    public ImageRoundBatchController() {
        baseTitle = message("ImageBatch") + " - " + message("Round");
    }

    @Override
    public boolean makeMoreParameters() {
        return super.makeMoreParameters() && roundController.pickValues();
    }

    @Override
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        try {
            int w, h;
            if (roundController.wPercenatge()) {
                w = source.getWidth() * roundController.wPer / 100;
            } else {
                w = roundController.w;
            }
            if (roundController.hPercenatge()) {
                h = source.getHeight() * roundController.hPer / 100;
            } else {
                h = roundController.h;
            }
            BufferedImage target = BufferedImageTools.setRound(currentTask, source,
                    w, h, roundController.awtColor());
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public void makeDemoFiles(FxTask currentTask, List<String> files, File demoFile, BufferedImage demoImage) {
        ImageDemos.round(currentTask, files, demoImage, roundController.awtColor(), demoFile);
    }

}
