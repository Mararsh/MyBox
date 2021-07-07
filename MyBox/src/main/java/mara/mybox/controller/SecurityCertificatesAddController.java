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
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlWindow;
import mara.mybox.tools.NetworkTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

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
    protected CheckBox chainCheck;

    public SecurityCertificatesAddController() {
        baseTitle = AppVariables.message("SecurityCertificates");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Certificate, VisitHistory.FileType.Html);
    }

    @FXML
    @Override
    public void okAction() {
        if (certController == null) {
            return;
        }
        File ksFile = certController.sourceFile;
        if (!ksFile.exists() || !ksFile.isFile()) {
            popError(message("NotExist"));
            return;
        }
        String name;
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
        try {
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected boolean handle() {
                        error = null;
                        if (certController.backupController.isBack()) {
                            certController.backupController.addBackup(certController.sourceFile);
                        }
                        if (addressRadio.isSelected()) {
                            try {
                                error = NetworkTools.installCertificateByHost(
                                        ksFile.getAbsolutePath(), password,
                                        addressInput.getText(), alias, chainCheck.isSelected());
                            } catch (Exception e) {
                                error = e.toString();
                            }
                        } else if (fileRadio.isSelected()) {
                            try {
                                error = NetworkTools.installCertificateByFile(
                                        ksFile.getAbsolutePath(), password,
                                        sourceFile, alias, chainCheck.isSelected());
                            } catch (Exception e) {
                                error = e.toString();
                            }
                        }
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (error == null) {
                            if (certController == null || !certController.getMyStage().isShowing()) {
                                certController = SecurityCertificatesController.oneOpen(ksFile);
                            }
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
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(false);
                thread.start();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {
                    private String result;

                    @Override
                    protected boolean handle() {
                        result = error = null;
                        try {
                            Certificate[] certs = NetworkTools.getCertificatesByFile(sourceFile);
                            StringBuilder s = new StringBuilder();
                            s.append("<h1  class=\"center\">").append(sourceFile.getAbsolutePath()).append("</h1>\n");
                            for (Certificate cert : certs) {
                                s.append("<hr>\n");
                                s.append("<pre>").append(cert).append("</pre>\n\n");
                            }
                            result = s.toString();
                        } catch (Exception e) {
                            error = e.toString();
                        }
                        return error == null;
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (result != null) {
                            FxmlWindow.openHtmlViewer(null, result);
                        } else {
                            popError(error);
                        }
                    }
                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(false);
                thread.start();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @FXML
    @Override
    public void cancelAction() {
        closeStage();
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
