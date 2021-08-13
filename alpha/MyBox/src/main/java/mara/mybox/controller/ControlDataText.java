package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-26
 * @License Apache License Version 2.0
 */
public class ControlDataText extends BaseController {

    protected ControlSheetData sheetController;
    protected String[][] sheet;
    protected String delimiter;

    @FXML
    protected TextArea textArea;
    @FXML
    protected ToggleGroup delimiterGroup;
    @FXML
    protected RadioButton blankRadio, blank4Radio, blank8Radio, tabRadio, commaRadio,
            lineRadio, atRadio, sharpRadio, semicolonsRadio, stringRadio;
    @FXML
    protected TextField delimiterInput;

    public void setParameters(BaseController parent) {
        try {
            if (parentController != null) {
                return;
            }
            this.parentController = parent;
            if (parent != null) {
                this.baseName = parent.baseName;
                this.baseTitle = parent.baseTitle;
            }
            delimiter = UserConfig.getString(baseName + "TargetDelimiter", "Blank");
            switch (delimiter.toLowerCase()) {
                case "blank":
                    blankRadio.fire();
                    break;
                case "blank4":
                    blank4Radio.fire();
                    break;
                case "blank8":
                    blank8Radio.fire();
                    break;
                case "tab":
                    tabRadio.fire();
                    break;
                case ",":
                    commaRadio.fire();
                    break;
                case "|":
                    lineRadio.fire();
                    break;
                case "@":
                    atRadio.fire();
                    break;
                case "#":
                    sharpRadio.fire();
                    break;
                case ";":
                    semicolonsRadio.fire();
                    break;
                default:
                    stringRadio.fire();
            }
            delimiterGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    delimiterInput.setStyle(null);
                    if (stringRadio.isSelected()) {
                        String v = delimiterInput.getText();
                        if (v == null || v.isBlank()) {
                            delimiterInput.setStyle(NodeStyleTools.badStyle);
                            return;
                        }
                        delimiter = v;
                        delimiterInput.setStyle(null);
                    } else if (blankRadio.isSelected()) {
                        delimiter = "Blank";
                    } else if (blank4Radio.isSelected()) {
                        delimiter = "Blank4";
                    } else if (blank8Radio.isSelected()) {
                        delimiter = "Blank8";
                    } else if (tabRadio.isSelected()) {
                        delimiter = "Tab";
                    } else if (commaRadio.isSelected()) {
                        delimiter = ",";
                    } else if (lineRadio.isSelected()) {
                        delimiter = "|";
                    } else if (atRadio.isSelected()) {
                        delimiter = "@";
                    } else if (sharpRadio.isSelected()) {
                        delimiter = "#";
                    } else if (semicolonsRadio.isSelected()) {
                        delimiter = ";";
                    }
                    UserConfig.setString(baseName + "TargetDelimiter", delimiter);
                    update(sheet);
                }
            });
            delimiterInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (!stringRadio.isSelected()) {
                        return;
                    }
                    if (newValue == null || newValue.isBlank()) {
                        delimiterInput.setStyle(NodeStyleTools.badStyle);
                        return;
                    }
                    delimiter = newValue;
                    UserConfig.setString(baseName + "TargetDelimiter", delimiter);
                    delimiterInput.setStyle(null);
                    update(sheet);
                }
            });

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public void update(String[][] sheet) {
        update(sheet, delimiter);
    }

    public void update(String[][] sheet, String delimiter) {
        this.sheet = sheet;
        List<String> colsNames = null;
        List<String> rowsNames = null;
        String title = null;
        if (sheetController != null) {
            if (sheetController.textTitleCheck != null && sheetController.textTitleCheck.isSelected()) {
                title = sheetController.titleName();
            }
            if (sheetController.textColumnCheck != null && sheetController.textColumnCheck.isSelected()) {
                colsNames = sheetController.columnNames();
            }
            if (sheetController.textRowCheck != null && sheetController.textRowCheck.isSelected()) {
                rowsNames = sheetController.rowNames(sheet.length);
            }
        }
        String text = TextTools.dataText(sheet, delimiter, colsNames, rowsNames);
        if (title != null && !title.isBlank()) {
            textArea.setText(title + "\n\n" + text);
        } else {
            textArea.setText(text);
        }
    }

}
