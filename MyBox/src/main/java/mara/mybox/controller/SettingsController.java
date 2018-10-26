package mara.mybox.controller;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.db.TableImageHistory;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getConfigValue;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import mara.mybox.tools.FxmlTools;
import static mara.mybox.tools.FxmlTools.badStyle;

/**
 * @Author Mara
 * @CreateDate 2018-10-14
 * @Description
 * @License Apache License Version 2.0
 */
public class SettingsController extends BaseController {

    final protected String TempDirKey;

    @FXML
    private ToggleGroup langGroup, alphaGroup, pdfMemGroup, hisGroup;
    @FXML
    private RadioButton chineseRadio, englishRadio, alphaBlackRadio, alphaWhiteRadio;
    @FXML
    private RadioButton pdfMem500MRadio, pdfMem1GRadio, pdfMem2GRadio, pdfMemUnlimitRadio, maxHisRadio, noHisRadio;
    @FXML
    private CheckBox showCommentsCheck, stopAlarmCheck;
    @FXML
    private TextField hisMaxInput, tempDirInput;
    @FXML
    protected ComboBox<String> styleBox;
    @FXML
    protected Button hisClearButton, hisOkButton;
    @FXML
    protected HBox pdfMemBox, imageHisBox;

    public SettingsController() {
        TempDirKey = "TempDir";
    }

    @Override
    protected void initializeNext() {
        try {

            langGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkLanguage();
                }
            });

            stopAlarmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVaribles.setConfigValue("StopAlarmsWhenExit", stopAlarmCheck.isSelected());
                }
            });

            showCommentsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVaribles.setConfigValue("ShowComments", showCommentsCheck.isSelected());
                }
            });

            Tooltip tips = new Tooltip(getMessage("PdfMemComments"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(pdfMemBox, tips);

            tips = new Tooltip(getMessage("ImageHisComments"));
            tips.setFont(new Font(16));
            FxmlTools.quickTooltip(imageHisBox, tips);

            hisMaxInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkMaxHis();
                }
            });

            tempDirInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        final File file = new File(newValue);
                        if (!file.exists() || !file.isDirectory()) {
                            tempDirInput.setStyle(badStyle);
                            return;
                        }
                        tempDirInput.setStyle(null);
                        AppVaribles.setConfigValue(TempDirKey, file.getAbsolutePath());
                    } catch (Exception e) {
                    }
                }

            });
            tempDirInput.setText(AppVaribles.getConfigValue(TempDirKey, CommonValues.UserFilePath));

            styleBox.getItems().addAll(Arrays.asList(
                    getMessage("DefaultStyle"), getMessage("caspianStyle"),
                    getMessage("WhiteOnBlackStyle"), getMessage("PinkOnBlackStyle"),
                    getMessage("YellowOnBlackStyle"), getMessage("GreenOnBlackStyle"),
                    getMessage("WhiteOnBlueStyle"), getMessage("WhiteOnGreenStyle"),
                    getMessage("WhiteOnVioletredStyle")));
            styleBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        checkStyle(newValue);
                    }
                }
            });

            initValues();

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void initValues() {
        try {
//            logger.debug("initValues");

            stopAlarmCheck.setSelected(AppVaribles.getConfigBoolean("StopAlarmsWhenExit"));

            showCommentsCheck.setSelected(AppVaribles.isShowComments());

            hisMaxInput.setText(AppVaribles.getConfigInt("MaxImageHistories", 20) + "");
            if (AppVaribles.getConfigBoolean("ImageHis")) {
                hisMaxInput.setDisable(false);
                hisOkButton.setDisable(false);
                hisClearButton.setDisable(false);
                checkMaxHis();

                maxHisRadio.setSelected(true);

            } else {
                hisMaxInput.setStyle(null);
                hisMaxInput.setDisable(true);
                hisOkButton.setDisable(true);
                hisClearButton.setDisable(true);

                noHisRadio.setSelected(true);
            }

            String style = AppVaribles.getConfigValue("InterfaceStyle", CommonValues.DefaultStyle);
            switch (style) {
                case CommonValues.DefaultStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("DefaultStyle"));
                    break;
                case CommonValues.caspianStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("caspianStyle"));
                    break;
                case CommonValues.WhiteOnBlackStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("WhiteOnBlackStyle"));
                    break;
                case CommonValues.PinkOnBlackStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("PinkOnBlackStyle"));
                    break;
                case CommonValues.YellowOnBlackStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("YellowOnBlackStyle"));
                    break;
                case CommonValues.GreenOnBlackStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("GreenOnBlackStyle"));
                    break;
                case CommonValues.WhiteOnBlueStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("WhiteOnBlueStyle"));
                    break;
                case CommonValues.WhiteOnGreenStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("WhiteOnGreenStyle"));
                    break;
                case CommonValues.WhiteOnPurpleStyle:
                    styleBox.getSelectionModel().select(AppVaribles.getMessage("WhiteOnVioletredStyle"));
                    break;
                default:
                    break;
            }

            checkLanguage();
            checkAlpha();
            checkPdfMem();

        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void checkLanguage() {
        if (AppVaribles.CurrentBundle == CommonValues.BundleZhCN) {
            chineseRadio.setSelected(true);
        } else {
            englishRadio.setSelected(true);
        }
    }

    protected void checkAlpha() {
        if (AppVaribles.isAlphaAsBlack()) {
            alphaBlackRadio.setSelected(true);
        } else {
            alphaWhiteRadio.setSelected(true);
        }
    }

    protected void checkPdfMem() {
        String pm = getConfigValue("PdfMemDefault", "1GB");
        switch (pm) {
            case "1GB":
                pdfMem1GRadio.setSelected(true);
                break;
            case "2GB":
                pdfMem2GRadio.setSelected(true);
                break;
            case "Unlimit":
                pdfMemUnlimitRadio.setSelected(true);
                break;
            case "500MB":
            default:
                pdfMem500MRadio.setSelected(true);
        }
    }

    protected void checkStyle(String s) {
        try {
            if (getMessage("DefaultStyle").equals(s)) {
                setStyle(CommonValues.DefaultStyle);
            } else if (getMessage("caspianStyle").equals(s)) {
                setStyle(CommonValues.caspianStyle);
            } else if (getMessage("WhiteOnBlackStyle").equals(s)) {
                setStyle(CommonValues.WhiteOnBlackStyle);
            } else if (getMessage("PinkOnBlackStyle").equals(s)) {
                setStyle(CommonValues.PinkOnBlackStyle);
            } else if (getMessage("YellowOnBlackStyle").equals(s)) {
                setStyle(CommonValues.YellowOnBlackStyle);
            } else if (getMessage("GreenOnBlackStyle").equals(s)) {
                setStyle(CommonValues.GreenOnBlackStyle);
            } else if (getMessage("WhiteOnBlueStyle").equals(s)) {
                setStyle(CommonValues.WhiteOnBlueStyle);
            } else if (getMessage("WhiteOnGreenStyle").equals(s)) {
                setStyle(CommonValues.WhiteOnGreenStyle);
            } else if (getMessage("WhiteOnVioletredStyle").equals(s)) {
                setStyle(CommonValues.WhiteOnPurpleStyle);
            }
        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    public void setStyle(String style) {
        try {
            AppVaribles.setConfigValue("InterfaceStyle", style);
            if (getParentController() != null) {
                getParentController().setInterfaceStyle(style);
            }
            setInterfaceStyle(style);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    private void checkMaxHis() {
        try {
            int v = Integer.valueOf(hisMaxInput.getText());
            if (v > 0) {
                hisMaxInput.setStyle(null);
            } else {
                hisMaxInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            hisMaxInput.setStyle(badStyle);
        }
    }

    @FXML
    protected void setChinese(ActionEvent event) {
        AppVaribles.setLanguage("zh");
        reload();
    }

    @FXML
    protected void setEnglish(ActionEvent event) {
        AppVaribles.setLanguage("en");
        reload();
    }

    private void reload() {
        BaseController c = reloadStage(getMyFxml(), AppVaribles.getMessage("AppTitle"));
        if (parentController != null) {
            c.setParentController(parentController);
            if (parentFxml != null) {
                c.setParentFxml(parentFxml);
                BaseController p;
                if (parentFxml.contains("ImageManufacture") && !parentFxml.contains("ImageManufactureBatch")) {
                    p = parentController.reloadStage(CommonValues.ImageManufactureFileFxml, parentController.getMyStage().getTitle());
                } else {
                    p = parentController.reloadStage(parentFxml, parentController.getMyStage().getTitle());
                }
                c.setParentController(p);
                c.setParentFxml(p.getMyFxml());
            }
        }
    }

    @FXML
    protected void replaceWhiteAction(ActionEvent event) {
        AppVaribles.setConfigValue("AlphaAsBlack", false);
    }

    @FXML
    protected void replaceBlackAction(ActionEvent event) {
        AppVaribles.setConfigValue("AlphaAsBlack", true);
    }

    @FXML
    protected void PdfMem500MB(ActionEvent event) {
        AppVaribles.setPdfMem("500MB");
    }

    @FXML
    protected void PdfMem1GB(ActionEvent event) {
        AppVaribles.setPdfMem("1GB");
    }

    @FXML
    protected void PdfMem2GB(ActionEvent event) {
        AppVaribles.setPdfMem("2GB");
    }

    @FXML
    protected void pdfMemUnlimit(ActionEvent event) {
        AppVaribles.setPdfMem("Unlimit");
    }

    @FXML
    protected void clearSettings(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getBaseTitle());
        alert.setContentText(AppVaribles.getMessage("SureClear"));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK) {
            return;
        }
        AppVaribles.clear();
        reload();
        popInformation(AppVaribles.getMessage("Successful"));
    }

    @FXML
    protected void clearHistories(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getBaseTitle());
        alert.setContentText(AppVaribles.getMessage("SureClear"));
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK) {
            return;
        }
        new TableImageHistory().clear();
        if (parentController != null && parentFxml != null
                && parentFxml.contains("ImageManufacture") && !parentFxml.contains("ImageManufactureBatch")) {
            ImageManufactureController p = (ImageManufactureController) parentController;
            p.updateHisBox();
        }
        popInformation(AppVaribles.getMessage("Successful"));
    }

    @FXML
    protected void maxHisAction(ActionEvent event) {
        AppVaribles.setConfigValue("ImageHis", true);
        hisMaxInput.setDisable(false);
        hisOkButton.setDisable(false);
        hisClearButton.setDisable(false);
        checkMaxHis();
        if (parentController != null && parentFxml != null
                && parentFxml.contains("ImageManufacture") && !parentFxml.contains("ImageManufactureBatch")) {
            ImageManufactureController p = (ImageManufactureController) parentController;
            p.updateHisBox();
        }
    }

    @FXML
    protected void setHisAction(ActionEvent event) {
        try {
            int v = Integer.valueOf(hisMaxInput.getText());
            if (v > 0) {
                hisMaxInput.setStyle(null);
                AppVaribles.setConfigInt("MaxImageHistories", v);
                if (parentController != null && parentFxml != null
                        && parentFxml.contains("ImageManufacture") && !parentFxml.contains("ImageManufactureBatch")) {
                    ImageManufactureController p = (ImageManufactureController) parentController;
                    p.updateHisBox();
                }
            } else {
                hisMaxInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            hisMaxInput.setStyle(badStyle);
        }
    }

    @FXML
    protected void noHisAction(ActionEvent event) {
        AppVaribles.setConfigValue("ImageHis", false);
        hisMaxInput.setStyle(null);
        hisMaxInput.setDisable(true);
        hisOkButton.setDisable(true);
        hisClearButton.setDisable(true);
        if (parentController != null && parentFxml != null
                && parentFxml.contains("ImageManufacture") && !parentFxml.contains("ImageManufactureBatch")) {
            ImageManufactureController p = (ImageManufactureController) parentController;
            p.updateHisBox();
        }
    }

    @FXML
    protected void selectTemp(ActionEvent event) {
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = new File(AppVaribles.getConfigValue(TempDirKey, CommonValues.UserFilePath));
            if (!path.isDirectory()) {
                path = new File(CommonValues.UserFilePath);
            }
            chooser.setInitialDirectory(path);
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            AppVaribles.setConfigValue(LastPathKey, directory.getPath());
            AppVaribles.setConfigValue(TempDirKey, directory.getPath());

            tempDirInput.setText(directory.getPath());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    private void close() {
        closeStage();
    }

}
