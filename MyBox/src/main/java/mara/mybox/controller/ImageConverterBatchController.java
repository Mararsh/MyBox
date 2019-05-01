package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import static mara.mybox.fxml.FxmlControl.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-7-5
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageConverterBatchController extends ImageConverterController {

    public ImageConverterBatchController() {
        baseTitle = AppVaribles.getMessage("ImageConverterBatch");

    }

    @Override
    public void initializeNext2() {
        try {
            imageConverterAttributesController.parentFxml = myFxml;
            imageConverterAttributesController.originalButton.setDisable(true);

            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(imageConverterAttributesController.xInput.styleProperty().isEqualTo(badStyle))
                            .or(imageConverterAttributesController.yInput.styleProperty().isEqualTo(badStyle))
                            .or(imageConverterAttributesController.qualityBox.disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageConverterAttributesController.qualityInput.styleProperty().isEqualTo(badStyle)))
                            .or(imageConverterAttributesController.colorBox.disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageConverterAttributesController.thresholdInput.styleProperty().isEqualTo(badStyle)))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void makeMoreParameters() {
        makeBatchParameters();
    }
}
