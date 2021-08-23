package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-26
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetDisplay_Text extends ControlSheetDisplay_Validation {

    protected String textDelimiter;

    @FXML
    protected TextArea textArea;
    @FXML
    protected ToggleGroup delimiterGroup;
    @FXML
    protected RadioButton blankRadio, blank4Radio, blank8Radio, tabRadio, commaRadio,
            lineRadio, atRadio, sharpRadio, semicolonsRadio, stringRadio;
    @FXML
    protected TextField delimiterInput;
    @FXML
    protected CheckBox textTitleCheck, textColumnCheck, textRowCheck;

    public void initTextControls() {
        try {
            textDelimiter = UserConfig.getString(baseName + "TextDelimiter", "Blank");
            switch (textDelimiter.toLowerCase()) {
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
                        textDelimiter = v;
                        delimiterInput.setStyle(null);
                    } else if (blankRadio.isSelected()) {
                        textDelimiter = "Blank";
                    } else if (blank4Radio.isSelected()) {
                        textDelimiter = "Blank4";
                    } else if (blank8Radio.isSelected()) {
                        textDelimiter = "Blank8";
                    } else if (tabRadio.isSelected()) {
                        textDelimiter = "Tab";
                    } else if (commaRadio.isSelected()) {
                        textDelimiter = ",";
                    } else if (lineRadio.isSelected()) {
                        textDelimiter = "|";
                    } else if (atRadio.isSelected()) {
                        textDelimiter = "@";
                    } else if (sharpRadio.isSelected()) {
                        textDelimiter = "#";
                    } else if (semicolonsRadio.isSelected()) {
                        textDelimiter = ";";
                    }
                    UserConfig.setString(baseName + "TextDelimiter", textDelimiter);
                    updateText();
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
                    textDelimiter = newValue;
                    UserConfig.setString(baseName + "TextDelimiter", textDelimiter);
                    delimiterInput.setStyle(null);
                    updateText();
                }
            });

            textTitleCheck.setSelected(UserConfig.getBoolean(baseName + "TextTitle", true));
            textTitleCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                updateText();
                UserConfig.setBoolean(baseName + "TextTitle", newValue);
            });
            textColumnCheck.setSelected(UserConfig.getBoolean(baseName + "TextColumn", false));
            textColumnCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                updateText();
                UserConfig.setBoolean(baseName + "TextColumn", newValue);
            });
            textRowCheck.setSelected(UserConfig.getBoolean(baseName + "TextRow", false));
            textRowCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                updateText();
                UserConfig.setBoolean(baseName + "TextRow", newValue);
            });

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public void updateText() {
        List<String> colsNames = null;
        List<String> rowsNames = null;
        String title = null;
        if (textTitleCheck.isSelected()) {
            title = titleName();
        }
        if (textColumnCheck.isSelected()) {
            colsNames = columnNames();
        }
        if (textRowCheck.isSelected()) {
            rowsNames = sheet == null ? null : rowNames(sheet.length);
        }
        String text = TextTools.dataText(sheet, textDelimiter, colsNames, rowsNames);
        if (title != null && !title.isBlank()) {
            textArea.setText(title + "\n\n" + text);
        } else {
            textArea.setText(text);
        }
    }

    @FXML
    public void editText() {
        TextEditorController controller = (TextEditorController) openStage(Fxmls.TextEditorFxml);
        controller.loadContents(textArea.getText());
    }
}
