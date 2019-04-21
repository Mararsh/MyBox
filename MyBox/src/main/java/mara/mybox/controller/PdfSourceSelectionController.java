/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import mara.mybox.controller.base.PdfBatchBaseController;
import mara.mybox.fxml.FxmlStage;
import java.io.File;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import mara.mybox.controller.PdfInformationController;
import mara.mybox.controller.base.PdfBatchBaseController;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.data.PdfInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class PdfSourceSelectionController extends PdfBatchBaseController {

    public PdfInformation pdfInformation;

    @FXML
    public Button pdfOpenButon;
    @FXML
    public TextField fromPageInput, toPageInput;
    @FXML
    public PasswordField passwordInput;

    public PdfSourceSelectionController() {

    }

    @Override
    public void initializeNext() {
        try {
            fileExtensionFilter = CommonValues.PdfExtensionFilter;

            sourceFileInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue == null || newValue.isEmpty() || !FileTools.isPDF(newValue)) {
                        sourceFileInput.setStyle(badStyle);
                        return;
                    }
                    final File file = new File(newValue);
                    if (!file.exists()) {
                        sourceFileInput.setStyle(badStyle);
                        return;
                    }
                    sourceFileInput.setStyle(null);
                    parentController.sourceFile = file;
                    if (file.isDirectory()) {
                        AppVaribles.setUserConfigValue(parentController.sourcePathKey, file.getPath());
                    } else {
                        AppVaribles.setUserConfigValue(parentController.sourcePathKey, file.getParent());
                    }
                    loadPdfInformation();
                }
            });
            if (fromPageInput != null) {
                FxmlControl.setPositiveValidation(fromPageInput);
                fromPageInput.setText("1");
            }
            if (toPageInput != null) {
                FxmlControl.setPositiveValidation(toPageInput);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @Override
    public void addFile(File file) {
        parentController.sourceFile = file;
        if (sourceFileInput != null) {
            sourceFileInput.setText(file.getAbsolutePath());
        }

        parentController.recordFileAdded(file);
        parentController.sourceFileChanged(file);
    }

    @FXML
    @Override
    public void infoAction() {
        if (pdfInformation == null) {
            return;
        }
        try {
            final PdfInformationController controller = (PdfInformationController) openStage(CommonValues.PdfInformationFxml);
            controller.setInformation(pdfInformation);

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void openPdfAction(ActionEvent event) {
        if (pdfInformation == null) {
            return;
        }
        FxmlStage.openPdfViewer(getClass(), null, pdfInformation.getFile());
    }

    public void loadPdfInformation() {
        if (sourceFile == null) {
            return;
        }
        toPageInput.setText("");
        infoButton.setDisable(true);

        pdfInformation = new PdfInformation(sourceFile);
        task = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                pdfInformation.readInformation(passwordInput.getText());

                return null;
            }

            @Override
            public void succeeded() {
                super.succeeded();
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (pdfInformation != null) {
                            toPageInput.setText(pdfInformation.getNumberOfPages() + "");
                            infoButton.setDisable(false);
                            pdfOpenButon.setDisable(false);
                        }
                        parentController.sourceFileChanged(sourceFile);
                    }
                });
            }
        };
        openHandlingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public int readFromPage() {
        return FxmlControl.getInputInt(fromPageInput);
    }

    public int readToPage() {
        return FxmlControl.getInputInt(toPageInput);
    }

    public String readPassword() {
        return passwordInput.getText();
    }

    public PdfInformation getPdfInformation() {
        return pdfInformation;
    }

    public void setPdfInformation(PdfInformation pdfInformation) {
        this.pdfInformation = pdfInformation;
    }

    public TextField getFromPageInput() {
        return fromPageInput;
    }

    public void setFromPageInput(TextField fromPageInput) {
        this.fromPageInput = fromPageInput;
    }

    public TextField getToPageInput() {
        return toPageInput;
    }

    public void setToPageInput(TextField toPageInput) {
        this.toPageInput = toPageInput;
    }

    public PasswordField getPasswordInput() {
        return passwordInput;
    }

    public void setPasswordInput(PasswordField passwordInput) {
        this.passwordInput = passwordInput;
    }

}
