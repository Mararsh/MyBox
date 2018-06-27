/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-6-24
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfExtractImagesBatchController extends PdfExtractImagesController {

    @FXML
    private ScrollPane scrollPane;

    @Override
    protected void initializeNext2() {
        try {
            filesTableController.setParentFxml(myFxml);
            filesTableController.setConfigPathName("pdfSourcePath");

            appendPageNumber.setSelected(AppVaribles.getConfigBoolean("pei_appendPageNumber"));
            appendIndex.setSelected(AppVaribles.getConfigBoolean("pei_appendIndex"));

            startButton.disableProperty().bind(
                    Bindings.isEmpty(filesTableController.getFilesTableView().getItems())
                            .or(Bindings.isEmpty(targetPathInput.textProperty()))
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
            );

            previewButton.disableProperty().bind(
                    startButton.disableProperty()
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
