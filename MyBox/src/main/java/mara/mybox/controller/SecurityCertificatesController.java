package mara.mybox.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import mara.mybox.data.CertificateEntry;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.fxml.TableDateCell;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.NetworkTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
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
    protected File lastBackup;

    @FXML
    protected TextField passwordInput;
    @FXML
    protected TableView<CertificateEntry> tableView;
    @FXML
    protected TableColumn<CertificateEntry, String> aliasColumn, timeColumn;
    @FXML
    protected TextArea certArea;
    @FXML
    protected Button plusButton, htmlButton;
    @FXML
    protected CheckBox backupCheck;

    public SecurityCertificatesController() {
        baseTitle = AppVariables.message("SecurityCertificates");

        SourceFileType = VisitHistory.FileType.Certificate;
        SourcePathType = VisitHistory.FileType.Certificate;
        TargetPathType = VisitHistory.FileType.Html;
        TargetFileType = VisitHistory.FileType.Html;

        sourcePathKey = "CertificateFilePath";
        targetPathKey = "HtmlFilePath";
        sourceExtensionFilter = CommonFxValues.KeyStoreExtensionFilter;
        targetExtensionFilter = CommonFxValues.HtmlExtensionFilter;
    }

    @Override
    public void initializeNext() {
        try {
            tableData = FXCollections.observableArrayList();

            aliasColumn.setCellValueFactory(new PropertyValueFactory<>("alias"));
            timeColumn.setCellValueFactory(new PropertyValueFactory<>("createTime"));
            timeColumn.setCellFactory(new TableDateCell());

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
            plusButton.setDisable(true);

        } catch (Exception e) {
            logger.error(e.toString());
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
    public void readAction() {
        loadAll(null);
    }

    @FXML
    public void loadAll(String selectAlias) {
        lastBackup = null;
        tableView.getItems().clear();
        certArea.setText("");
        htmlButton.setDisable(true);
        plusButton.setDisable(true);
        if (sourceFileInput.getText().isEmpty() || passwordInput.getText().isEmpty()) {
            return;
        }
        try {
            synchronized (this) {
                if (task != null) {
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
                                File keyStoreFile = new File(sourceFileInput.getText());
                                KeyStore keyStore = KeyStore.getInstance(keyStoreFile, passphrase);

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
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (!entires.isEmpty()) {
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
                            plusButton.setDisable(false);
                            bottomLabel.setText(message("Total") + ": " + tableData.size());
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
    public void htmlAction() {
        if (sourceFileInput.getText().isEmpty() || passwordInput.getText().isEmpty()) {
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
                        try {
                            result = error = null;
                            try {
                                File keyStoreFile = new File(sourceFileInput.getText());
                                char[] passphrase = passwordInput.getText().toCharArray();
                                KeyStore keyStore = KeyStore.getInstance(keyStoreFile, passphrase);
                                StringBuilder s = new StringBuilder();
                                s.append("<h1  class=\"center\">").append(keyStoreFile.getAbsolutePath()).append("</h1>\n");
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
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    @FXML
    public void plusAction() {
        try {
            SecurityCertificatesAddController controller
                    = (SecurityCertificatesAddController) openStage(CommonValues.SecurityCertificateAddFxml);
            controller.setCertController(this);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public boolean backupKeyStore() {
        if (!backupCheck.isSelected()) {
            return true;
        }
        sourceFile = new File(sourceFileInput.getText());
        if (!sourceFile.exists() || !sourceFile.isFile()) {
            popError(message("NotExist"));
            return false;
        }
        try {
            File newKsFile = new File(sourceFile.getParentFile().getAbsolutePath() + File.separator
                    + FileTools.appendName(sourceFile.getName(), DateTools.nowString4()));
            Files.copy(Paths.get(sourceFile.getAbsolutePath()), Paths.get(newKsFile.getAbsolutePath()),
                    StandardCopyOption.COPY_ATTRIBUTES);
            lastBackup = newKsFile;
            bottomLabel.setText(message("Total") + ": " + tableData.size() + "   "
                    + message("KeyStoreBacked") + ": " + lastBackup
            );
            return true;
        } catch (Exception e) {
            popError(e.toString());
            return false;
        }
    }

    @FXML
    @Override
    public void deleteAction() {
        List<CertificateEntry> selected = tableView.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty() || !backupKeyStore()) {
            return;
        }
        List<String> aliases = new ArrayList();
        for (CertificateEntry cert : selected) {
            aliases.add(cert.getAlias());
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
                        try {
                            error = NetworkTools.uninstallCertificate(
                                    sourceFileInput.getText(), passwordInput.getText(),
                                    aliases);
                        } catch (Exception e) {
                            error = e.toString();
                        }
                        return true;
                    }

                    @Override
                    protected void whenSucceeded() {
                        if (error == null) {
                            readAction();
                            popSuccessul();
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
    public TextField getPasswordInput() {
        return passwordInput;
    }

    public void setPasswordInput(TextField passwordInput) {
        this.passwordInput = passwordInput;
    }

}
