package mara.mybox.controller;

import java.nio.charset.Charset;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import static mara.mybox.fxml.NodeStyleTools.badStyle;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-29
 * @License Apache License Version 2.0
 */
public class ControlCsvOptions extends BaseController {

    protected char delimiter;
    protected Charset charset;
    protected boolean autoDetermine;

    @FXML
    protected CheckBox withNamesCheck;
    @FXML
    protected ToggleGroup delimiterGroup;
    @FXML
    protected RadioButton commaRadio, lineRadio, atRadio, sharpRadio, semicolonsRadio, delimiterInputRadio;
    @FXML
    protected TextField delimiterInput;
    @FXML
    protected ToggleGroup charsetGroup;
    @FXML
    protected ComboBox<String> charsetSelector;

    public ControlCsvOptions() {
    }

    public void setControls(String name) {
        try {
            this.baseName = name;

            if (charsetGroup == null) {
                autoDetermine = false;
            } else {
                autoDetermine = true;
                charsetGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                    @Override
                    public void changed(ObservableValue<? extends Toggle> ov,
                            Toggle old_toggle, Toggle new_toggle) {
                        checkCharset();
                    }
                });
            }

            withNamesCheck.setSelected(UserConfig.getUserConfigBoolean(baseName + "Names", true));
            withNamesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setUserConfigBoolean(baseName + "Names", newValue);
                }
            });

            List<String> setNames = TextTools.getCharsetNames();
            charsetSelector.getItems().addAll(setNames);
            try {
                charset = Charset.forName(UserConfig.getUserConfigString(baseName + "Charset", Charset.defaultCharset().name()));
            } catch (Exception e) {
                charset = Charset.defaultCharset();
            }
            charsetSelector.setValue(charset.name());
            charsetSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkCharset();
                }
            });

            delimiterGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    delimiterInput.setStyle(null);
                    if (delimiterInputRadio.isSelected()) {
                        String v = delimiterInput.getText();
                        if (v == null || v.isBlank()) {
                            delimiterInput.setStyle(NodeStyleTools.badStyle);
                            return;
                        }
                        delimiter = v.charAt(0);
                    } else {
                        delimiter = ((RadioButton) (delimiterGroup.getSelectedToggle())).getText().charAt(0);
                    }
                    UserConfig.setUserConfigInt(baseName + "Delimiter", delimiter);
                }
            });
            delimiter = (char) (UserConfig.getUserConfigInt(baseName + "Delimiter", ','));
            switch (delimiter) {
                case ',':
                    commaRadio.fire();
                    break;
                case '|':
                    lineRadio.fire();
                    break;
                case '@':
                    atRadio.fire();
                    break;
                case '#':
                    sharpRadio.fire();
                    break;
                case ';':
                    semicolonsRadio.fire();
                    break;
                default:
                    delimiterInput.setText(delimiter + "");
                    delimiterInputRadio.fire();
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkCharset() {
        if (charsetGroup == null) {
            autoDetermine = false;
            charset = Charset.forName(charsetSelector.getSelectionModel().getSelectedItem());
        } else {
            RadioButton selected = (RadioButton) charsetGroup.getSelectedToggle();
            if (Languages.message("DetermainAutomatically").equals(selected.getText())) {
                autoDetermine = true;
                charsetSelector.setDisable(true);
            } else {
                autoDetermine = false;
                charset = Charset.forName(charsetSelector.getSelectionModel().getSelectedItem());
                charsetSelector.setDisable(false);
            }
        }
        UserConfig.setUserConfigString(baseName + "Charset", charset.name());
    }

}
