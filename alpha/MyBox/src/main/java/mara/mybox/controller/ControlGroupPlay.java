package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-7-17
 * @License Apache License Version 2.0
 */
public class ControlGroupPlay extends ControlPlay {

    protected int askLarger;

    @FXML
    protected TextField largerInput;

    @Override
    public void initControls() {
        try {
            super.initControls();

            askLarger = UserConfig.getInt(baseName + "AskLarger", 100);
            largerInput.setText(askLarger + "");
            largerInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    try {
                        if (newValue == null || newValue.isBlank()) {
                            askLarger = -1;
                        } else {
                            askLarger = Integer.valueOf(newValue);
                        }
                        largerInput.setStyle(null);
                    } catch (Exception ex) {
                        largerInput.setStyle(UserConfig.badStyle());
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
