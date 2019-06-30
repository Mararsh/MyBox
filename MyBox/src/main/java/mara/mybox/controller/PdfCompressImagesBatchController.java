/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.io.File;
import javafx.beans.binding.Bindings;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.FileTools;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class PdfCompressImagesBatchController extends PdfCompressImagesController {

    public PdfCompressImagesBatchController() {
        baseTitle = AppVaribles.getMessage("PdfCompressImagesBatch");

    }

    @Override
    public void initializeNext2() {
        try {
            allowPaused = false;
            startButton.disableProperty().bind(
                    Bindings.isEmpty(targetPathInput.textProperty())
                            .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                            .or(jpegBox.styleProperty().isEqualTo(badStyle))
                            .or(thresholdInput.styleProperty().isEqualTo(badStyle))
                            .or(Bindings.isEmpty(tableView.getItems()))
            );
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @Override
    public void makeMoreParameters() {
        makeBatchParameters();
    }

    @Override
    protected void makeTargetFile(File file) {
        String filename = file.getName();
        targetFile = new File(targetPath.getAbsolutePath() + File.separator + filename);
        if (targetExistType == TargetExistType.Rename) {
            while (targetFile.exists()) {
                filename = FileTools.getFilePrefix(filename)
                        + targetSuffixInput.getText().trim() + "." + FileTools.getFileSuffix(filename);
                targetFile = new File(targetPath.getAbsolutePath() + File.separator + filename);
            }
        } else if (targetExistType == TargetExistType.Skip) {
            if (targetFile.exists()) {
                targetFile = null;
            }
        }
    }

}
