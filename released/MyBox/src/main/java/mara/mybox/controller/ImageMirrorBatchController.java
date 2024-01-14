package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import mara.mybox.bufferedimage.TransformTools;
import mara.mybox.fxml.FxTask;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @License Apache License Version 2.0
 */
public class ImageMirrorBatchController extends BaseImageEditBatchController {

    @FXML
    protected RadioButton horizontalRadio;

    public ImageMirrorBatchController() {
        baseTitle = message("ImageBatch") + " - " + message("Mirror");
    }

    @Override
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        if (horizontalRadio.isSelected()) {
            return TransformTools.horizontalMirrorImage(currentTask, source);
        } else {
            return TransformTools.verticalMirrorImage(currentTask, source);
        }
    }

}
