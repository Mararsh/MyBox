package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import static mara.mybox.fxml.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-6-16
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfConvertImagesBatchController extends PdfConvertImagesController {

    public PdfConvertImagesBatchController() {
    }

    @Override
    protected void initializeNext2() {
        try {

            operationBarController.startButton.disableProperty().bind(
                    Bindings.isEmpty(filesTableController.getFilesTableView().getItems())
                            .or(Bindings.isEmpty(targetSelectionController.targetPathInput.textProperty()))
                            .or(targetSelectionController.targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(pdfConvertAttributesController.getDensityInput().styleProperty().isEqualTo(badStyle))
                            .or(pdfConvertAttributesController.getQualityBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(pdfConvertAttributesController.getQualityInput().styleProperty().isEqualTo(badStyle)))
                            .or(pdfConvertAttributesController.getColorBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(pdfConvertAttributesController.getThresholdInput().styleProperty().isEqualTo(badStyle)))
            );

            previewButton.disableProperty().bind(
                    pdfConvertAttributesController.getRawSelect().selectedProperty()
                            .or(operationBarController.startButton.disableProperty())
                            .or(operationBarController.startButton.textProperty().isNotEqualTo(AppVaribles.getMessage("Start")))
            );

            if (targetSelectionController.targetPathInput != null && targetSelectionController.targetPathInput.getText().isEmpty()) {
                targetSelectionController.targetPathInput.setText(AppVaribles.getUserConfigPath("pdfTargetPath").getAbsolutePath());
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    protected void makeMoreParameters() {
        makeBatchParameters();
    }

}
