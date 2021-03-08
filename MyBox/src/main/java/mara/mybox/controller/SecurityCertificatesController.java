package mara.mybox.controller;

import java.io.File;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import mara.mybox.data.CertificateEntry;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.data.VisitHistoryTools;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.TableTimeCell;
import mara.mybox.tools.NetworkTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2019-11-29
 * @License Apache License Version 2.0
 */
public class SecurityCertificatesController extends BaseController {

    protected ObservableList<CertificateEntry> tableData;
    protected File cacertsFile;

    @FXML
    protected TextField passwordInput;
    @FXML
    protected TableView<CertificateEntry> tableView;
    @FXML
    protected TableColumn<CertificateEntry, String> aliasColumn, timeColumn;
    @FXML
    protected TextArea certArea;
    @FXML
    protected Button htmlButton;
    @FXML
    protected ControlFileBackup backupController;

    public SecurityCertificatesController() {
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

    @Override
    public void initControls() {
        try {
            super.initControls();
            tableData = FXCollections.observableArrayList();

            aliasColumn.setCellValueFactory(new PropertyValueFactory<>("alias"));
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("createTime"));
            timeColumn.setCellFactory(new TableTimeCell());

            tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    checkSelected();
                }
            });
            checkSelected();
            tableView.setItems(tableData);

            sourceFileInput.setText(SystemTools.keystore());
            passwordInput.setText(SystemTools.keystorePassword());
            htmlButton.setDisable(true);
            addButton.setDisable(true);
            recoverButton.setDisable(true);

            backupController.setControls(this, baseName);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkSelected() {
        if (isSettingValues) {
            return;
        }
        CertificateEntry selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            certArea.setText("");
            deleteButton.setDisable(true);
        } else {
            certArea.setText(selected.getCertificates());
            deleteButton.setDisable(false);
        }
    }

    @FXML
    @Override
    public void startAction() {
        loadAll(null);
    }

    @FXML
    public void loadAll(String selectAlias) {
        tableView.getItems().clear();
        certArea.setText("");
        htmlButton.setDisable(true);
        addButton.setDisable(true);
        recoverButton.setDisable(true);
        backupController.loadBackups(null);
        cacertsFile = null;
        File file = new File(sourceFileInput.getText());
        if (!file.exists()) {
            return;
        }
        cacertsFile = file;
        recoverButton.setVisible(cacertsFile.getAbsolutePath().equals(SystemTools.myboxCacerts().getAbsolutePath()));
        try {
            synchronized (this) {
                if (task != null && !task.isQuit()) {
                    return;
                }
                task = new SingletonTask<Void>() {
                    private String texts;
                    private List<CertificateEntry> entires;
                    private CertificateEntry selectCert;

                    @Override
                    protected boolean handle() {
                        try {
                            texts = error = null;
                            entires = new ArrayList();
                            selectCert = null;
                            // https://docs.oracle.com/javase/10/docs/api/java/security/KeyStore.html
                            try {
                                char[] passphrase = passwordInput.getText().toCharArray();
                                KeyStore keyStore = KeyStore.getInstance(cacertsFile, passphrase);
                                Enumeration<String> storeAliases = keyStore.aliases();
                                while (storeAliases.hasMoreElements()) {
                                    String alias = storeAliases.nextElement();
                                    if (!keyStore.isCertificateEntry(alias)) {
                                        continue;
                                    }
                                    try {
                                        Certificate[] chain = keyStore.getCertificateChain(alias);
                                        if (chain == null) {
                                            Certificate cert = keyStore.getCertificate(alias);
                                            if (cert != null) {
                                                chain = new Certificate[1];
                                                chain[0] = cert;
                                            }
                                        }
                                        CertificateEntry entry = CertificateEntry.create()
                                                .setAlias(alias)
                                                .setCreateTime(keyStore.getCreationDate(alias).getTime())
                                                .setCertificateChain(chain);
                                        entires.add(entry);
                                        if (selectAlias != null && alias.equals(selectAlias)) {
                                            selectCert = entry;
                                        }
                                    } catch (Exception e) {
                                        error = e.toString();
                                    }
                                }
                                if (selectCert == null) {
                                    StringBuilder s = new StringBuilder();
                                    s.append(cacertsFile).append("\n\n");

                                    s.append("## ").append(message("Type")).append(": ").append(keyStore.getType()).append("   ").
                                            append(message("Size")).append(": ").append(keyStore.size()).
                                            append("\n\n");
                                    for (CertificateEntry entry : entires) {
                                        s.append("#### ").append(message("Alias")).append(": ").append(entry.getAlias()).append("\n");
                                        s.append("----------------------------\n");
                                        if (entry.getCertificateChain() != null) {
                                            for (Certificate cert : entry.getCertificateChain()) {
                                                s.append(cert).append("\n\n");
                                            }
                                        }
                                    }
                                    texts = s.toString();
                                }
                            } catch (Exception e) {
                                error = e.toString();
                            }
                        } catch (Exception e) {
                            error = e.toString();
                        }
                        return !entires.isEmpty();
                    }

                    @Override
                    protected void whenSucceeded() {
                        isSettingValues = true;
                        tableView.getItems().addAll(entires);
                        // https://stackoverflow.com/questions/36240142/sort-tableview-by-certain-column-javafx?r=SearchResults
                        tableView.getSortOrder().add(timeColumn);
                        timeColumn.setSortType(TableColumn.SortType.DESCENDING);
                        tableView.sort();
                        isSettingValues = false;
                        if (selectCert != null) {
                            tableView.scrollTo(selectCert);
                            tableView.getSelectionModel().select(selectCert);
                        } else {
                            certArea.setText(texts);
                        }
                        htmlButton.setDisable(false);
                        addButton.setDisable(false);
                        recoverButton.setDisable(false);
                        bottomLabel.setText(message("Total") + ": " + tableData.size());
                        backupController.loadBackups(cacertsFile);
                    }
                };
                openHandlingStage(task, Modality.WINDOW_MODAL);
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @FXML
    public void htmlAction() {
        if (cacertsFile == null) {
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
                        try {
                            result = error = null;
                            try {
                                char[] passphrase = passwordInput.getText().toCharArray();
                                KeyStore keyStore = KeyStore.getInstance(cacertsFile, passphrase);
                                StringBuilder s = new StringBuilder();
                                s.append("<h1  class=\"center\">").append(cacertsFile.getAbsolutePath()).append("</h1>\n");
                                s.append("<h2  class=\"center\">").
                                        append(message("Type")).append(": ").append(keyStore.getType()).append(" ").
                                        append(message("Size")).append(": ").append(keyStore.size()).
                                        append("</h2>\n");
                                s.append("<hr>\n");
                                Enumeration<String> aliases = keyStore.aliases();
                                while (aliases.hasMoreElements()) {
                                    String alias = aliases.nextElement();
                                    s.append("<h3  class=\"center\">").
                                            append(message("Alias")).append(": ").append(alias).
                                            append("</h3>\n");
                                    Certificate[] chain = keyStore.getCertificateChain(alias);
                                    if (chain != null) {
                                        for (Certificate cert : chain) {
                                            s.append("<pre>").append(cert).append("</pre>\n\n");
                                        }
                                    } else {
                                        Certificate cert = keyStore.getCertificate(alias);
                                        if (cert != null) {
                                            s.append("<pre>").append(cert).append("</pre>\n");
                                        }
                                    }
                                }
                                result = s.toString();
                            } catch (Exception e) {
                                error = e.toString();
                            }
                        } catch (Exception e) {
                            error = e.toString();
                        }
                        return true;
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
                task.setSelf(task);
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

    @FXML
    @Override
    public void addAction(ActionEvent event) {
        if (cacertsFile == null) {
            return;
        }
        try {
            SecurityCertificatesAddController controller
                    = (SecurityCertificatesAddController) openStage(CommonValues.SecurityCertificateAddFxml);
            controller.setCertController(this);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void deleteAction() {
        if (cacertsFile == null) {
            return;
        }
        List<CertificateEntry> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    error = null;
                    try {
                        backupController.addBackup(cacertsFile);
                        List<String> aliases = new ArrayList();
                        for (CertificateEntry cert : selected) {
                            aliases.add(cert.getAlias());
                        }
                        error = NetworkTools.uninstallCertificate(
                                cacertsFile.getAbsolutePath(), passwordInput.getText(),
                                aliases);
                    } catch (Exception e) {
                        error = e.toString();
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (error == null) {
                        startAction();
                        popSuccessful();
                    } else {
                        popError(error);
                    }
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }

    }

    @FXML
    public void recover() {
        if (cacertsFile == null
                || !cacertsFile.getAbsolutePath().equals(SystemTools.myboxCacerts().getAbsolutePath())) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    SystemTools.resetKeystore();
                    NetworkTools.installCertificates();
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    alertInformation(message("TakeEffectWhenReboot"));
                    startAction();
                }
            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void refreshAction() {
        startAction();
    }


    /*
        get/set
     */
    public TextField getPasswordInput() {
        return passwordInput;
    }

    public void setPasswordInput(TextField passwordInput) {
        this.passwordInput = passwordInput;
    }

}
