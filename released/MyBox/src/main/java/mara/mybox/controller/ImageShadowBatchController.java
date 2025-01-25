package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.image.tools.BufferedImageTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.image.PixelDemos;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-25
 * @License Apache License Version 2.0
 */
public class ImageShadowBatchController extends BaseImageEditBatchController {

    protected java.awt.Color color;
    protected boolean wPer, hPer, blur;

    @FXML
    protected ControlImageShadow shadowController;

    public ImageShadowBatchController() {
        baseTitle = message("ImageBatch") + " - " + message("Shadow");
    }

    @Override
    public boolean makeMoreParameters() {
        wPer = shadowController.wPercenatge();
        hPer = shadowController.hPercenatge();
        color = shadowController.awtColor();
        blur = shadowController.blur();
        return super.makeMoreParameters() && shadowController.pickValues();
    }

    @Override
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        try {
            int w, h;
            if (wPer) {
                w = source.getWidth() * shadowController.wPer / 100;
            } else {
                w = shadowController.w;
            }
            if (hPer) {
                h = source.getHeight() * shadowController.hPer / 100;
            } else {
                h = shadowController.h;
            }
            BufferedImage target = BufferedImageTools.addShadow(currentTask,
                    source, w, h, color, blur);
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @Override
    public void makeDemoFiles(FxTask currentTask, List<String> files, File demoFile, BufferedImage demoImage) {
        PixelDemos.shadow(currentTask, files, demoImage, shadowController.awtColor(), demoFile);
    }

}
