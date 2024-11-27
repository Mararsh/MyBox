package mara.mybox.controller;

import java.io.File;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.HelpTools;
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
public class ControlDataJavascript extends BaseDataValuesController {

    protected ControlWebView htmlWebView;
    protected String outputs = "";

    @FXML
    protected TextArea scriptInput;
    @FXML
    protected CheckBox wrapCheck;
    @FXML
    protected Tab htmlTab;
    @FXML
    protected ControlWebView outputController;
    @FXML
    protected WebAddressController htmlController;

    public ControlDataJavascript() {
        TipsLabelKey = "JavaScriptTips";
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Javascript);
    }

    @Override
    public void initEditor() {
        try {
            valueInput = scriptInput;
            valueWrapCheck = wrapCheck;
            valueName = "script";
            super.initEditor();

            outputController.setParent(this, ControlWebView.ScrollType.Bottom);

            htmlWebView = htmlController.webViewController;
            if (htmlController != null) {
                htmlController.webViewController.setParent(this, ControlWebView.ScrollType.Bottom);
                htmlController.loadContents(HtmlWriteTools.emptyHmtl(message("AppTitle")));
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(ControlWebView sourceWebView) {
        htmlWebView = sourceWebView;
        tabPane.getTabs().remove(htmlTab);
    }

    @FXML
    @Override
    public void startAction() {
        runScirpt(getScript());
    }

    public String getScript() {
        return scriptInput.getText();
    }

    public void runScirpt(String script) {
        try {
            if (htmlWebView == null) {
                popError(message("InvalidParameters") + ": Source WebView ");
                return;
            }
            if (script == null || script.isBlank()) {
                popError(message("InvalidParameters") + ": JavaScript");
                return;
            }
            if (rightPaneCheck != null) {
                rightPaneCheck.setSelected(true);
            }
            String ret;
            try {
                Object o = htmlWebView.webEngine.executeScript(script);
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
            outputController.loadContents(html);
            TableStringValues.add("JavaScriptHistories", script.trim());
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void popJavascriptHelps(Event event) {
        if (UserConfig.getBoolean("JavaScriptHelpsPopWhenMouseHovering", false)) {
            showJavascriptHelps(event);
        }
    }

    @FXML
    public void showJavascriptHelps(Event event) {
        popEventMenu(event, HelpTools.javascriptHelps());
    }

    @FXML
    protected void popExamplesMenu(MouseEvent event) {
        if (UserConfig.getBoolean(interfaceName + "ExamplesPopWhenMouseHovering", false)) {
            showExamplesMenu(event);
        }
    }

    @FXML
    protected void showExamplesMenu(Event event) {
        PopTools.popJavaScriptExamples(this, event, scriptInput, interfaceName + "Examples", null);
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

    @Override
    public void cleanPane() {
        try {
            cancelAction();
        } catch (Exception e) {
        }
        super.cleanPane();
    }

    /*
        static
     */
    public static DataTreeController open(ControlWebView controlWebView) {
        try {
            DataTreeController controller = DataTreeController.javascript(controlWebView, false);
//            ((ControlDataJavascript) controller.nodeController.dataController).setParameters(controlWebView); ###########
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataTreeController loadScript(String script) {
        try {
            DataTreeController controller = DataTreeController.javascript(null, false);
//            ((ControlDataJavascript) controller.nodeController.dataController).edit(script);  ###########
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataTreeController openFile(File file) {
        try {
            DataTreeController controller = DataTreeController.javascript(null, false);
//            ((ControlDataJavascript) controller.nodeController.dataController).selectSourceFile(file); ###########
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
