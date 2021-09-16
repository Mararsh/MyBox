package mara.mybox.controller;

import java.nio.charset.Charset;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TextTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-16
 * @License Apache License Version 2.0
 */
public class ControlTextWriteOptions extends BaseController {

    protected Charset charset;
    protected String delimiterName;

    @FXML
    protected CheckBox withNamesCheck;
    @FXML
    protected ComboBox<String> charsetSelector;
    @FXML
    protected ControlTextDelimiter delimiterController;

    public void setControls(String baseName) {
        try {
            this.baseName = baseName;
            delimiterController.setControls(baseName + "Write", false);
            delimiterName = delimiterController.delimiterName;
            delimiterController.changedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    delimiterName = delimiterController.delimiterName;
                }
            });

            try {
                charset = Charset.forName(UserConfig.getString(baseName + "WriteCharset", Charset.defaultCharset().name()));
            } catch (Exception e) {
                charset = Charset.defaultCharset();
            }
            charsetSelector.getItems().addAll(TextTools.getCharsetNames());
            charsetSelector.setValue(charset.name());
            charsetSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    charset = Charset.forName(charsetSelector.getSelectionModel().getSelectedItem());
                    UserConfig.setString(baseName + "WriteCharset", charset.name());
                }
            });
            withNamesCheck.setSelected(UserConfig.getBoolean(baseName + "WriteWithNames", true));
            withNamesCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "WriteWithNames", withNamesCheck.isSelected());
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
