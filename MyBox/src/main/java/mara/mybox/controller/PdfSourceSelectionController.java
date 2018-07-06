/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.PdfInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * FXML Controller class
 *
 * @author mara
 */
public class PdfSourceSelectionController extends BaseController {

    protected PdfInformation pdfInformation;

    @FXML
    protected Button sourceSelectButton;
    @FXML
    protected Button fileInformationButton;
    @FXML
    protected TextField fromPageInput;
    @FXML
    protected TextField toPageInput;
    @FXML
    protected PasswordField passwordInput;

    public PdfSourceSelectionController() {

    }

    @Override
    protected void initializeNext() {
        try {

            fileExtensionFilter = new ArrayList();
            fileExtensionFilter.add(new FileChooser.ExtensionFilter("pdf", "*.pdf", "*.PDF"));

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
                        AppVaribles.setConfigValue(parentController.sourcePathKey, file.getPath());
                    } else {
                        AppVaribles.setConfigValue(parentController.sourcePathKey, file.getParent());
                    }
                    loadPdfInformation();
                }
            });
            if (fromPageInput != null) {
                FxmlTools.setNonnegativeValidation(fromPageInput);
            }
            if (toPageInput != null) {
                FxmlTools.setNonnegativeValidation(toPageInput);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    protected void selectSourceFile(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getConfigValue(parentController.sourcePathKey, System.getProperty("user.home")));
            if (!path.isDirectory()) {
                path = new File(System.getProperty("user.home"));
            }
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(fileExtensionFilter);
            File file = fileChooser.showOpenDialog(getMyStage());
            if (file == null) {
                return;
            }
            parentController.sourceFile = file;
            AppVaribles.setConfigValue("LastPath", file.getParent());
            AppVaribles.setConfigValue(parentController.sourcePathKey, file.getParent());

            if (sourceFileInput != null) {
                sourceFileInput.setText(file.getAbsolutePath());
            }

            parentController.sourceFileChanged(file);

        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @FXML
    protected void showFileInformation(ActionEvent event) {
        if (pdfInformation == null) {
            return;
        }
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(CommonValues.PdfInformationFxml), AppVaribles.CurrentBundle);
            Pane root = fxmlLoader.load();
            PdfInformationController controller = fxmlLoader.getController();
            controller.setInformation(pdfInformation);

            Stage infoStage = new Stage();
            controller.setMyStage(infoStage);
            infoStage.setTitle(AppVaribles.getMessage("AppTitle"));
            infoStage.initModality(Modality.NONE);
            infoStage.initStyle(StageStyle.DECORATED);
            infoStage.initOwner(null);
            infoStage.getIcons().add(CommonValues.AppIcon);
            infoStage.setScene(new Scene(root));
            infoStage.show();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void loadPdfInformation() {

        if (sourceFile == null) {
            return;
        }
        toPageInput.setText("");
        fileInformationButton.setDisable(true);

        pdfInformation = new PdfInformation(sourceFile);
        task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                pdfInformation.loadDocument(passwordInput.getText());
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        if (pdfInformation != null) {
                            toPageInput.setText((pdfInformation.getNumberOfPages() - 1) + "");
                            fileInformationButton.setDisable(false);
                        }
                        parentController.sourceFileChanged(sourceFile);
                    }
                });
                return null;
            }
        };
        openLoadingStage(task, Modality.WINDOW_MODAL);
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    public int readFromPage() {
        return FxmlTools.getInputInt(fromPageInput);
    }

    public int readToPage() {
        return FxmlTools.getInputInt(toPageInput);
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

    public Button getSourceSelectButton() {
        return sourceSelectButton;
    }

    public void setSourceSelectButton(Button sourceSelectButton) {
        this.sourceSelectButton = sourceSelectButton;
    }

    public Button getFileInformationButton() {
        return fileInformationButton;
    }

    public void setFileInformationButton(Button fileInformationButton) {
        this.fileInformationButton = fileInformationButton;
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
