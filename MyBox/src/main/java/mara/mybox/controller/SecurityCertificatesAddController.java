package mara.mybox.controller;

import java.io.File;
import java.security.cert.Certificate;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.tools.NetworkTools;
import mara.mybox.tools.VisitHistoryTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2019-11-29
 * @License Apache License Version 2.0
 */
public class SecurityCertificatesAddController extends BaseController {

    protected SecurityCertificatesController certController;

    @FXML
    protected TextField addressInput;
    @FXML
    protected RadioButton addressRadio, fileRadio;
    @FXML
    protected CheckBox backupCheck;

    public SecurityCertificatesAddController() {
        baseTitle = AppVariables.message("SecurityCertificates");

        SourceFileType = VisitHistory.FileType.Certificate;
        SourcePathType = VisitHistory.FileType.Certificate;
        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;

        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Certificate);
        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Html);
        sourceExtensionFilter = CommonFxValues.KeyStoreExtensionFilter;
        targetExtensionFilter = CommonFxValues.HtmlExtensionFilter;
    }

    @FXML
    @Override
    public void okAction() {
        if (certController == null) {
            return;
        }
        String name = "";
        if (addressRadio.isSelected()) {
            if (addressInput.getText().isEmpty()) {
                popError(message("NotExist"));
                return;
            }
            name = addressInput.getText();
        } else {
            sourceFile = new File(sourceFileInput.getText());
            if (!sourceFile.exists() || !sourceFile.isFile()) {
                popError(message("NotExist"));
                return;
            }
            name = sourceFile.getName();
        }

        File ksFile = new File(certController.sourceFileInput.getText());
        if (!ksFile.exists() || !ksFile.isFile()) {
            popError(message("NotExist"));
            return;
        }
        String password = certController.getPasswordInput().getText();

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle(message("SecurityCertificates"));
        dialog.setHeaderText(message("Alias"));
        dialog.setContentText("");
        dialog.getEditor().setPrefWidth(300);
        dialog.getEditor().setText(name);
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        Optional<String> result = dialog.showAndWait();
        if (!result.isPresent() || result.get().trim().isBlank()) {
            return;
        }
        final String alias = result.get().trim();
        if (!certController.backupKeyStore()) {
            return;
        }
        try {
            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        error = null;

                        if (addressRadio.isSelected()) {
                            try {
                                error = NetworkTools.installCertificateByHost(
                                        ksFile.getAbsolutePath(), password,
                                        addressInput.getText(), alias);
                            } catch (Exception e) {
                                error = e.toString();
                            }
                        } else if (fileRadio.isSelected()) {
                            try {
                                error = NetworkTools.installCertificateByFile(
                                        ksFile.getAbsolutePath(), password,
                                        sourceFile, alias);
                            } catch (Exception e) {
                                error = e.toString();
                            }
                        }
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (error == null) {
                            certController.loadAll(alias);
                            if (saveCloseCheck.isSelected()) {
                                closeStage();
                            }
                            popSuccessful();
                        } else {
                            popError(error);
                        }
                    }
                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    public void viewAction() {
        sourceFile = new File(sourceFileInput.getText());
        if (!sourceFile.exists() || !sourceFile.isFile()) {
            popError(message("NotExist"));
            return;
        }
        try {
            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {
                    private String result;

                    @Override
                    protected boolean handle() {
                        result = error = null;
                        try {
                            Certificate cert = NetworkTools.getCertificateByFile(sourceFile);
                            StringBuilder s = new StringBuilder();
                            s.append("<h1  class=\"center\">").append(sourceFile.getAbsolutePath()).append("</h1>\n");
                            s.append("<hr>\n");
                            s.append("<pre>").append(cert).append("</pre>\n");
                            result = s.toString();
                        } catch (Exception e) {
                            error = e.toString();
                        }

                        return error == null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (result != null) {
                            FxmlStage.openHtmlViewer(null, result);
                        } else {
                            popError(error);
                        }
                    }
                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    /*
        get/set
     */
    public SecurityCertificatesController getCertController() {
        return certController;
    }

    public void setCertController(SecurityCertificatesController certController) {
        this.certController = certController;
    }

}
