package mara.mybox.controller;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import mara.mybox.data.PdfInformation;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

/**
 * @Author Mara
 * @CreateDate 2018-9-10
 * @Description
 * @License Apache License Version 2.0
 */
public class PdfAttributesBatchController extends PdfBatchController {

    private float version;
    private Date createTime, modifyTime;
    private String newUserPassword, newOwnerPassword;

    @FXML
    protected TextField titleInput, subjectInput, authorInput, creatorInput, producerInput,
            createTimeInput, modifyTimeInput, keywordInput, versionInput;
    @FXML
    protected PasswordField userPasswordInput, userPasswordInput2, ownerPasswordInput, ownerPasswordInput2;
    @FXML
    protected CheckBox titleCheck, subjectCheck, keywordsCheck, creatorCheck, productorCheck,
            authorCheck, versionCheck, createTimeCheck, modifyTimeCheck,
            permissionAssembleCheck, permissionExtractCheck, permissionModifyCheck,
            permissionFillCheck, permissionPrintCheck;
    @FXML
    protected VBox protectionBox;
    @FXML
    protected Button nowCreateButton, nowModifyButton;

    public PdfAttributesBatchController() {
        baseTitle = AppVariables.message("PDFAttributesBatch");
        needUserPassword = false;
        needOwnerPassword = true;
    }

    @Override
    public void initializeNext() {
        try {
            super.initializeNext();

            tableView.getColumns().removeAll(pdfsTableController.fromColumn, pdfsTableController.toColumn);

            pdfsTableController.setPDFPane.getChildren().remove(pdfsTableController.fromToBox);

            pdfsTableController.tableCommentsLabel.setText(message("PdfAttributesTableComments"));

            FxmlControl.setTooltip(pdfsTableController.passwordInput, new Tooltip(message("OwnerPassword")));

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
                    AppVariables.setUserConfigValue("AuthorKey", newValue);
                }
            });
            authorInput.setText(AppVariables.getUserConfigValue("AuthorKey", System.getProperty("user.name")));

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
                            .or(versionInput.styleProperty().isEqualTo(badStyle))
                            .or(createTimeInput.styleProperty().isEqualTo(badStyle))
                            .or(modifyTimeInput.styleProperty().isEqualTo(badStyle))
                            .or(userPasswordInput2.styleProperty().isEqualTo(badStyle))
                            .or(ownerPasswordInput2.styleProperty().isEqualTo(badStyle))
            );
        } catch (Exception e) {
            logger.error(e.toString());
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
                versionInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            versionInput.setStyle(badStyle);
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
                createTimeInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            createTimeInput.setStyle(badStyle);
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
                modifyTimeInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            modifyTimeInput.setStyle(badStyle);
        }
    }

    public void checkUserPassword() {
        String p1 = userPasswordInput.getText();
        String p2 = userPasswordInput2.getText();
        if (p1 == null || p1.isEmpty()) {
            p1 = null;
        }
        if (p2 == null || p2.isEmpty()) {
            p2 = null;
        }
        if ((p1 == null && p2 == null) || (p1 != null && p1.equals(p2))) {
            userPasswordInput.setStyle(null);
            userPasswordInput2.setStyle(null);
            newUserPassword = p1;
        } else {
            userPasswordInput2.setStyle(badStyle);
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
            ownerPasswordInput2.setStyle(badStyle);
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

    @Override
    public boolean makeActualParameters() {
        if (newUserPassword != null || newOwnerPassword != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(myStage.getTitle());
            alert.setContentText(message("SureSetPasswords"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
            ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSure, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != buttonSure) {
                return false;
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(myStage.getTitle());
            alert.setContentText(message("SureUnsetPasswords"));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            ButtonType buttonSure = new ButtonType(AppVariables.message("Sure"));
            ButtonType buttonCancel = new ButtonType(AppVariables.message("Cancel"));
            alert.getButtonTypes().setAll(buttonSure, buttonCancel);
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.setAlwaysOnTop(true);
            stage.toFront();

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != buttonSure) {
                return false;
            }
        }

        return super.makeActualParameters();
    }

    @Override
    public String handleFile(File srcFile, File targetPath) {
        try {
            countHandling(srcFile);
            PdfInformation rowInfo = tableData.get(currentParameters.currentIndex);
            String filePassword = rowInfo.getOwnerPassword();
            try ( PDDocument pd = PDDocument.load(srcFile, filePassword, AppVariables.pdfMemUsage)) {
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
                pd.save(srcFile);
                pd.close();
            }
            return message("Successful");
        } catch (Exception e) {
            logger.debug(e.toString());
            if (e.toString().contains("the password is incorrect")) {
                return message("PasswordIncorrect");
            } else {
                return message("Failed");
            }
        }

    }

}
