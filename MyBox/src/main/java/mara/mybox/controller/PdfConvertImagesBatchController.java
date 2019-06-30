package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import static mara.mybox.fxml.FxmlControl.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-6-16
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfConvertImagesBatchController extends PdfConvertImagesController {

    public PdfConvertImagesBatchController() {
        baseTitle = AppVaribles.getMessage("PdfConvertImagesBatch");

    }

    @Override
    public void initializeNext2() {
        try {

            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(pdfConvertAttributesController.densityInput.styleProperty().isEqualTo(badStyle))
                            .or(pdfConvertAttributesController.qualityBox.disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(pdfConvertAttributesController.qualityInput.styleProperty().isEqualTo(badStyle)))
                            .or(pdfConvertAttributesController.colorBox.disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(pdfConvertAttributesController.thresholdInput.styleProperty().isEqualTo(badStyle)))
            );

            previewButton.disableProperty().bind(
                    pdfConvertAttributesController.rawSelect.selectedProperty()
                            .or(startButton.disableProperty())
                            .or(startButton.textProperty().isNotEqualTo(AppVaribles.getMessage("Start")))
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
