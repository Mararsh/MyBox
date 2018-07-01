/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.io.File;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.tools.FxmlTools;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class TargetSelectionController extends BaseController {

    protected File targetPath;
    protected PdfBaseController parentController;

    @FXML
    protected TextField targetPathInput;
    @FXML
    protected CheckBox subdirCheck;
    @FXML
    protected TextField targetPrefixInput;
    @FXML
    protected TextField targetFileInput;

    @Override
    protected void initializeNext() {
        try {
            targetPathInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        final File file = new File(newValue);
                        if (file.isDirectory()) {
                            AppVaribles.setConfigValue("pdfTargetPath", file.getPath());
                        } else {
                            AppVaribles.setConfigValue("pdfTargetPath", file.getParent());
                        }
                        targetPath = file;
                        parentController.targetPathChanged();
                    } catch (Exception e) {
                    }
                }
            });
            FxmlTools.setFileValidation(targetPathInput);

            if (subdirCheck != null) {
                subdirCheck.setSelected(AppVaribles.getConfigBoolean("pdf_creatSubdir"));
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    protected void selectTargetPath(ActionEvent event) {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = new File(AppVaribles.getConfigValue("pdfTargetPath", System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            chooser.setInitialDirectory(path);
            File directory = chooser.showDialog(getMyStage());
            if (directory != null) {
                targetPathInput.setText(directory.getPath());
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public File getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(File targetPath) {
        this.targetPath = targetPath;
    }

    public TextField getTargetPathInput() {
        return targetPathInput;
    }

    public void setTargetPathInput(TextField targetPathInput) {
        this.targetPathInput = targetPathInput;
    }

    public CheckBox getSubdirCheck() {
        return subdirCheck;
    }

    public void setSubdirCheck(CheckBox subdirCheck) {
        this.subdirCheck = subdirCheck;
    }

    public TextField getTargetPrefixInput() {
        return targetPrefixInput;
    }

    public void setTargetPrefixInput(TextField targetPrefixInput) {
        this.targetPrefixInput = targetPrefixInput;
    }

    public PdfBaseController getParentController() {
        return parentController;
    }

    public void setParentController(PdfBaseController parentController) {
        this.parentController = parentController;
    }

}
