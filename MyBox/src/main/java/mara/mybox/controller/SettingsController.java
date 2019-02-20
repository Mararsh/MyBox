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
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.db.TableImageHistory;
import mara.mybox.db.TableImageInit;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.getMessage;
import mara.mybox.value.CommonValues;
import mara.mybox.fxml.FxmlTools;
import static mara.mybox.fxml.FxmlTools.badStyle;
import static mara.mybox.value.AppVaribles.getUserConfigValue;

/**
 * @Author Mara
 * @CreateDate 2018-10-14
 * @Description
 * @License Apache License Version 2.0
 */
public class SettingsController extends BaseController {

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
    protected ComboBox<String> styleBox, imageWidthBox, fontSizeBox;
    @FXML
    protected Button hisClearButton, hisOkButton;
    @FXML
    protected HBox pdfMemBox, imageHisBox;

    public SettingsController() {
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

            fontSizeBox.getItems().addAll(Arrays.asList(
                    "9", "10", "12", "14", "15", "16", "17", "18", "19", "20", "21", "22"));
            fontSizeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.valueOf(newValue);
                            setPaneFontSize(v);
                        } catch (Exception e) {
                        }
                    }
                }
            });
            fontSizeBox.getSelectionModel().select(AppVaribles.getPaneFontSize() + "");

            stopAlarmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVaribles.setUserConfigValue("StopAlarmsWhenExit", stopAlarmCheck.isSelected());
                }
            });

            showCommentsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVaribles.setUserConfigValue("ShowComments", showCommentsCheck.isSelected());
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
                        AppVaribles.setUserConfigValue(CommonValues.userTempPathKey, file.getAbsolutePath());
                    } catch (Exception e) {
                    }
                }

            });
            tempDirInput.setText(AppVaribles.getUserConfigPath(CommonValues.userTempPathKey).getAbsolutePath());

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

            imageWidthBox.getItems().addAll(Arrays.asList(
                    "4096", "2048", "8192", "1024", "10240", "6144", "512", "15360", "20480", "30720"));
            imageWidthBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (newValue != null && !newValue.isEmpty()) {
                        try {
                            int v = Integer.valueOf(newValue);
                            if (v > 0) {
                                AppVaribles.setUserConfigInt("MaxImageSampleWidth", v);
                                imageWidthBox.getEditor().setStyle(null);
                            } else {
                                imageWidthBox.getEditor().setStyle(badStyle);
                            }
                        } catch (Exception e) {
                            imageWidthBox.getEditor().setStyle(badStyle);
                        }
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

            stopAlarmCheck.setSelected(AppVaribles.getUserConfigBoolean("StopAlarmsWhenExit"));

            showCommentsCheck.setSelected(AppVaribles.isShowComments());

            hisMaxInput.setText(AppVaribles.getUserConfigInt("MaxImageHistories", 20) + "");
            if (AppVaribles.getUserConfigBoolean("ImageHis")) {
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

            String style = AppVaribles.getUserConfigValue("InterfaceStyle", CommonValues.DefaultStyle);
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

            imageWidthBox.getSelectionModel().select(AppVaribles.getUserConfigInt("MaxImageSampleWidth", 4096) + "");

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
        String pm = getUserConfigValue("PdfMemDefault", "1GB");
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
            AppVaribles.setUserConfigValue("InterfaceStyle", style);
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
        AppVaribles.setUserConfigValue("AlphaAsBlack", false);
    }

    @FXML
    protected void replaceBlackAction(ActionEvent event) {
        AppVaribles.setUserConfigValue("AlphaAsBlack", true);
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
    protected void clearHistories(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(getBaseTitle());
        alert.setContentText(AppVaribles.getMessage("SureClear"));
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK) {
            return;
        }
        new TableImageInit().clear();
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
        AppVaribles.setUserConfigValue("ImageHis", true);
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
                AppVaribles.setUserConfigInt("MaxImageHistories", v);
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
        AppVaribles.setUserConfigValue("ImageHis", false);
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
            File path = AppVaribles.getUserTempPath();
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            File directory = chooser.showDialog(getMyStage());
            if (directory == null) {
                return;
            }
            if (CommonValues.AppDataPaths.contains(directory)) {
                alertError(AppVaribles.getMessage("DirectoryReserved"));
                return;
            }
            AppVaribles.setUserConfigValue(LastPathKey, directory.getPath());
            AppVaribles.setUserConfigValue(CommonValues.userTempPathKey, directory.getPath());

            tempDirInput.setText(directory.getPath());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    @FXML
    @Override
    protected void clearSettings(ActionEvent event) {
        super.clearSettings(event);
        BaseController c = getParentController();
        String f = c.getMyFxml();
        if (f.contains("ImageManufacture") && !f.contains("ImageManufactureBatch")) {
            f = CommonValues.ImageManufactureFileFxml;
        }
        c.reloadStage(f, c.getMyStage().getTitle());
        f = getMyFxml();
        if (f.contains("ImageManufacture") && !f.contains("ImageManufactureBatch")) {
            f = CommonValues.ImageManufactureFileFxml;
        }
        reloadStage(f, getMyStage().getTitle());
//        popInformation(AppVaribles.getMessage("Successful"));
    }

    @FXML
    private void close() {
        closeStage();
    }

}
