package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;
import mara.mybox.data.StringTable;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-12-26
 * @License Apache License Version 2.0
 */
public class ControlSheetData extends BaseController {

    protected String[][] sheet;
    protected int colsNumber, rowsNumber;
    protected TextArea textArea;

    @FXML
    protected ControlDataText textController;
    @FXML
    protected WebView webView;

    public ControlSheetData() {
        baseTitle = message("Data");
    }

    // Should always run this after scene loaded and before input data
    public void setControls(BaseController parent, String baseName) {
        try {
            this.parentController = parent;
            this.baseName = baseName;
            if (textController != null) {
                textController.setControls(baseName);
                textArea = textController.textArea;
            }

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    protected String[][] data() {
        if (sheet == null || sheet.length == 0) {
            rowsNumber = colsNumber = 0;
        } else {
            rowsNumber = sheet.length;
            colsNumber = sheet[0].length;
        }
        return sheet;
    }

    protected void setData(String[][] sheet) {
        try {
            this.sheet = sheet;
            data();
            updateDataHtml(sheet);
            updateDataText(sheet);
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    protected void updateDataText(String[][] data) {
        if (textController != null) {
            textController.update(data);
        }
    }

    protected void updateDataHtml(String[][] data) {
        if (webView != null) {
            webView.getEngine().loadContent(html(data));
        }
    }

    public String html(String[][] data) {
        try {
            if (data == null || data.length == 0) {
                return "";
            }
            int rNumber = data.length;
            int cNumber = data[0].length;
            if (cNumber == 0) {
                return "";
            }
            StringTable table = new StringTable();
            for (int i = 0; i < rNumber; i++) {
                List<String> row = new ArrayList<>();
                for (int j = 0; j < cNumber; j++) {
                    row.add(data[i][j]);
                }
                table.add(row);
            }
            return table.html();
        } catch (Exception e) {
            MyBoxLog.console(e);
            return "";
        }
    }

    @FXML
    public void copyText() {
        copyToSystemClipboard(textController.textArea.getText());
    }

    @FXML
    public void copyHtml() {
        copyToSystemClipboard(FxmlControl.getHtml(webView));
    }

    @FXML
    public void editText() {
        TextEditerController controller = (TextEditerController) openStage(CommonValues.TextEditerFxml);
        controller.loadContexts(textController.textArea.getText());
    }

    @FXML
    public void editHtml() {
        HtmlEditorController controller = (HtmlEditorController) openStage(CommonValues.HtmlEditorFxml);
        controller.loadContents(FxmlControl.getHtml(webView));
    }

}
