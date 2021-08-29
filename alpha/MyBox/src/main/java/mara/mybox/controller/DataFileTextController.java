package mara.mybox.controller;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-25
 * @License Apache License Version 2.0
 */
public abstract class DataFileTextController extends BaseDataFileController {

    protected String[][] pageData;

    protected TextField[][] sheetInputs;
    protected String sourceDelimiter, displayDelimiter;

    @FXML
    protected ToggleGroup delimiterSourceGroup, delimiterDisplayGroup;
    @FXML
    protected RadioButton blankSourceRadio, blank4SourceRadio, blank8SourceRadio, tabSourceRadio, commaSourceRadio,
            lineSourceRadio, atSourceRadio, sharpSourceRadio, semicolonsSourceRadio, stringSourceRadio;
    @FXML
    protected TextField delimiterSourceInput;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    public void initTextsEdit() {
        try {
            setSourceDelimiter(UserConfig.getString(baseName + "SourceDelimiter", "Blank"));
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
                    synchronizeChanged();
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
                    synchronizeChanged();
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

    public synchronized void synchronizeChanged() {

    }

    public synchronized void synchronizeChanged(String s) {
        try {
            if (isSettingValues) {
                return;
            }
//            String s = textsEditArea.getText();
//            String[] lines = s.split("\n");
//            rowsNumber = colsNumber = 0;
//            List<List<String>> data = new ArrayList<>();
//            for (String line : lines) {
//                line = line.trim();
//                if (line.isEmpty()) {
//                    continue;
//                }
//                String[] values;
//                switch (sourceDelimiter.toLowerCase()) {
//                    case "tab":
//                        values = line.split("\t");
//                        break;
//                    case "blank":
//                        values = line.split("\\s+");
//                        break;
//                    case "blank4":
//                        values = line.split("\\s{4}");
//                        break;
//                    case "blank8":
//                        values = line.split("\\s{8}");
//                        break;
//                    case "|":
//                        values = line.split("\\|");
//                        break;
//                    default:
//                        values = line.split(sourceDelimiter);
//                        break;
//                }
//                if (values == null || values.length == 0) {
//                    continue;
//                }
//                List<String> row = new ArrayList<>();
//                for (String value : values) {
//                    row.add(value);
//                }
//                int size = row.size();
//                if (size == 0) {
//                    continue;
//                }
//                if (size > colsNumber) {
//                    colsNumber = size;
//                }
//                data.add(row);
//            }
//            rowsNumber = data.size();
//            if (rowsNumber == 0 || colsNumber == 0) {
//                makeSheet(null);
//                return;
//            }
//            pageData = new String[rowsNumber][colsNumber];
//            for (int i = 0; i < rowsNumber; i++) {
//                List<String> row = data.get(i);
//                for (int j = 0; j < row.size(); j++) {
//                    pageData[i][j] = row.get(j);
//                }
//            }
//            makeSheet(pageData, false);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

//    @Override
//    public void sourceFileChanged(File file) {
//        isSettingValues = true;
//        textsEditArea.setText(TextTools.readText(file));
//        isSettingValues = false;
//        synchronizeChanged();
//    }
//
//    @FXML
//    @Override
//    public void pasteAction() {
//        textsEditArea.setText(TextClipboardTools.getSystemClipboardString());
//    }
}
