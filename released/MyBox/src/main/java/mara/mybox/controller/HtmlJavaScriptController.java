package mara.mybox.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.event.Event;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2022-3-20
 * @License Apache License Version 2.0
 */
public class HtmlJavaScriptController extends BaseJavaScriptController {

    public HtmlJavaScriptController() {
        baseTitle = "JavaScript";
    }

    @Override
    public void afterTask(boolean ok) {
        if (error != null) {
            popError(error);
        }
    }

    @FXML
    @Override
    protected void showExamplesMenu(Event event) {
        try {
            List<List<String>> preValues = new ArrayList<>();
            preValues.add(Arrays.asList(
                    "window.scrollTo(50,70 );",
                    "document.body.contentEditable=true;",
                    "alert(\"" + (Languages.isChinese() ? "人生得意须尽欢！" : "Beauty is everywhere if you know where to look.") + "\");",
                    "confirm(\"" + (Languages.isChinese() ? "能饮一杯无？" : "A gentle word opens an iron gate.") + "\");",
                    "prompt(\"" + (Languages.isChinese() ? "君不见" : "Your cat") + "\");",
                    "window.open(\"https://moon.bao.ac.cn\");",
                    "function addStyle(style) {\n"
                    + "    var node = document.createElement(\"style\");        \n"
                    + "    node.id = \"mystyleid\";        \n"
                    + "    node.type = \"text/css\";        \n"
                    + "    node.innerHTML = style.replace(/\\n/g,\"  \");        \n"
                    + "    document.getElementsByTagName(\"HEAD\").item(0).appendChild(node);\n"
                    + "};\n"
                    + "addStyle(\"body { background-color:black; color:#CCFF99; }\");",
                    "function removeNode(id) {\n"
                    + "    var node = document.getElementById(id);\n"
                    + "    if ( node != null ) \n"
                    + "        node.parentNode.removeChild(node);\n"
                    + "};\n"
                    + "removeNode(\"mystyleid\");",
                    "function selectNode(id) {\n"
                    + "    window.getSelection().removeAllRanges();     \n"
                    + "    var selection = window.getSelection();        \n"
                    + "    var range = document.createRange();        \n"
                    + "    range.selectNode(document.getElementById(id));        \n"
                    + "    selection.addRange(range);\n"
                    + "};\n"
                    + "selectNode(\"someid\");",
                    "alert('selected=' + window.getSelection().toString());",
                    "window.getSelection().removeAllRanges();     \n"
                    + "var selection = window.getSelection();        \n"
                    + "var range = document.createRange();        \n"
                    + "range.selectNode(document.documentElement);        \n"
                    + "selection.addRange(range);",
                    "window.getSelection().removeAllRanges();",
                    "alert('scrollHeight=' + (document.documentElement.scrollHeight || document.body.scrollHeight));",
                    "alert('scrollWidth=' + (document.documentElement.scrollWidth || document.body.scrollWidth));",
                    "alert('document.cookie=' + document.cookie);"
            ));
            PopTools.popJavaScriptExamples(this, event, scriptInput, interfaceName + "Examples", preValues);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static HtmlJavaScriptController open(BaseController parent, ControlWebView sourceWebView) {
        try {
            HtmlJavaScriptController controller
                    = (HtmlJavaScriptController) WindowTools.referredTopStage(parent, Fxmls.HtmlJavaScriptFxml);
            controller.setParameters(sourceWebView);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
