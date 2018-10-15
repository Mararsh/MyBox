package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import static mara.mybox.controller.BaseController.logger;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.getConfigValue;
import static mara.mybox.objects.AppVaribles.getMessage;
import mara.mybox.objects.CommonValues;
import static mara.mybox.tools.FxmlTools.badStyle;

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
    private TextField hisMaxInput;
    @FXML
    protected ComboBox<String> styleBox;

    @Override
    protected void initializeNext() {
        try {

            checkLanguage();
            checkAlpha();
            checkPdfMem();

            stopAlarmCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVaribles.setConfigValue("StopAlarmsWhenExit", stopAlarmCheck.isSelected());
                }
            });
            stopAlarmCheck.setSelected(AppVaribles.getConfigBoolean("StopAlarmsWhenExit"));

            showCommentsCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) {
                    AppVaribles.setConfigValue("ShowComments", showCommentsCheck.isSelected());
                    AppVaribles.showComments = showCommentsCheck.isSelected();
                }
            });
            showCommentsCheck.setSelected(AppVaribles.showComments);

            hisMaxInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    checkMaxHis();
                }
            });
            hisMaxInput.setText(AppVaribles.getConfigValue("ImageMaxHisKey", "10"));
            if (AppVaribles.getConfigBoolean("ImageHisKey")) {
                maxHisRadio.setSelected(true);
            } else {
                noHisRadio.setSelected(true);
            }

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
        if (AppVaribles.alphaAsBlack) {
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
            AppVaribles.currentStyle = style;
            AppVaribles.setConfigValue("InterfaceStyle", AppVaribles.currentStyle);
            if (getParentController() != null) {
                getParentController().setInterfaceStyle(style);
            }
            setInterfaceStyle(AppVaribles.currentStyle);
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    private void checkMaxHis() {
        try {
            int v = Integer.valueOf(hisMaxInput.getText());
            if (v > 0) {
                hisMaxInput.setStyle(null);
                AppVaribles.setConfigInt("ImageMaxHisKey", v);
            } else {
                hisMaxInput.setStyle(badStyle);
            }
        } catch (Exception e) {
            hisMaxInput.setStyle(badStyle);
        }
    }

    @FXML
    protected void setChinese(ActionEvent event) {
        AppVaribles.setCurrentBundle("zh");
        reload();
    }

    @FXML
    protected void setEnglish(ActionEvent event) {
        AppVaribles.setCurrentBundle("en");
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
                    p = parentController.reloadStage(CommonValues.ImageManufactureFileFxml, AppVaribles.getMessage("ImageManufacture"));
                } else {
                    p = parentController.reloadStage(parentFxml, AppVaribles.getMessage("AppTitle"));
                }
                c.setParentController(p);
                c.setParentFxml(p.getMyFxml());
            }
        }
    }

    @FXML
    protected void replaceWhiteAction(ActionEvent event) {
        AppVaribles.setConfigValue("AlphaAsBlack", false);
        AppVaribles.alphaAsBlack = false;
    }

    @FXML
    protected void replaceBlackAction(ActionEvent event) {
        AppVaribles.setConfigValue("AlphaAsBlack", true);
        AppVaribles.alphaAsBlack = true;
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
        try {
            File configFile = new File(CommonValues.UserConfigFile);
            if (!configFile.exists()) {
                configFile.createNewFile();
            } else {
                try (FileWriter fileWriter = new FileWriter(configFile)) {
                    fileWriter.write("");
                    fileWriter.flush();
                }
                popInformation(AppVaribles.getMessage("Successful"));
            }
        } catch (Exception e) {
            popError(e.toString());
        }
    }

    @FXML
    protected void maxHisAction(ActionEvent event) {
        AppVaribles.setConfigValue("ImageHisKey", true);
        checkMaxHis();
    }

    @FXML
    protected void noHisAction(ActionEvent event) {
        AppVaribles.setConfigValue("ImageHisKey", false);
        hisMaxInput.setStyle(null);
    }

    @FXML
    private void close() {
        closeStage();
    }

}
