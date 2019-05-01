package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import static mara.mybox.fxml.FxmlControl.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfExtractImagesBatchController extends PdfExtractImagesController {

    @FXML
    private ScrollPane scrollPane;

    public PdfExtractImagesBatchController() {
        baseTitle = AppVaribles.getMessage("PdfExtractImagesBatch");

    }

    @Override
    public void initializeNext2() {
        try {

            appendPageNumber.setSelected(AppVaribles.getUserConfigBoolean("pei_appendPageNumber"));
            appendIndex.setSelected(AppVaribles.getUserConfigBoolean("pei_appendIndex"));

            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
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
