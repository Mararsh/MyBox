package mara.mybox.controller;

import java.util.Arrays;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import mara.mybox.data2d.Data2D;
import mara.mybox.db.data.InfoNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2023-9-16
 * @License Apache License Version 2.0
 */
public class ControlData2DDefAttributes extends ControlInfoTreeAttributes {

    protected short scale;
    protected int maxRandom;
    protected Data2D data2D;

    @FXML
    protected TextArea descInput;
    @FXML
    protected ComboBox<String> scaleSelector, randomSelector;

    public ControlData2DDefAttributes() {
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            scale = (short) UserConfig.getInt(baseName + "Scale", 2);
            if (scale < 0) {
                scale = 2;
            }
            scaleSelector.getItems().addAll(
                    Arrays.asList("2", "1", "0", "3", "4", "5", "6", "7", "8", "10", "12", "15")
            );
            scaleSelector.setValue(scale + "");
            scaleSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(scaleSelector.getValue());
                            if (v >= 0 && v <= 15) {
                                scale = (short) v;
                                UserConfig.setInt(baseName + "Scale", v);
                                scaleSelector.getEditor().setStyle(null);
                                if (!isSettingValues) {
                                    attributesChanged();
                                }
                            } else {
                                scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            scaleSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            maxRandom = UserConfig.getInt(baseName + "MaxRandom", 100000);
            if (maxRandom < 0) {
                maxRandom = 100000;
            }
            randomSelector.getItems().addAll(Arrays.asList("1", "100", "10", "1000", "10000", "1000000", "10000000"));
            randomSelector.setValue(maxRandom + "");
            randomSelector.getSelectionModel().selectedItemProperty().addListener(
                    (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                        try {
                            int v = Integer.parseInt(newValue);
                            if (v > 0) {
                                maxRandom = v;
                                UserConfig.setInt(baseName + "MaxRandom", v);
                                randomSelector.getEditor().setStyle(null);
                                if (!isSettingValues) {
                                    attributesChanged();
                                }
                            } else {
                                randomSelector.getEditor().setStyle(UserConfig.badStyle());
                            }
                        } catch (Exception e) {
                            randomSelector.getEditor().setStyle(UserConfig.badStyle());
                        }
                    });

            descInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable,
                        String oldValue, String newValue) {
                    if (!isSettingValues) {
                        if (newValue == null && oldValue != null
                                || newValue != null && !newValue.equals(oldValue)) {
                            attributesChanged();
                        }
                    }
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void editNode(InfoNode node) {
        editNode(node, data2D);
    }

    protected void editNode(InfoNode node, Data2D data) {
        try {
            super.editNode(node);
            isSettingValues = true;
            tableData.clear();
            data2D = data;
            if (data2D != null) {
                if (node == null) {
                    nameInput.setText(data2D.getDataName());
                }
                scaleSelector.setValue(data2D.getScale() + "");
                randomSelector.setValue(data2D.getMaxRandom() + "");
                descInput.setText(data2D.getComments());
            } else {
                scaleSelector.setValue(scale + "");
                randomSelector.setValue(maxRandom + "");
                descInput.clear();
            }
            isSettingValues = false;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

}
