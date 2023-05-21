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
        textArea.clear();

        if (nullRadio.isSelected() || trueRadio.isSelected() || falseRadio.isSelected()) {
            textArea.setDisable(true);

        } else {

            if (stringRadio.isSelected()) {
                textArea.setText("\"\"");

            } else if (numberRadio.isSelected()) {
                textArea.setText("0");

            } else if (objectRadio.isSelected()) {
                textArea.setText("{ }");

            } else if (arrayRadio.isSelected()) {
                textArea.setText("[ ]");

            }

            textArea.setDisable(false);
        }
    }

    public JsonNode pickValue() {
        try {
            if (trueRadio.isSelected()) {
                return JsonTreeNode.parseByJackson("true");

            } else if (falseRadio.isSelected()) {
                return JsonTreeNode.parseByJackson("false");

            } else if (nullRadio.isSelected()) {
                return JsonTreeNode.parseByJackson("null");

            } else {
                String newValue = textArea.getText();
                if (numberRadio.isSelected()) {
                    try {
                        Double.parseDouble(newValue);
                    } catch (Exception e) {
                        popError(message("InvalidData"));
                        return null;
                    }

                }
                return JsonTreeNode.parseByJackson(newValue);

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
