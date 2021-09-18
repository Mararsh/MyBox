package mara.mybox.controller;

import java.nio.charset.Charset;
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
import mara.mybox.tools.TextTools;
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
    protected RadioButton charsetAutoRadio, charsetKnownRadio,
            commaRadio, lineRadio, atRadio, sharpRadio, semicolonsRadio, delimiterInputRadio;
    @FXML
    protected TextField delimiterInput;
    @FXML
    protected ToggleGroup charsetGroup;
    @FXML
    protected ComboBox<String> charsetSelector;

    public ControlCsvOptions() {
    }

    public void setControls(String baseName) {
        try {
            this.baseName = baseName;

            withNamesCheck.setSelected(UserConfig.getBoolean(baseName + "CsvWithNames", true));
            withNamesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "CsvWithNames", newValue);
                }
            });

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
            charsetSelector.getItems().addAll(TextTools.getCharsetNames());
            try {
                charset = Charset.forName(UserConfig.getString(baseName + "CsvCharset", Charset.defaultCharset().name()));
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

            setDelimiter((char) (UserConfig.getInt(baseName + "CsvDelimiter", ',')));
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
                    UserConfig.setInt(baseName + "CsvDelimiter", delimiter);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setDelimiter(char c) {
        delimiter = c;
        switch (c) {
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
        UserConfig.setString(baseName + "CsvCharset", charset.name());
    }

    protected void setCharset(Charset charset) {
        if (charsetKnownRadio != null) {
            charsetKnownRadio.fire();
        }
        if (charset != null) {
            charsetSelector.setValue(charset.name());
        }
    }

}
