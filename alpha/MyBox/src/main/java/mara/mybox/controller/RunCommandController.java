package mara.mybox.controller;

import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.HtmlWriteTools;
import mara.mybox.tools.StringTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-3-4
 * @License Apache License Version 2.0
 */
public class RunCommandController extends HtmlPopController {

    protected Process process;
    protected String outputs = "";
    protected Charset charset;

    public RunCommandController() {
        charset = SystemTools.ConsoleCharset();
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            webViewController.scrollType = ControlWebView.ScrollType.Bottom;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean beforeRun() {
        return true;
    }

    public void run(String cmd) {
        if (cmd == null || cmd.isBlank()) {
            popError(message("InvalidData"));
            return;
        }
        if (!beforeRun()) {
            return;
        }
        output("<font color=\"blue\"><b>&gt;&nbsp;" + HtmlWriteTools.stringToHtml(cmd) + "</b></font><br>");
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    List<String> p = new ArrayList<>();
                    p.addAll(Arrays.asList(StringTools.splitBySpace(cmd)));
                    ProcessBuilder pb = new ProcessBuilder(p).redirectErrorStream(true);
                    process = pb.start();
                    try (BufferedReader inReader = process.inputReader(charset)) {
                        String line;
                        while ((line = inReader.readLine()) != null) {
                            output(HtmlWriteTools.stringToHtml(line) + "<br>");
                        }
                    }
                    process.waitFor();
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                setStatus(message("Completed"));
            }

            @Override
            protected void whenCanceled() {
                setStatus(message("Canceled"));
            }

            @Override
            protected void whenFailed() {
                setStatus(message("Failed") + "<br>\n" + error);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                cancelCommand();
            }
        };
        start(task, false);
    }

    public void cancelCommand() {
        if (process != null) {
            process.destroyForcibly();
            process = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    protected void output(String msg) {
        Platform.runLater(() -> {
            outputs += msg;
            String html = HtmlWriteTools.html(null, charset.name(), null, "<body>" + outputs + "</body>");
            loadContents(html);
        });
        Platform.requestNextPulse();
    }

    protected void setStatus(String msg) {
        Platform.runLater(() -> {
            String html = HtmlWriteTools.html(null, charset.name(), null, "<body>" + outputs + "<br><hr>\n" + msg + "</body>");
            loadContents(html);
        });
        Platform.requestNextPulse();
    }

    @FXML
    @Override
    public void clearAction() {
        outputs = "";
        loadContents("");
    }

    /*
        static
     */
    public static RunCommandController open(BaseController parent, String cmd) {
        try {
            RunCommandController controller = (RunCommandController) WindowTools.referredTopStage(
                    parent, Fxmls.RunCommandFxml);
            controller.run(cmd);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
