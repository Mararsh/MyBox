/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import mara.mybox.fxml.FxmlStage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;

import mara.mybox.controller.base.PdfBatchBaseController;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.AppVaribles;
import mara.mybox.value.CommonValues;
import mara.mybox.data.PdfInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class PdfSourceSelectionController extends PdfBatchBaseController {

    public PdfInformation pdfInformation;

    @FXML
    public TextField fromPageInput, toPageInput;

    public PdfSourceSelectionController() {
        fileExtensionFilter = CommonValues.PdfExtensionFilter;

    }

    @Override
    public void initializeNext() {
        try {

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

                    loadPdfInformation(file, null);
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
    public void viewAction(ActionEvent event) {
        if (pdfInformation == null) {
            return;
        }
        FxmlStage.openPdfViewer( null, pdfInformation.getFile());
    }

    public void loadPdfInformation(final File file, final String inPassword) {
        if (file == null) {
            return;
        }
        toPageInput.setText("");
        infoButton.setDisable(true);

        pdfInformation = new PdfInformation(file);
        sourceFileInput.setStyle(badStyle);
        task = new Task<Void>() {
            private boolean ok, pop;

            @Override
            public Void call() throws Exception {
                try {
                    try (PDDocument doc = PDDocument.load(file, inPassword, AppVaribles.pdfMemUsage)) {
                        pdfInformation.setUserPassword(inPassword);
                        pdfInformation.readInfo(doc);
                        ok = true;
                    }
                } catch (InvalidPasswordException e) {
                    pop = true;
                } catch (IOException e) {
                }
                if (pop) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            TextInputDialog dialog = new TextInputDialog();
                            dialog.setContentText(AppVaribles.getMessage("Password"));
                            Optional<String> result = dialog.showAndWait();
                            if (result.isPresent()) {
                                loadPdfInformation(file, result.get());
                            }
                        }
                    });
                }
                return null;
            }

            @Override
            public void succeeded() {
                super.succeeded();
                if (!ok) {
                    return;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        sourceFileInput.setStyle(null);
                        if (file.isDirectory()) {
                            AppVaribles.setUserConfigValue(parentController.sourcePathKey, file.getPath());
                        } else {
                            AppVaribles.setUserConfigValue(parentController.sourcePathKey, file.getParent());
                        }
                        toPageInput.setText(pdfInformation.getNumberOfPages() + "");
                        infoButton.setDisable(false);
                        viewButton.setDisable(false);
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

}
