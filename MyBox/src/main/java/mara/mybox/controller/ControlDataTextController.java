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
import javafx.scene.control.Tooltip;
import javafx.scene.web.WebView;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.TextTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-12-26
 * @License Apache License Version 2.0
 */
public class ControlDataTextController extends BaseController {

    protected String[][] sheet;
    protected int colsNumber, rowsNumber;
    protected String currentEdit, currentCalculation;
    protected String delimiter;
    protected boolean isMatrix;

    @FXML
    protected TextArea inputArea, dataArea;
    @FXML
    protected WebView webView;
    @FXML
    protected ToggleGroup delimiterGroup;
    @FXML
    protected RadioButton blankRadio, blank4Radio, blank8Radio, tabRadio, commaRadio, lineRadio,
            atRadio, sharpRadio, semicolonsRadio, stringRadio;
    @FXML
    protected TextField delimiterInput;

    public ControlDataTextController() {
        baseTitle = message("DataEdit");
    }

    public void setControls(String baseName, boolean isMatrix) {
        try {
            this.baseName = baseName;
            this.isMatrix = isMatrix;
            if (isMatrix) {
                FxmlControl.setTooltip(tipsView, new Tooltip(message("MatrixInputComments")));
            } else {
                FxmlControl.setTooltip(tipsView, new Tooltip(message("DataInputComments")));
            }
            delimiterInput.setText(delimiter);
            switch (delimiter) {
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
                    delimiterInput.setText(delimiter);
                    stringRadio.fire();
            }
            delimiterGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    delimiterInput.setStyle(null);
                    if (stringRadio.isSelected()) {
                        String v = delimiterInput.getText();
                        if (v == null || v.isBlank()) {
                            delimiterInput.setStyle(badStyle);
                            return;
                        }
                        delimiter = v;
                        delimiterInput.setStyle(null);
                    } else if (blankRadio.isSelected()) {
                        delimiter = "blank";
                    } else if (blank4Radio.isSelected()) {
                        delimiter = "blank4";
                    } else if (blank8Radio.isSelected()) {
                        delimiter = "blank8";
                    } else if (tabRadio.isSelected()) {
                        delimiter = "tab";
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
                    AppVariables.setUserConfigValue(baseName + "Delimiter", delimiter);
                    validateData();
                }
            });

            delimiterInput.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    if (!stringRadio.isSelected()) {
                        return;
                    }
                    if (newValue == null || newValue.isBlank()) {
                        delimiterInput.setStyle(badStyle);
                        return;
                    }
                    delimiter = newValue;
                    delimiterInput.setStyle(null);
                    validateData();
                }
            });

            inputArea.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    validateData();
                }
            });

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    protected void validateData() {
        try {
            if (isSettingValues) {
                return;
            }
            dataArea.clear();
            webView.getEngine().loadContent("");
            String s = inputArea.getText();
            String[] lines = s.split("\n");
            rowsNumber = colsNumber = 0;
            List<List<String>> data = new ArrayList<>();
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] values;
                switch (delimiter) {
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
                        values = line.split(delimiter);
                        break;
                }
                if (values == null || values.length == 0) {
                    continue;
                }
                List<String> row = new ArrayList<>();
                for (String value : values) {
                    if (isMatrix) {
                        value = value.replaceAll(CommonValues.DataIgnoreChars, "");
                        try {
                            double d = Double.parseDouble(value);
                            row.add(d + "");
                        } catch (Exception e) {
                        }
                    } else {
                        row.add(value);
                    }
                }
                int size = row.size();
                if (size == 0) {
                    continue;
                }
                if (size > colsNumber) {
                    colsNumber = size;
                }
                data.add(row);
            }
            rowsNumber = data.size();
            if (rowsNumber == 0 || colsNumber == 0) {
                popError(message("InvalidData"));
                return;
            }
            sheet = new String[rowsNumber][colsNumber];
            StringTable table = new StringTable(null, baseTitle);
            for (int i = 0; i < rowsNumber; i++) {
                List<String> row = data.get(i);
                for (int j = 0; j < row.size(); j++) {
                    sheet[i][j] = row.get(j);
                }
                table.add(row);
            }
            dataArea.setText(TextTools.dataText(sheet, delimiter));
            webView.getEngine().loadContent(table.html());
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @FXML
    @Override
    public void clearAction() {
        isSettingValues = true;
        inputArea.clear();
        dataArea.clear();
        webView.getEngine().loadContent("");
        isSettingValues = false;
    }

    @FXML
    @Override
    public void pasteAction() {
        inputArea.setText(FxmlControl.getSystemClipboardString());
    }

    @FXML
    @Override
    public void copyAction() {
        if (rowsNumber < 1 || colsNumber < 1) {
            return;
        }
        if (FxmlControl.copyToSystemClipboard(dataArea.getText())) {
            popInformation(message("CopiedToSystemClipboard"));
        }
    }

    @FXML
    public void editHtmlAction() {
        if (rowsNumber < 1 || colsNumber < 1) {
            return;
        }
        HtmlEditorController controller = (HtmlEditorController) openStage(CommonValues.HtmlEditorFxml);
        controller.loadContents(FxmlControl.getHtml(webView));
    }

}
