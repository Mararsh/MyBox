package mara.mybox.controller;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import mara.mybox.bufferedimage.TransformTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.ValidationTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-9-23
 * @License Apache License Version 2.0
 */
public class ImageRotateBatchController extends BaseImageEditBatchController {

    protected int rotateAngle;

    @FXML
    protected ComboBox<String> angleSelector;

    public ImageRotateBatchController() {
        baseTitle = message("ImageBatch") + " - " + message("Rotate");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            rotateAngle = UserConfig.getInt(baseName + "RotateAngle", 45);
            angleSelector.getItems().addAll(Arrays.asList(
                    "45", "-45", "90", "-90", "180", "-180", "30", "-30", "60", "-60",
                    "120", "-120", "15", "-15", "5", "-5", "10", "-10", "1", "-1",
                    "75", "-75", "135", "-135"));
            angleSelector.setVisibleRowCount(10);
            angleSelector.setValue(rotateAngle + "");

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(targetPathController.valid.not()
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(angleSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean makeMoreParameters() {
        if (!super.makeMoreParameters()) {
            return false;
        }
        try {
            rotateAngle = Integer.parseInt(angleSelector.getValue());
            ValidationTools.setEditorNormal(angleSelector);
            return true;
        } catch (Exception e) {
            popError(message("InvalidParameter") + ": " + message("RotateAngle"));
            ValidationTools.setEditorBadStyle(angleSelector);
            return false;
        }
    }

    @Override
    protected BufferedImage handleImage(FxTask currentTask, BufferedImage source) {
        return TransformTools.rotateImage(currentTask, source, rotateAngle);
    }

}
