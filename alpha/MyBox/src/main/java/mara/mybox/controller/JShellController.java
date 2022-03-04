package mara.mybox.controller;

import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.HtmlWriteTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-4
 * @License Apache License Version 2.0
 */
public class JShellController extends BaseWebViewController {

    protected JShell jShell;
    protected String outputs = "";

    @FXML
    protected TextArea codesArea;

    public JShellController() {
        baseTitle = message("JShell");
        TipsLabelKey = "NotesComments";
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            jShell = JShell.create();
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void startAction() {
        try {
            String codes = codesArea.getText();
            if (codes == null || codes.isBlank()) {
                popError(message("NoData"));
                return;
            }
            startButton.setDisable(true);
//            String[] statements = codes.split(";");
//            for (String statement : statements) {
//              
//            }
            List<SnippetEvent> events = jShell.eval(codes);
            for (SnippetEvent e : events) {
                output("<div><font color=\"blue\"><b>&gt;&nbsp;"
                        + HtmlWriteTools.stringToHtml(e.snippet().source()) + "</b></font></div>");
//                output(e.status() + " " + e.snippet().kind() + " " + e.snippet().subKind() + " " + e.value());
                if (e.causeSnippet() != null) {
                    continue;
                }
                switch (e.status()) {
                    case VALID:
                        if (e.value() != null) {
                            output(message("Value") + ": " + HtmlWriteTools.stringToHtml(e.value()) + "<br>");
                        } else {
                            output(message("Successful") + "<br>");
                        }
                        break;
                    case RECOVERABLE_DEFINED:
                        output("With unresolved references" + "<br>");
                        break;
                    case RECOVERABLE_NOT_DEFINED:
                        output("Possibly reparable, failed" + "<br>");
                        break;
                    case REJECTED:
                        output(message("Failed") + "<br>");
                        break;
                }
            }
            for (Snippet snippet : jShell.snippets().toList()) {

            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        startButton.setDisable(false);
    }

    protected void output(String msg) {
        Platform.runLater(() -> {
            outputs += msg;
            String html = HtmlWriteTools.html(null, "<body>" + outputs + "</body>");
            loadContents(html);
        });

    }

    @FXML
    protected void clearCodes() {
        codesArea.clear();
    }

    @FXML
    protected void popExamplesMenu(MouseEvent mouseEvent) {
//        PopTools.popTableDefinition(this, codesArea, mouseEvent) ;
    }

    @FXML
    protected void popCodesHistories(MouseEvent mouseEvent) {
//        PopTools.popTableDefinition(this, codesArea, mouseEvent) ;
    }

}
