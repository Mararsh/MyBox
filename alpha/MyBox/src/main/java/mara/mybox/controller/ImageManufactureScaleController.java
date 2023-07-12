package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import mara.mybox.data.DoublePoint;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2019-9-5
 * @License Apache License Version 2.0
 */
public class ImageManufactureScaleController extends ImageManufactureOperationController {

    @FXML
    protected ControlImageScale sizeController;
    @FXML
    protected Label commentsLabel;

    @Override
    public void initPane() {
        try {
            super.initPane();

            okButton.disableProperty().bind(sizeController.widthInput.styleProperty().isEqualTo(UserConfig.badStyle())
                    .or(sizeController.heightInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                    .or(sizeController.scaleSelector.getEditor().styleProperty().isEqualTo(UserConfig.badStyle()))
            );

            sizeController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }

    }

    @Override
    protected void paneExpanded() {
        sizeController.checkScaleType();
    }

    @Override
    public void paneClicked(MouseEvent event, DoublePoint p) {
        sizeController.imageClicked();
    }

    @FXML
    @Override
    public void okAction() {
        sizeController.scale();
    }

    @Override
    protected void resetOperationPane() {
        sizeController.checkScaleType();
    }

}
