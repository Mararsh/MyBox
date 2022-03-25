package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.fxml.style.NodeStyleTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-20
 * @License Apache License Version 2.0
 */
public class JavaScriptEditor extends TreeNodeEditor {

    protected JavaScriptController jsController;
    protected String outputs = "";

    @FXML
    protected Button clearCodesButton;

    public JavaScriptEditor() {
        defaultExt = "js";
    }

    @Override
    public void setControlsStyle() {
        try {
            super.setControlsStyle();

            NodeStyleTools.setTooltip(clearCodesButton, new Tooltip(message("Clear") + "\nCTRL+g"));
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    protected void setParameters(JavaScriptController jsController) {
        this.jsController = jsController;
    }

    @Override
    protected void showEditorPane() {
    }

    @FXML
    @Override
    public void startAction() {
        try {
            if (jsController.sourceWebView == null) {
                popError(message("InvalidParameters") + ": Source WebView ");
                return;
            }
            String script = valueInput.getText();
            if (script == null || script.isBlank()) {
                popError(message("InvalidParameters") + ": JavaScript");
                return;
            }
            jsController.rightPaneCheck.setSelected(true);
            String ret;
            try {
                Object o = jsController.sourceWebView.webEngine.executeScript(script);
                if (o != null) {
                    ret = o.toString();
                } else {
                    ret = "";
                }
            } catch (Exception e) {
                ret = e.toString();
            }
            outputs += DateTools.nowString() + "<div class=\"valueText\" >"
                    + HtmlWriteTools.stringToHtml(script) + "</div>";
            outputs += "<div class=\"valueBox\">" + HtmlWriteTools.stringToHtml(ret) + "</div><br><br>";
            String html = HtmlWriteTools.html(null, HtmlStyles.DefaultStyle, "<body>" + outputs + "</body>");
            jsController.outputController.loadContents(html);
            popDone();
            TableStringValues.add("JavaScriptHistories", script.trim());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    protected void popHistories(MouseEvent mouseEvent) {
        PopTools.popStringValues(this, valueInput, mouseEvent, "JavaScriptHistories");
    }

    @Override
    public void cleanPane() {
        try {
            cancelAction();
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
