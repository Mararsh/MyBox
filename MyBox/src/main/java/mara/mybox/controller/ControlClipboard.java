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
public class ControlClipboard extends BaseController {

    protected String[][] sheet;
    protected int colsNumber, rowsNumber;
    protected TextArea textArea;

    @FXML
    protected ControlDataText textController;
    @FXML
    protected WebView webView;

    public ControlClipboard() {
        baseTitle = message("DataClipboard");
    }

    public void setControls(String baseName) {
        try {
            this.baseName = baseName;
            textController.setControls(baseName);
            textArea = textController.textArea;

        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    protected void update(String[][] sheet) {
        try {
            this.sheet = sheet;
            textController.update(sheet);
            if (sheet == null || sheet.length == 0) {
                webView.getEngine().loadContent("");
                rowsNumber = colsNumber = 0;
                return;
            }
            rowsNumber = sheet.length;
            colsNumber = sheet[0].length;
            StringTable table = new StringTable();
            for (int i = 0; i < rowsNumber; i++) {
                List<String> row = new ArrayList<>();
                for (int j = 0; j < colsNumber; j++) {
                    row.add(sheet[i][j]);
                }
                table.add(row);
            }
            webView.getEngine().loadContent(table.html());
        } catch (Exception e) {
            MyBoxLog.console(e.toString());
        }
    }

    @FXML
    public void copyText() {
        if (FxmlControl.copyToSystemClipboard(textController.textArea.getText())) {
            popInformation(message("CopiedToSystemClipboard"));
        }
    }

    @FXML
    public void copyHtml() {
        if (FxmlControl.copyToSystemClipboard(FxmlControl.getHtml(webView))) {
            popInformation(message("CopiedToSystemClipboard"));
        }
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
