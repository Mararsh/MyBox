package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import mara.mybox.db.data.DataNode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2024-8-8
 * @License Apache License Version 2.0
 */
public class ControlDataText extends BaseDataValuesController {

    @FXML
    protected TextArea textInput;
    @FXML
    protected CheckBox wrapCheck;

    @Override
    public void initEditor() {
        try {
            textInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue v, String ov, String nv) {
                    valueChanged(true);
                }
            });

            wrapCheck.setSelected(UserConfig.getBoolean(baseName + "Wrap", false));
            wrapCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Wrap", newValue);
                    textInput.setWrapText(newValue);
                }
            });
            textInput.setWrapText(wrapCheck.isSelected());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void editValues() {
        try {
            Object v;
            if (nodeEditor.currentNode == null) {
                v = null;
            } else {
                v = nodeEditor.currentNode.getValue("text");
            }
            textInput.setText(v != null ? (String) v : null);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected DataNode pickValues(DataNode node) {
        try {
            node.setValue("title", nodeEditor.titleInput.getText());
            node.setValue("text", textInput.getText());
            return node;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    public void clearValue() {
        textInput.clear();
    }

    @FXML
    protected void popHistories(Event event) {
        if (UserConfig.getBoolean(baseName + "HistoriesPopWhenMouseHovering", false)) {
            showHistories(event);
        }
    }

    @FXML
    protected void showHistories(Event event) {
        PopTools.popStringValues(this, textInput, event, baseName + "Histories", false);
    }

}