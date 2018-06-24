package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.tools.FxmlTools.badStyle;

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
            filesTableController.setParentFxml(myFxml);
            filesTableController.setConfigPathName("pdfSourcePath");

            startButton.disableProperty().bind(
                    Bindings.isEmpty(filesTableController.getFilesTableView().getItems())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(imageAttributesController.getDensityInput().styleProperty().isEqualTo(badStyle))
                            .or(imageAttributesController.getQualityBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageAttributesController.getQualityInput().styleProperty().isEqualTo(badStyle)))
                            .or(imageAttributesController.getColorBox().disableProperty().isEqualTo(new SimpleBooleanProperty(false)).and(imageAttributesController.getThresholdInput().styleProperty().isEqualTo(badStyle)))
            );

            previewButton.disableProperty().bind(
                    imageAttributesController.getRawSelect().selectedProperty()
                            .or(startButton.disableProperty())
                            .or(startButton.textProperty().isNotEqualTo(AppVaribles.getMessage("Start")))
            );

            if (targetPathInput != null && targetPathInput.getText().isEmpty()) {
                targetPathInput.setText(AppVaribles.getConfigValue("pdfTargetPath", System.getProperty("user.home")));
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
