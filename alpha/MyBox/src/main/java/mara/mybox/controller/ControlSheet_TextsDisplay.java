package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Toggle;
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
public abstract class ControlSheet_TextsDisplay extends ControlSheet_Html {

    public void initTextControls() {
        try {
            displayDelimiter = UserConfig.getString(baseName + "TextDelimiter", "Blank");
            switch (displayDelimiter.toLowerCase()) {
                case "blank":
                    blankDisplayRadio.fire();
                    break;
                case "blank4":
                    blank4DisplayRadio.fire();
                    break;
                case "blank8":
                    blank8DisplayRadio.fire();
                    break;
                case "tab":
                    tabDisplayRadio.fire();
                    break;
                case ",":
                    commaDisplayRadio.fire();
                    break;
                case "|":
                    lineDisplayRadio.fire();
                    break;
                case "@":
                    atDisplayRadio.fire();
                    break;
                case "#":
                    sharpDisplayRadio.fire();
                    break;
                case ";":
                    semicolonsDisplayRadio.fire();
                    break;
                default:
                    stringDisplayRadio.fire();
            }
            delimiterDisplayGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    delimiterDisplayInput.setStyle(null);
                    if (stringDisplayRadio.isSelected()) {
                        String v = delimiterDisplayInput.getText();
                        if (v == null || v.isBlank()) {
                            delimiterDisplayInput.setStyle(NodeStyleTools.badStyle);
                            return;
                        }
                        displayDelimiter = v;
                        delimiterDisplayInput.setStyle(null);
                    } else if (blankDisplayRadio.isSelected()) {
                        displayDelimiter = "Blank";
                    } else if (blank4DisplayRadio.isSelected()) {
                        displayDelimiter = "Blank4";
                    } else if (blank8DisplayRadio.isSelected()) {
                        displayDelimiter = "Blank8";
                    } else if (tabDisplayRadio.isSelected()) {
                        displayDelimiter = "Tab";
                    } else if (commaDisplayRadio.isSelected()) {
                        displayDelimiter = ",";
                    } else if (lineDisplayRadio.isSelected()) {
                        displayDelimiter = "|";
                    } else if (atDisplayRadio.isSelected()) {
                        displayDelimiter = "@";
                    } else if (sharpDisplayRadio.isSelected()) {
                        displayDelimiter = "#";
                    } else if (semicolonsDisplayRadio.isSelected()) {
                        displayDelimiter = ";";
                    }
                    UserConfig.setString(baseName + "TextDelimiter", displayDelimiter);
                    updateText();
                }
            });
            delimiterDisplayInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (!stringDisplayRadio.isSelected()) {
                        return;
                    }
                    if (newValue == null || newValue.isBlank()) {
                        delimiterDisplayInput.setStyle(NodeStyleTools.badStyle);
                        return;
                    }
                    displayDelimiter = newValue;
                    UserConfig.setString(baseName + "TextDelimiter", displayDelimiter);
                    delimiterDisplayInput.setStyle(null);
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
            rowsNames = pageData == null ? null : rowNames(pageData.length);
        }
        String text = TextTools.dataText(pageData, displayDelimiter, colsNames, rowsNames);
        if (title != null && !title.isBlank()) {
            textsDisplayArea.setText(title + "\n\n" + text);
        } else {
            textsDisplayArea.setText(text);
        }
    }

    @FXML
    public void editText() {
        TextEditorController controller = (TextEditorController) openStage(Fxmls.TextEditorFxml);
        controller.loadContents(textsDisplayArea.getText());
    }
}
