package mara.mybox.controller;

import java.io.File;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import mara.mybox.db.table.TableNodeJavaScript;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.HtmlStyles;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2022-3-20
 * @License Apache License Version 2.0
 */
public class ControlDataJavascript extends BaseJavaScriptController {

    protected String outputs = "";

    @FXML
    protected Tab htmlTab;
    @FXML
    protected ControlWebView outputController;
    @FXML
    protected WebAddressController htmlController;

    @Override
    public void initEditor() {
        try {
            super.initEditor();

            outputController.setParent(this, ControlWebView.ScrollType.Bottom);

            MyBoxLog.console(htmlController != null);

            htmlWebView = htmlController.webViewController;
            htmlController.webViewController.setParent(this, ControlWebView.ScrollType.Bottom);
            htmlController.loadContents(HtmlWriteTools.emptyHmtl(message("AppTitle")));

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void afterTask(boolean ok) {
        try {
            outputs += DateTools.nowString() + "<div class=\"valueText\" >"
                    + HtmlWriteTools.stringToHtml(script) + "</div>";
            outputs += "<div class=\"valueBox\">" + HtmlWriteTools.stringToHtml(results) + "</div><br><br>";
            String html = HtmlWriteTools.html(null, HtmlStyles.DefaultStyle, "<body>" + outputs + "</body>");
            outputController.loadContent(html);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        right pane
     */
    @FXML
    public void editResults() {
        outputController.editAction();
    }

    @FXML
    public void clearResults() {
        outputs = "";
        outputController.clear();
    }

    public void edit(String script) {
        scriptInput.setText(script);
    }

    @FXML
    protected void showHtmlStyle(Event event) {
        PopTools.popHtmlStyle(event, outputController);
    }

    @FXML
    protected void popHtmlStyle(Event event) {
        if (UserConfig.getBoolean("HtmlStylesPopWhenMouseHovering", false)) {
            showHtmlStyle(event);
        }
    }

    /*
        static
     */
    public static DataTreeNodeEditorController openScriptEditor(BaseController parent) {
        try {
            DataTreeNodeEditorController controller
                    = DataTreeNodeEditorController.openTable(parent, new TableNodeJavaScript());
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataTreeNodeEditorController open(ControlWebView controlWebView) {
        try {
            DataTreeNodeEditorController controller = openScriptEditor(controlWebView);
            ((ControlDataJavascript) controller.valuesController).setParameters(controlWebView);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataTreeNodeEditorController loadScript(BaseController parent, String script) {
        try {
            DataTreeNodeEditorController controller = openScriptEditor(parent);
            ((ControlDataJavascript) controller.valuesController).edit(script);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataTreeNodeEditorController openFile(BaseController parent, File file) {
        try {
            DataTreeNodeEditorController controller = openScriptEditor(parent);
            ((ControlDataJavascript) controller.valuesController).selectSourceFile(file);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
