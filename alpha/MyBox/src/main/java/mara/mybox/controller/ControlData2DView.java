package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import mara.mybox.data.Data2D;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WebViewTools;
import mara.mybox.tools.TextTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-10-18
 * @License Apache License Version 2.0
 *
 */
public class ControlData2DView extends BaseController {

    protected ControlData2D dataController;
    protected String displayDelimiterName;
    protected Data2D data2D;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab htmlTab, textTab;
    @FXML
    protected CheckBox formCheck, titleCheck, columnCheck, rowCheck, allCheck;
    @FXML
    protected TextArea textArea;
    @FXML
    protected ControlWebView htmlController;

    protected void setParameters(ControlData2D dataController) {
        try {
            this.dataController = dataController;
            this.baseName = dataController.baseName;
            data2D = dataController.data2D;

            htmlController.setParent(parentController);

            titleCheck.setSelected(UserConfig.getBoolean(baseName + "HtmlTitle", true));
            titleCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                updateHtml();
                UserConfig.setBoolean(baseName + "HtmlTitle", newValue);
            });
            columnCheck.setSelected(UserConfig.getBoolean(baseName + "HtmlColumn", false));
            columnCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                updateHtml();
                UserConfig.setBoolean(baseName + "HtmlColumn", newValue);
            });
            rowCheck.setSelected(UserConfig.getBoolean(baseName + "HtmlRow", false));
            rowCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                updateHtml();
                UserConfig.setBoolean(baseName + "HtmlRow", newValue);
            });
            if (allCheck != null) {
//            allCheck.setSelected(UserConfig.getBoolean(baseName + "HtmlAll", false));
                allCheck.selectedProperty().addListener((ChangeListener<Boolean>) (observable, oldValue, newValue) -> {
                    updateHtml();
//                UserConfig.setBoolean(baseName + "HtmlAll", newValue);
                });
            }
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    public void loadData() {
        try {
            updateHtml();
            updateText();
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @FXML
    public void editAction() {

    }

    /*
        Display Html
     */
    protected void updateHtml() {
        if (data2D.isMutiplePages() && allCheck != null && allCheck.isSelected()) {
            displayAllHtml();
        } else {
            displayPageHtml();
        }
    }

    protected void displayPageHtml() {
        try {
            int rNumber = data2D.pageRowsNumber();
            int cNumber = data2D.pageColsNumber();
            if (rNumber <= 0 || cNumber <= 0) {
                htmlController.webEngine.loadContent("");
                return;
            }
            List<String> names;
            if (columnCheck.isSelected()) {
                names = new ArrayList<>();
                if (rowCheck.isSelected()) {
                    names.add("");
                }
                for (int i = 0; i < cNumber; i++) {
                    names.add(data2D.colName(i));
                }
            } else {
                names = null;
            }
            String title = null;
            if (titleCheck.isSelected()) {
                title = data2D.titleName();
            }
            StringTable table = new StringTable(names, title);
            for (int i = 0; i < rNumber; i++) {
                List<String> row = new ArrayList<>();
                if (rowCheck.isSelected()) {
                    row.add(data2D.rowName(i));
                }
                for (int j = 0; j < cNumber; j++) {
                    row.add(data2D.cell(i, j));
                }
                table.add(row);
            }
            htmlController.webEngine.loadContent(table.html());
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    protected void displayAllHtml() {
        displayPageHtml();
    }

    protected int pageHtml(StringTable table, int inIndex) {
        int index = inIndex;
        try {
            int len = data2D.pageRowsNumber();
            for (int r = 0; r < len; r++) {
                List<String> values = new ArrayList<>();
                if (rowCheck.isSelected()) {
                    values.add(message("Row") + (index + 1));
                }
                values.addAll(data2D.rowList(r));
                table.add(values);
                index++;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return index;
    }

    @FXML
    public void editHtml() {
        HtmlEditorController controller = (HtmlEditorController) openStage(Fxmls.HtmlEditorFxml);
        controller.loadContents(WebViewTools.getHtml(htmlController.webEngine));
    }

    /*
        Display Text
     */
    protected void updateText() {
        if (data2D.isMutiplePages() && allCheck != null && allCheck.isSelected()) {
            displayAllText();
        } else {
            displayPageText();
        }
    }

    protected void displayPageText() {
        List<String> colsNames = null;
        List<String> rowsNames = null;
        String title = null;
        if (titleCheck.isSelected()) {
            title = data2D.titleName();
        }
        if (columnCheck.isSelected()) {
            colsNames = data2D.columnNames();
        }
        List<List<String>> pageData = data2D.getTableData();
        if (rowCheck.isSelected()) {
            rowsNames = pageData == null ? null : data2D.rowNames(pageData.size());
        }
        String text = TextTools.dataText(pageData, displayDelimiterName, colsNames, rowsNames);
        if (title != null && !title.isBlank()) {
            textArea.setText(title + "\n\n" + text);
        } else {
            textArea.setText(text);
        }
    }

    protected void displayAllText() {
        displayPageText();
    }

    protected void rowText(StringBuilder s, int index, List<String> values, String delimiter) {
        try {
            if (rowCheck.isSelected()) {
                if (index == -1) {
                    s.append(delimiter);
                } else if (index >= 0) {
                    s.append(message("Row")).append(index + 1).append(delimiter);
                }
            }
            int end = values.size() - 1;
            for (int c = 0; c <= end; c++) {
                s.append(values.get(c));
                if (c < end) {
                    s.append(delimiter);
                }
            }
            s.append("\n");
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected int pageText(StringBuilder s, int inIndex, String delimiter) {
        int index = inIndex;
        try {
            int len = data2D.pageRowsNumber();
            for (int r = 0; r < len; r++) {
                rowText(s, index++, data2D.rowList(r), delimiter);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return index;
    }

    @FXML
    public void editText() {
        TextEditorController controller = (TextEditorController) openStage(Fxmls.TextEditorFxml);
        controller.loadContents(textArea.getText());
    }
}
