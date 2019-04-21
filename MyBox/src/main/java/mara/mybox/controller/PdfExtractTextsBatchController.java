package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import static mara.mybox.fxml.FxmlControl.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-7-4
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfExtractTextsBatchController extends PdfExtractTextsController {

    @FXML
    private ScrollPane scrollPane;

    public PdfExtractTextsBatchController() {
        baseTitle = AppVaribles.getMessage("PdfExtractTextsBatch");

    }

    @Override
    public void initializeNext2() {
        try {

            operationBarController.startButton.disableProperty().bind(
                    Bindings.isEmpty(filesTableController.filesTableView.getItems())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
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
    public void makeMoreParameters() {
        makeBatchParameters();
    }

}
