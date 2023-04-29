package mara.mybox.controller;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mara.mybox.data.PdfInformation;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.PdfTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

/**
 * @Author Mara
 * @CreateDate 2018-9-10
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfAttributesController extends BaseController {

    private PdfInformation pdfInfo;
    private float version;
    private Date createTime, modifyTime;

    @FXML
    protected TextField titleInput, subjectInput, authorInput, creatorInput, producerInput,
            createTimeInput, modifyTimeInput, keywordInput, versionInput;
    @FXML
    protected TextField userPasswordInput, userPasswordInput2, ownerPasswordInput, ownerPasswordInput2;
    @FXML
    protected CheckBox assembleCheck, extractCheck, modifyCheck, fillCheck, printCheck,
            viewPasswordCheck;
    @FXML
    protected RadioButton clearProtectionRadio, changeProtectionRadio;
    @FXML
    protected ToggleGroup protectionGroup;
    @FXML
    protected VBox protectionBox;

    public PdfAttributesController() {
        baseTitle = Languages.message("PDFAttributes");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.PDF);
    }

    @Override
    public void initControls() {
        super.initControls();
        versionInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                try {
                    Float f = Float.parseFloat(newValue);
                    if (f >= 0) {
                        versionInput.setStyle(null);
                        version = f;
                    } else {
                        versionInput.setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                    versionInput.setStyle(UserConfig.badStyle());
                }
            }
        });

        createTimeInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                if (newValue == null || newValue.trim().isEmpty()) {
                    createTime = null;
                    createTimeInput.setStyle(null);
                    return;
                }
                try {
                    Date d = DateTools.encodeDate(newValue, -1);
                    if (d != null) {
                        createTimeInput.setStyle(null);
                        createTime = d;
                    } else {
                        createTimeInput.setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                    createTimeInput.setStyle(UserConfig.badStyle());
                }
            }
        });

        modifyTimeInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                if (newValue == null || newValue.trim().isEmpty()) {
                    modifyTime = null;
                    modifyTimeInput.setStyle(null);
                    return;
                }
                try {
                    Date d = DateTools.encodeDate(newValue, -1);
                    if (d != null) {
                        modifyTimeInput.setStyle(null);
                        modifyTime = d;
                    } else {
                        modifyTimeInput.setStyle(UserConfig.badStyle());
                    }
                } catch (Exception e) {
                    modifyTimeInput.setStyle(UserConfig.badStyle());
                }
            }
        });

        protectionGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                protectionBox.setDisable(!changeProtectionRadio.isSelected());
                if (!changeProtectionRadio.isSelected()) {
                    userPasswordInput.setText("");
                    userPasswordInput2.setText("");
                    ownerPasswordInput.setText("");
                    ownerPasswordInput2.setText("");
                }
            }
        });

        userPasswordInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                checkUserPassword();
            }
        });

        userPasswordInput2.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                checkUserPassword();
            }
        });

        ownerPasswordInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                checkOwnerPassword();
            }
        });

        ownerPasswordInput2.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue ov, String oldValue, String newValue) {
                checkOwnerPassword();
            }
        });

        authorInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                UserConfig.setString("AuthorKey", newValue);
            }
        });
        authorInput.setText(UserConfig.getString("AuthorKey", System.getProperty("user.name")));

        saveButton.disableProperty().bind(
                sourceFileInput.styleProperty().isEqualTo(UserConfig.badStyle())
                        .or(versionInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                        .or(createTimeInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                        .or(modifyTimeInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                        .or(userPasswordInput2.styleProperty().isEqualTo(UserConfig.badStyle()))
                        .or(ownerPasswordInput2.styleProperty().isEqualTo(UserConfig.badStyle()))
        );

    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();
            NodeStyleTools.setTooltip(versionInput, Languages.message("PdfVersionComments"));

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void sourceFileChanged(final File file) {
        sourceFileInput.setStyle(UserConfig.badStyle());
        if (!PdfTools.isPDF(file.getAbsolutePath())) {
            return;
        }
        sourceFile = file;
        loadPdfInformation(null);
    }

    public void checkUserPassword() {
        String p1 = userPasswordInput.getText();
        String p2 = userPasswordInput2.getText();
        boolean valid;
        if (p1 == null || p1.isEmpty()) {
            valid = p2 == null || p2.isEmpty();
        } else {
            valid = p1.equals(p2);
        }
        if (valid) {
            userPasswordInput.setStyle(null);
            userPasswordInput2.setStyle(null);
        } else {
            userPasswordInput2.setStyle(UserConfig.badStyle());
        }
    }

    public void checkOwnerPassword() {
        String p1 = ownerPasswordInput.getText();
        String p2 = ownerPasswordInput2.getText();
        boolean valid;
        if (p1 == null || p1.isEmpty()) {
            valid = p2 == null || p2.isEmpty();
        } else {
            valid = p1.equals(p2);
        }
        if (valid) {
            ownerPasswordInput.setStyle(null);
            ownerPasswordInput2.setStyle(null);
        } else {
            ownerPasswordInput2.setStyle(UserConfig.badStyle());
        }
    }

    public void loadPdfInformation(final String password) {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            infoButton.setDisable(true);
            pdfInfo = null;
            version = -1;
            createTime = null;
            modifyTime = null;
            if (sourceFile == null) {
                return;
            }
            pdfInfo = new PdfInformation(sourceFile);
            task = new SingletonTask<Void>(this) {

                private boolean pop;

                @Override
                protected boolean handle() {
                    ok = false;
                    pop = false;
                    try ( PDDocument doc = PDDocument.load(sourceFile, password, AppVariables.pdfMemUsage)) {
                        pdfInfo.setUserPassword(password);
                        pdfInfo.readInfo(doc);
                        doc.close();
                        ok = true;
                    } catch (InvalidPasswordException e) {
                        pop = true;
                        return false;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    sourceFileInput.setStyle(null);
                    infoButton.setDisable(false);
                    resetAction();
                }

                @Override
                protected void whenFailed() {
                    if (pop) {
                        TextInputDialog dialog = new TextInputDialog();
                        dialog.setContentText(Languages.message("UserPassword"));
                        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                        stage.setAlwaysOnTop(true);
                        stage.toFront();
                        Optional<String> result = dialog.showAndWait();
                        if (result.isPresent()) {
                            loadPdfInformation(result.get());
                        }
                        return;
                    }
                    if (error != null) {
                        popError(Languages.message(error));
                    } else {
                        popFailed();
                    }
                }
            };
            start(task);
        }
    }

    @FXML
    public void resetAction() {
        if (pdfInfo == null) {
            return;
        }
        titleInput.setText(pdfInfo.getTitle());
        subjectInput.setText(pdfInfo.getSubject());
        authorInput.setText(pdfInfo.getAuthor());
        creatorInput.setText(pdfInfo.getCreator());
        producerInput.setText(pdfInfo.getProducer());
        createTime = new Date(pdfInfo.getCreateTime());
        modifyTime = new Date(pdfInfo.getModifyTime());
        createTimeInput.setText(DateTools.datetimeToString(pdfInfo.getCreateTime()));
        modifyTimeInput.setText(DateTools.datetimeToString(pdfInfo.getModifyTime()));
        keywordInput.setText(pdfInfo.getKeywords());
        version = pdfInfo.getVersion();
        versionInput.setText(pdfInfo.getVersion() + "");
        AccessPermission acc = pdfInfo.getAccess();
        if (acc != null) {
            assembleCheck.setSelected(acc.canAssembleDocument());
            extractCheck.setSelected(acc.canExtractContent());
            modifyCheck.setSelected(acc.canModify());
            fillCheck.setSelected(acc.canFillInForm());
            printCheck.setSelected(acc.canPrint());
        }
        ownerPasswordInput.setText(pdfInfo.getOwnerPassword());
        ownerPasswordInput2.setText(pdfInfo.getOwnerPassword());
        userPasswordInput.setText(pdfInfo.getUserPassword());
        userPasswordInput2.setText(pdfInfo.getUserPassword());

    }

    @FXML
    @Override
    public void infoAction() {
        if (pdfInfo == null) {
            return;
        }
        try {
            final PdfInformationController controller = (PdfInformationController) openStage(Fxmls.PdfInformationFxml);
            controller.setInformation(pdfInfo);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    public void nowCreateTime() {
        createTimeInput.setText(DateTools.nowString());
    }

    @FXML
    public void nowModifyTime() {
        modifyTimeInput.setText(DateTools.nowString());
    }

    @FXML
    public void clearUserPassword() {
        userPasswordInput.setText("");
        userPasswordInput2.setText("");
    }

    @FXML
    public void clearOwnerPassword() {
        ownerPasswordInput.setText("");
        ownerPasswordInput2.setText("");
    }

    @FXML
    @Override
    public void saveAction() {
        if (sourceFile == null) {
            return;
        }
        String userPassword = userPasswordInput.getText();
        userPassword = userPassword == null || userPassword.isBlank() ? null : userPassword;
        String ownerPassword = ownerPasswordInput.getText();
        ownerPassword = ownerPassword == null || ownerPassword.isBlank() ? null : ownerPassword;
        if (changeProtectionRadio.isSelected()) {
            if (userPassword != null || ownerPassword != null) {
                if (!PopTools.askSure(getTitle(), Languages.message("SureSetPasswords"))) {
                    return;
                }
            } else {
                if (!PopTools.askSure(getTitle(), Languages.message("SureUnsetPasswords"))) {
                    return;
                }
            }
        }

        final PdfInformation modify = new PdfInformation(sourceFile);
        modify.setAuthor(authorInput.getText());
        modify.setTitle(titleInput.getText());
        modify.setSubject(subjectInput.getText());
        modify.setCreator(creatorInput.getText());
        modify.setProducer(producerInput.getText());
        modify.setKeywords(keywordInput.getText());
        if (modifyTime != null) {
            modify.setModifyTime(modifyTime.getTime());
        }
        if (createTime != null) {
            modify.setCreateTime(createTime.getTime());
        }
        if (version > 0) {
            modify.setVersion(version);
        }

        modify.setUserPassword(userPassword);
        modify.setOwnerPassword(ownerPassword);
        AccessPermission acc = AccessPermission.getOwnerAccessPermission();
        acc.setCanAssembleDocument(assembleCheck.isSelected());
        acc.setCanExtractContent(extractCheck.isSelected());
        acc.setCanExtractForAccessibility(extractCheck.isSelected());
        acc.setCanFillInForm(fillCheck.isSelected());
        acc.setCanModify(modifyCheck.isSelected());
        acc.setCanModifyAnnotations(modifyCheck.isSelected());
        acc.setCanPrint(printCheck.isSelected());
        acc.setCanPrintDegraded(printCheck.isSelected());
        modify.setAccess(acc);

        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>(this) {

                @Override
                protected boolean handle() {
                    return setAttributes(sourceFile, pdfInfo.getUserPassword(), modify);
                }

                @Override
                protected void whenSucceeded() {
                    loadPdfInformation(modify.getUserPassword());
                    popSuccessful();
                }

            };
            start(task);
        }
    }

    private boolean setAttributes(File file, String password, PdfInformation info) {
        try {
            if (file == null || info == null) {
                return false;
            }
            File tmpFile = FileTmpTools.getTempFile();
            FileCopyTools.copyFile(file, tmpFile);
            try ( PDDocument doc = PDDocument.load(tmpFile, password, AppVariables.pdfMemUsage)) {
                PDDocumentInformation docInfo = doc.getDocumentInformation();
                docInfo.setAuthor(info.getAuthor());
                docInfo.setTitle(info.getTitle());
                docInfo.setSubject(info.getSubject());
                docInfo.setCreator(info.getCreator());
                docInfo.setProducer(info.getProducer());
                Calendar c = Calendar.getInstance();
                if (info.getCreateTime() > 0) {
                    c.setTimeInMillis​(info.getCreateTime());
                    docInfo.setCreationDate(c);
                }
                if (info.getModifyTime() > 0) {
                    c.setTimeInMillis​(info.getModifyTime());
                    docInfo.setModificationDate(c);
                }
                docInfo.setKeywords(info.getKeywords());
                doc.setDocumentInformation(docInfo);
                if (info.getVersion() > 0) {
                    doc.setVersion(info.getVersion());
                }

                if (clearProtectionRadio.isSelected()) {
                    doc.setAllSecurityToBeRemoved(true);

                } else if (changeProtectionRadio.isSelected()) {

                    StandardProtectionPolicy policy = new StandardProtectionPolicy(
                            info.getOwnerPassword(), info.getUserPassword(), info.getAccess());
                    doc.protect(policy);
                }

                doc.save(tmpFile);
                doc.close();
            }
            return FileTools.rename(tmpFile, file, true);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return false;
        }
    }

}
