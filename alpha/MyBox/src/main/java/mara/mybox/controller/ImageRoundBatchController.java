package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.BufferedImageTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2018-9-25
 * @License Apache License Version 2.0
 */
public class ImageRoundBatchController extends BaseImageEditBatchController {

    @FXML
    protected ControlImageRound roundController;

    public ImageRoundBatchController() {
        baseTitle = Languages.message("ImageManufactureBatchRound");
    }

    @Override
    public boolean makeMoreParameters() {
        return super.makeMoreParameters() && roundController.pickValues();
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
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
            BufferedImage target = BufferedImageTools.setRound(task, source,
                    w, h, roundController.awtColor());
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
