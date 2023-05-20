package mara.mybox.controller;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import mara.mybox.data.JsonTreeNode;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-5-16
 * @License Apache License Version 2.0
 */
public class ControlJsonNodeBase extends BaseController {

    protected ControlJsonTree treeController;
    protected TreeItem<JsonTreeNode> treeItem;

    @FXML
    protected TextField nameInput;
    @FXML
    protected Label nameLabel, infoLabel;
    @FXML
    protected ToggleGroup valueGroup;
    @FXML
    protected RadioButton stringRadio, numberRadio, trueRadio, falseRadio, nullRadio,
            objectRadio, arrayRadio, jsonRadio;
    @FXML
    protected TextArea textArea;

    @Override
    public void initControls() {
        try {
            super.initControls();

            valueGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue v, Toggle ov, Toggle nv) {
                    checkValue();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void checkValue() {
        if (stringRadio.isSelected() || numberRadio.isSelected() || jsonRadio.isSelected()) {
            if (treeItem == null || treeItem.getValue() == null) {
                textArea.clear();
            } else {
                textArea.setText(treeItem.getValue().getValue());
            }
            textArea.setDisable(false);
        } else {
            textArea.clear();
            textArea.setDisable(true);
        }
    }

    public JsonNode pickValue() {
        try {
            String newValue = textArea.getText();
            if (numberRadio.isSelected()) {
                try {
                    Double.parseDouble(newValue);
                } catch (Exception e) {
                    popError(message("InvalidData"));
                    return null;
                }
            }

            JsonTreeNode currentTreeNode = treeItem != null ? treeItem.getValue() : null;

            if (trueRadio.isSelected()) {
                return JsonTreeNode.parseByJackson("true");

            } else if (falseRadio.isSelected()) {
                return JsonTreeNode.parseByJackson("false");

            } else if (nullRadio.isSelected()) {
                return JsonTreeNode.parseByJackson("null");

            } else if (numberRadio.isSelected()) {
                return JsonTreeNode.parseByJackson(newValue);

            } else if (stringRadio.isSelected()) {
                return JsonTreeNode.parseByJackson("\"" + newValue + "\"");

            } else if (objectRadio.isSelected()) {
                if (currentTreeNode != null && currentTreeNode.isObject()) {
                    return currentTreeNode.getJsonNode();
                } else {
                    return JsonTreeNode.parseByJackson("{}");
                }

            } else if (arrayRadio.isSelected()) {
                if (currentTreeNode != null && currentTreeNode.isArray()) {
                    return currentTreeNode.getJsonNode();
                } else {
                    return JsonTreeNode.parseByJackson("[]");
                }

            } else if (jsonRadio.isSelected()) {
                return JsonTreeNode.parseByJackson(newValue);

            } else {
                return null;
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }

    }

    public void clearNode() {
        treeItem = null;
        infoLabel.setText("");
        nameInput.clear();
        textArea.clear();
    }

}
