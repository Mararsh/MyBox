package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-7-5
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageConverterBatchController extends ImageConverterController {

    @Override
    protected void initializeNext2() {
        try {

            imageConverterAttributesController.setParentFxml(myFxml);
            imageConverterAttributesController.getOriginalButton().setDisable(true);

            operationBarController.startButton.disableProperty().bind(
                    Bindings.isEmpty(filesTableController.getFilesTableView().getItems())
                            .or(Bindings.isEmpty(targetSelectionController.targetPathInput.textProperty()))
                            .or(targetSelectionController.targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(imageConverterAttributesController.getxInput().styleProperty().isEqualTo(badStyle))
                            .or(imageConverterAttributesController.getyInput().styleProperty().isEqualTo(badStyle))
                            .or(imageConverterAttributesController.getQualityBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageConverterAttributesController.getQualityInput().styleProperty().isEqualTo(badStyle)))
                            .or(imageConverterAttributesController.getColorBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageConverterAttributesController.getThresholdInput().styleProperty().isEqualTo(badStyle)))
            );

            previewButton.disableProperty().bind(
                    operationBarController.startButton.disableProperty()
                            .or(operationBarController.startButton.textProperty().isNotEqualTo(AppVaribles.getMessage("Start")))
            );

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void makeMoreParameters() {
        makeBatchParameters();
    }
}
