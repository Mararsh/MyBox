package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import mara.mybox.bufferedimage.PixelsBlend;
import mara.mybox.bufferedimage.PixelsOperationFactory.BlendColor;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @License Apache License Version 2.0
 */
public class ImageBlendColorBatchController extends BaseImageEditBatchController {

    protected BlendColor blendColor;

    @FXML
    protected ControlColorSet colorController;
    @FXML
    protected ControlImagesBlend blendController;

    public ImageBlendColorBatchController() {
        baseTitle = message("ImageBatch") + " - " + message("BlendColor");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            colorController.init(this, baseName + "Color", Color.PINK);

            blendController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean makeMoreParameters() {
        if (!super.makeMoreParameters()) {
            return false;
        }
        PixelsBlend blend = blendController.pickValues();
        if (blend == null) {
            return false;
        }
        blendColor = new BlendColor(null, null).setBlender(blend);
        blendColor.setColorPara1(colorController.awtColor());
        return true;
    }

    @Override
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        return blendColor.setImage(source).setTask(currentTask).operateImage();
    }

}