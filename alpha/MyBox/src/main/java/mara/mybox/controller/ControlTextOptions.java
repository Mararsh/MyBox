package mara.mybox.controller;

import java.nio.charset.Charset;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TextTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-16
 * @License Apache License Version 2.0
 */
public class ControlTextOptions extends BaseController {

    protected Charset charset;
    protected String delimiterName;
    protected boolean autoDetermine;

    @FXML
    protected CheckBox withNamesCheck;
    @FXML
    protected ComboBox<String> charsetSelector;
    @FXML
    protected ControlTextDelimiter delimiterController;
    @FXML
    protected ToggleGroup charsetGroup;
    @FXML
    protected RadioButton autoCharsetRadio, charsetKnownRadio;

    public void setControls(String baseName, boolean hasBlanks) {
        try {
            this.baseName = baseName;

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
                charset = Charset.forName(UserConfig.getString(baseName + "TextCharset", Charset.defaultCharset().name()));
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

            delimiterController.setControls(baseName, hasBlanks);
            delimiterName = delimiterController.delimiterName;
            delimiterController.changedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    delimiterName = delimiterController.delimiterName;
                }
            });

            withNamesCheck.setSelected(UserConfig.getBoolean(baseName + "TextWithNames", true));
            withNamesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "TextWithNames", withNamesCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void checkCharset() {
        if (charsetGroup == null) {
            autoDetermine = false;
            charset = Charset.forName(charsetSelector.getSelectionModel().getSelectedItem());
        } else {
            if (autoCharsetRadio.isSelected()) {
                autoDetermine = true;
                charsetSelector.setDisable(true);
            } else {
                autoDetermine = false;
                charset = Charset.forName(charsetSelector.getSelectionModel().getSelectedItem());
                charsetSelector.setDisable(false);
            }
        }
        UserConfig.setString(baseName + "TextCharset", charset.name());
    }

    protected void setCharset(Charset charset) {
        if (charsetKnownRadio != null) {
            charsetKnownRadio.setSelected(true);
        }
        if (charset != null) {
            charsetSelector.setValue(charset.name());
        }
    }

    protected void setDelimiterName(String delimiter) {
        delimiterController.setDelimiterName(delimiter);
    }

    public String getDelimiterValue() {
        return delimiterController.getDelimiterValue();
    }

}
