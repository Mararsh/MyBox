package mara.mybox.controller;

import java.util.ArrayList;
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
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-9-7
 * @License Apache License Version 2.0
 */
public abstract class ControlSheet_Edit extends ControlSheet_Pages {

    protected String sourceDelimiter;

    @FXML
    protected ToggleGroup delimiterSourceGroup;
    @FXML
    protected RadioButton blankSourceRadio, blank4SourceRadio, blank8SourceRadio, tabSourceRadio, commaSourceRadio,
            lineSourceRadio, atSourceRadio, sharpSourceRadio, semicolonsSourceRadio, stringSourceRadio;
    @FXML
    protected TextArea textsEditArea;
    @FXML
    protected TextField delimiterSourceInput;

    public void initEdit() {
        try {
            setSourceDelimiter(UserConfig.getString(baseName + "SourceDelimiter", ","));
            delimiterSourceGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    delimiterSourceInput.setStyle(null);
                    if (stringSourceRadio.isSelected()) {
                        String v = delimiterSourceInput.getText();
                        if (v == null || v.isBlank()) {
                            delimiterSourceInput.setStyle(NodeStyleTools.badStyle);
                            return;
                        }
                        sourceDelimiter = v;
                        delimiterSourceInput.setStyle(null);
                    } else if (blankSourceRadio.isSelected()) {
                        sourceDelimiter = "Blank";
                    } else if (blank4SourceRadio.isSelected()) {
                        sourceDelimiter = "Blank4";
                    } else if (blank8SourceRadio.isSelected()) {
                        sourceDelimiter = "Blank8";
                    } else if (tabSourceRadio.isSelected()) {
                        sourceDelimiter = "Tab";
                    } else if (commaSourceRadio.isSelected()) {
                        sourceDelimiter = ",";
                    } else if (lineSourceRadio.isSelected()) {
                        sourceDelimiter = "|";
                    } else if (atSourceRadio.isSelected()) {
                        sourceDelimiter = "@";
                    } else if (sharpSourceRadio.isSelected()) {
                        sourceDelimiter = "#";
                    } else if (semicolonsSourceRadio.isSelected()) {
                        sourceDelimiter = ";";
                    }
                    UserConfig.setString(baseName + "SourceDelimiter", sourceDelimiter);
                }
            });
            delimiterSourceInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (isSettingValues || !stringSourceRadio.isSelected()) {
                        return;
                    }
                    if (newValue == null || newValue.isBlank()) {
                        delimiterSourceInput.setStyle(NodeStyleTools.badStyle);
                        return;
                    }
                    sourceDelimiter = newValue;
                    UserConfig.setString(baseName + "SourceDelimiter", sourceDelimiter);
                    delimiterSourceInput.setStyle(null);
                }
            });

            textsEditArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    if (isSettingValues) {
                        return;
                    }
                    editTab.setText(message("EditText") + " *");
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setSourceDelimiter(String sourceDelimiter) {
        try {
            this.sourceDelimiter = sourceDelimiter;
            switch (sourceDelimiter.toLowerCase()) {
                case "blank":
                    blankSourceRadio.fire();
                    break;
                case "blank4":
                    blank4SourceRadio.fire();
                    break;
                case "blank8":
                    blank8SourceRadio.fire();
                    break;
                case "tab":
                    tabSourceRadio.fire();
                    break;
                case ",":
                    commaSourceRadio.fire();
                    break;
                case "|":
                    lineSourceRadio.fire();
                    break;
                case "@":
                    atSourceRadio.fire();
                    break;
                case "#":
                    sharpSourceRadio.fire();
                    break;
                case ";":
                    semicolonsSourceRadio.fire();
                    break;
                default:
                    stringSourceRadio.fire();
                    delimiterSourceInput.setText(sourceDelimiter);
            }
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @FXML
    @Override
    public boolean synchronizeAction() {
        try {
            if (isSettingValues || !editTab.isSelected()) {
                return false;
            }
            String s = textsEditArea.getText();
            String[] lines = s.split("\n");
            int colsSize = 0;
            List<List<String>> data = new ArrayList<>();
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] values;
                switch (sourceDelimiter.toLowerCase()) {
                    case "tab":
                        values = line.split("\t");
                        break;
                    case "blank":
                        values = line.split("\\s+");
                        break;
                    case "blank4":
                        values = line.split("\\s{4}");
                        break;
                    case "blank8":
                        values = line.split("\\s{8}");
                        break;
                    case "|":
                        values = line.split("\\|");
                        break;
                    default:
                        values = line.split(sourceDelimiter);
                        break;
                }
                if (values == null || values.length == 0) {
                    continue;
                }
                List<String> row = new ArrayList<>();
                for (String value : values) {
                    row.add(value);
                }
                int size = row.size();
                if (size == 0) {
                    continue;
                }
                if (size > colsSize) {
                    colsSize = size;
                }
                data.add(row);
            }
            int rowsSize = data.size();
            if (pagesNumber > 1) {
                colsSize = columns == null ? 0 : columns.size();
            }
            if (rowsSize == 0 || colsSize == 0) {
                makeSheet(null);
                return true;
            }
            pageData = new String[rowsSize][colsSize];
            for (int r = 0; r < rowsSize; r++) {
                List<String> row = data.get(r);
                for (int c = 0; c < Math.min(colsSize, row.size()); c++) {
                    pageData[r][c] = row.get(c);
                }
            }
            makeSheet(pageData, true);
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
            return false;
        }
    }

    protected void updateEdit() {
        String text = TextTools.dataText(pageData, sourceDelimiter);
        isSettingValues = true;
        textsEditArea.setText(text);
        isSettingValues = false;
        editTab.setText(message("EditText"));
    }

    public void loadText(String text) {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            tabPane.getSelectionModel().select(editTab);
            isSettingValues = true;
            textsEditArea.setText(text);
            isSettingValues = false;
            editTab.setText(message("EditText") + " *");
            synchronizeAction();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

}
