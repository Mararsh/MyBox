package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-2
 * @License Apache License Version 2.0
 */
public class SheetCopyToSystemClipboardController extends BaseDataOperationController {

    protected char delimiter;

    @FXML
    protected ToggleGroup delimiterGroup;
    @FXML
    protected RadioButton commaRadio, lineRadio, atRadio, sharpRadio, semicolonsRadio, delimiterInputRadio;
    @FXML
    protected CheckBox colNameCheck;
    @FXML
    protected TextField delimiterInput;
    @FXML
    protected FlowPane delimiterPane;

    @Override
    public void setParameters(ControlSheet sheetController, int row, int col) {
        try {
            super.setParameters(sheetController, row, col);

            setDelimiter((char) (UserConfig.getInt(baseName + "Delimiter", ',')));
            delimiterGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    delimiterInput.setStyle(null);
                    if (delimiterInputRadio.isSelected()) {
                        String v = delimiterInput.getText();
                        if (v == null || v.isBlank()) {
                            delimiterInput.setStyle(UserConfig.badStyle());
                            return;
                        }
                        delimiter = v.charAt(0);
                    } else {
                        delimiter = ((RadioButton) (delimiterGroup.getSelectedToggle())).getText().charAt(0);
                    }
                    UserConfig.setInt(baseName + "Delimiter", delimiter);
                }
            });

            colNameCheck.setSelected(UserConfig.getBoolean(baseName + "Names", true));
            colNameCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    UserConfig.setBoolean(baseName + "Names", newValue);
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

    @FXML
    @Override
    public void okAction() {
        try {
            sheetController.copyDelimiter = delimiter;
            boolean withNames = colNameCheck.isSelected();

            List<Integer> cols = cols();
            if (rowAllRadio.isSelected()) {
                sheetController.copyCols(cols, withNames, true);

            } else {
                sheetController.copyRowsCols(rows(), cols, withNames, true);
            }

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }

    }

}
