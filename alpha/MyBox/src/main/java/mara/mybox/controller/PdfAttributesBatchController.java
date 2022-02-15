package mara.mybox.controller;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import mara.mybox.data.PdfInformation;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.fxml.NodeTools;
import mara.mybox.value.UserConfig;
import mara.mybox.fxml.PopTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileCopyTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
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
public class PdfAttributesBatchController extends BaseBatchPdfController {

    private float version;
    private Date createTime, modifyTime;
    private String newUserPassword, newOwnerPassword;

    @FXML
    protected TextField titleInput, subjectInput, authorInput, creatorInput, producerInput,
            createTimeInput, modifyTimeInput, keywordInput, versionInput;
    @FXML
    protected TextField userPasswordInput, userPasswordInput2, ownerPasswordInput, ownerPasswordInput2;
    @FXML
    protected CheckBox titleCheck, subjectCheck, keywordsCheck, creatorCheck, productorCheck,
            authorCheck, versionCheck, createTimeCheck, modifyTimeCheck,
            permissionAssembleCheck, permissionExtractCheck, permissionModifyCheck,
            permissionFillCheck, permissionPrintCheck;
    @FXML
    protected VBox protectionBox;
    @FXML
    protected Button nowCreateButton, nowModifyButton;
    @FXML
    protected RadioButton clearProtectionRadio, changeProtectionRadio;
    @FXML
    protected ToggleGroup protectionGroup;

    public PdfAttributesBatchController() {
        baseTitle = Languages.message("PDFAttributesBatch");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            tableView.getColumns().removeAll(pdfsTableController.fromColumn, pdfsTableController.toColumn);

            pdfsTableController.setPDFPane.getChildren().remove(pdfsTableController.fromToBox);

            titleCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    titleInput.setDisable(!titleCheck.isSelected());
                }
            });
            subjectCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    subjectInput.setDisable(!subjectCheck.isSelected());
                }
            });

            keywordsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    keywordInput.setDisable(!keywordsCheck.isSelected());
                }
            });
            creatorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    creatorInput.setDisable(!creatorCheck.isSelected());
                }
            });
            productorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    producerInput.setDisable(!productorCheck.isSelected());
                }
            });
            authorCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    authorInput.setDisable(!authorCheck.isSelected());
                }
            });
            authorInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    UserConfig.setString("AuthorKey", newValue);
                }
            });
            authorInput.setText(UserConfig.getString("AuthorKey", System.getProperty("user.name")));

            versionCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    versionInput.setDisable(!versionCheck.isSelected());
                    checkVersion();
                }
            });
            versionInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkVersion();
                }
            });

            createTimeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    createTimeInput.setDisable(!createTimeCheck.isSelected());
                    nowCreateButton.setDisable(!createTimeCheck.isSelected());
                    checkCreateTime();
                }
            });
            createTimeInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkCreateTime();
                }
            });

            modifyTimeCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    modifyTimeInput.setDisable(!modifyTimeCheck.isSelected());
                    nowModifyButton.setDisable(!modifyTimeCheck.isSelected());
                    checkModifyTime();
                }
            });
            modifyTimeInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkModifyTime();
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

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(tableView.getItems())
                            .or(versionInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(createTimeInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(modifyTimeInput.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(userPasswordInput2.styleProperty().isEqualTo(UserConfig.badStyle()))
                            .or(ownerPasswordInput2.styleProperty().isEqualTo(UserConfig.badStyle()))
            );
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkVersion() {
        if (!versionCheck.isSelected()) {
            versionInput.setStyle(null);
            return;
        }
        try {
            Float f = Float.parseFloat(versionInput.getText());
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

    public void checkCreateTime() {
        if (!createTimeCheck.isSelected()) {
            createTimeCheck.setStyle(null);
            return;
        }
        String s = createTimeInput.getText();
        if (s == null || s.trim().isEmpty()) {
            createTime = null;
            createTimeInput.setStyle(null);
            return;
        }
        try {
            Date d = DateTools.stringToDatetime(s);
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

    public void checkModifyTime() {
        if (!modifyTimeCheck.isSelected()) {
            modifyTimeCheck.setStyle(null);
            return;
        }
        String s = modifyTimeInput.getText();
        if (s == null || s.trim().isEmpty()) {
            modifyTime = null;
            modifyTimeInput.setStyle(null);
            return;
        }
        try {
            Date d = DateTools.stringToDatetime(s);
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

    public void checkUserPassword() {
        String p1 = userPasswordInput.getText();
        String p2 = userPasswordInput2.getText();
        if (p1 == null || p1.isBlank()) {
            p1 = null;
        }
        if (p2 == null || p2.isBlank()) {
            p2 = null;
        }
        if ((p1 == null && p2 == null) || (p1 != null && p1.equals(p2))) {
            userPasswordInput.setStyle(null);
            userPasswordInput2.setStyle(null);
            newUserPassword = p1;
        } else {
            userPasswordInput2.setStyle(UserConfig.badStyle());
        }
    }

    public void checkOwnerPassword() {
        String p1 = ownerPasswordInput.getText();
        String p2 = ownerPasswordInput2.getText();
        if (p1 == null || p1.isEmpty()) {
            p1 = null;
        }
        if (p2 == null || p2.isEmpty()) {
            p2 = null;
        }
        if ((p1 == null && p2 == null) || (p1 != null && p1.equals(p2))) {
            ownerPasswordInput.setStyle(null);
            ownerPasswordInput2.setStyle(null);
            newOwnerPassword = p1;
        } else {
            ownerPasswordInput2.setStyle(UserConfig.badStyle());
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
        newUserPassword = null;
    }

    @FXML
    public void clearOwnerPassword() {
        ownerPasswordInput.setText("");
        ownerPasswordInput2.setText("");
        newOwnerPassword = null;
    }

    @Override
    public boolean makeActualParameters() {
        if (changeProtectionRadio.isSelected()) {
            if (newUserPassword != null || newOwnerPassword != null) {
                if (!PopTools.askSure(this,myStage.getTitle(), Languages.message("SureSetPasswords"))) {
                    return false;
                }
            } else {
                if (!PopTools.askSure(this,myStage.getTitle(), Languages.message("SureUnsetPasswords"))) {
                    return false;
                }
            }
        }

        return super.makeActualParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            PdfInformation rowInfo = tableData.get(currentParameters.currentIndex);
            String filePassword = rowInfo.getUserPassword();
            File tmpFile = TmpFileTools.getTempFile();
            FileCopyTools.copyFile(srcFile, tmpFile);
            try ( PDDocument pd = PDDocument.load(tmpFile, filePassword, AppVariables.pdfMemUsage)) {
                PDDocumentInformation docInfo = pd.getDocumentInformation();
                if (authorCheck.isSelected()) {
                    docInfo.setAuthor(authorInput.getText());
                }
                if (titleCheck.isSelected()) {
                    docInfo.setTitle(titleInput.getText());
                }
                if (subjectCheck.isSelected()) {
                    docInfo.setSubject(subjectInput.getText());
                }
                if (creatorCheck.isSelected()) {
                    docInfo.setCreator(creatorInput.getText());
                }
                if (productorCheck.isSelected()) {
                    docInfo.setProducer(producerInput.getText());
                }
                if (keywordsCheck.isSelected()) {
                    docInfo.setKeywords(keywordInput.getText());
                }
                Calendar c = Calendar.getInstance();
                if (createTimeCheck.isSelected() && createTime != null) {
                    c.setTimeInMillis​(createTime.getTime());
                    docInfo.setCreationDate(c);
                }
                if (modifyTimeCheck.isSelected() && modifyTime != null) {
                    c.setTimeInMillis​(modifyTime.getTime());
                    docInfo.setModificationDate(c);
                }
                pd.setDocumentInformation(docInfo);

                if (versionCheck.isSelected() && version > 0) {
                    pd.setVersion(version);
                }

                // https://stackoverflow.com/questions/63653107/pdfbox-disable-copy-paste-with-standardprotectionpolicy?r=SearchResults
                // If a program can open a PDF for reading, that program can do anything with the PDF, no matter how restricted it is configured to be.
                if (clearProtectionRadio.isSelected()) {
                    pd.setAllSecurityToBeRemoved(true);

                } else if (changeProtectionRadio.isSelected()) {
                    AccessPermission acc = AccessPermission.getOwnerAccessPermission();
                    acc.setCanAssembleDocument(permissionAssembleCheck.isSelected());
                    acc.setCanExtractContent(permissionExtractCheck.isSelected());
                    acc.setCanExtractForAccessibility(permissionExtractCheck.isSelected());
                    acc.setCanFillInForm(permissionFillCheck.isSelected());
                    acc.setCanModify(permissionModifyCheck.isSelected());
                    acc.setCanModifyAnnotations(permissionModifyCheck.isSelected());
                    acc.setCanPrint(permissionPrintCheck.isSelected());
                    acc.setCanPrintDegraded(permissionPrintCheck.isSelected());

                    StandardProtectionPolicy policy = new StandardProtectionPolicy(newOwnerPassword, newUserPassword, acc);
                    pd.protect(policy);
                }
                pd.save(tmpFile);
                pd.close();
            }
            if (FileTools.rename(tmpFile, srcFile, true)) {
                return Languages.message("Successful");
            } else {
                return Languages.message("Failed");
            }

        } catch (InvalidPasswordException e) {
            return Languages.message("PasswordIncorrect");
        } catch (Exception e) {
            return e.toString();
        }
    }

}
