package mara.mybox.controller;

import java.sql.Connection;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-6-23
 * @License Apache License Version 2.0
 */
public class ControlSvgOptions extends ControlXmlOptions {

    protected int width, height;

    @FXML
    protected TextField widthInput, heightInput;

    @Override
    public void initControls(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            width = UserConfig.getInt("SvgWidth", 0);
            if (width > 0) {
                widthInput.setText(width + "");
            }
            widthInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        return;
                    }
                    width = 0;
                    try {
                        int v = Integer.parseInt(widthInput.getText());
                        if (v > 0) {
                            width = v;
                        }
                    } catch (Exception e) {
                    }
                    UserConfig.setInt("SvgWidth", width);
                }
            });

            height = UserConfig.getInt("SvgHeight", 0);
            if (height > 0) {
                heightInput.setText(height + "");
            }
            heightInput.setText(height + "");
            heightInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    if (newValue) {
                        return;
                    }
                    height = 0;
                    try {
                        int v = Integer.parseInt(heightInput.getText());
                        if (v > 0) {
                            height = v;
                        }
                    } catch (Exception e) {
                    }
                    UserConfig.setInt("SvgHeight", height);
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
