package mara.mybox.controller;

import java.nio.charset.Charset;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2020-11-16
 * @License Apache License Version 2.0
 */
public class TextReplaceBatchOptions extends ControlFindReplace {

    protected Charset charset;
    protected boolean autoDetermine;

    @FXML
    protected ToggleGroup charsetGroup;
    @FXML
    protected ComboBox<String> encodeBox;

    public TextReplaceBatchOptions() {
        TipsLabelKey = "TextReplaceBatchTips";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            charsetGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> ov,
                        Toggle old_toggle, Toggle new_toggle) {
                    checkCharset();
                }
            });

            List<String> setNames = TextTools.getCharsetNames();
            encodeBox.getItems().addAll(setNames);
            encodeBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    checkCharset();
                }
            });
            encodeBox.getSelectionModel().select(Charset.defaultCharset().name());

        } catch (Exception e) {
            logger.error(e.toString());
        }

    }

    protected void checkCharset() {
        RadioButton selected = (RadioButton) charsetGroup.getSelectedToggle();
        if (message("DetermainAutomatically").equals(selected.getText())) {
            autoDetermine = true;
            encodeBox.setDisable(true);
        } else {
            autoDetermine = false;
            charset = Charset.forName(encodeBox.getSelectionModel().getSelectedItem());
            encodeBox.setDisable(false);
        }
    }

    @Override
    protected void checkFindInput(String string) {
    }

    @Override
    protected boolean checkReplaceInput(String string) {
        return true;
    }

}
