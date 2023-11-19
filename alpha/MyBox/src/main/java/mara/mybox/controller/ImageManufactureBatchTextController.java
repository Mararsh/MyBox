package mara.mybox.controller;

import java.awt.image.BufferedImage;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import mara.mybox.bufferedimage.ImageTextTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2018-9-25
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageManufactureBatchTextController extends BaseImageEditBatchController {

    @FXML
    protected ControlImageText optionsController;

    public ImageManufactureBatchTextController() {
        baseTitle = Languages.message("ImageManufactureBatchText");

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            optionsController.setParameters(this, null);

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(targetPathController.valid.not()
                    .or(Bindings.isEmpty(tableView.getItems()))
                    .or(Bindings.isEmpty(optionsController.textArea.textProperty()))
                    .or(optionsController.xInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(optionsController.yInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(optionsController.marginInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    @Override
    public boolean makeMoreParameters() {
        return super.makeMoreParameters() && optionsController.checkParameters();
    }

    @Override
    protected BufferedImage handleImage(BufferedImage source) {
        try {
            BufferedImage target = ImageTextTools.addText(source, optionsController);
            return target;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
