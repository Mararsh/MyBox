package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.ControlStyle;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.HtmlTools;
import mara.mybox.tools.StringTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-27
 * @License Apache License Version 2.0
 */
public class RunSystemCommandController extends HtmlViewerController {

    protected Process process;

    @FXML
    protected ControlStringSelector cmdController;

    public RunSystemCommandController() {
        baseTitle = message("RunSystemCommand");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            cmdController.init(this, baseName + "Saved", "ping github.com", 20);

        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @FXML
    @Override
    public void startAction() {
        if (startButton.getUserData() != null) {
            cancel();
            return;
        }
        webView.getEngine().loadContent("");
        String cmd = cmdController.value();
        if (cmd == null || cmd.isBlank()) {
            popError(message("InvalidData"));
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            cmdController.disable(true);
            cmdController.refreshList();
            ControlStyle.setNameIcon(startButton, message("Stop"), "iconStart.png");
            startButton.applyCss();
            startButton.setUserData("started");
            task = new SingletonTask<Void>() {

                private String lastText, htmlStyle;

                @Override
                protected boolean handle() {
                    try {
                        lastText = "";
                        htmlStyle = AppVariables.getUserConfigValue(baseName + "HtmlStyle", "Default");
                        List<String> p = new ArrayList<>();
                        p.addAll(Arrays.asList(StringTools.splitBySpace(cmd)));
                        ProcessBuilder pb = new ProcessBuilder(p).redirectErrorStream(true);
                        process = pb.start();
                        try ( BufferedReader inReader = new BufferedReader(
                                new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
                            String line;
                            while ((line = inReader.readLine()) != null) {
                                String msg = line + "\n";
                                Platform.runLater(() -> {
                                    lastText += msg;
                                    String html = HtmlTools.html(null, htmlStyle, "<body><PRE>" + lastText + "</PRE></body>");
                                    displayHtml(html);
                                });
                            }
                        }
                        process.waitFor();
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                protected void setStatus(String msg) {
                    String html = HtmlTools.html(null, htmlStyle, "<body><PRE>" + lastText + "</PRE>"
                            + "<br><hr>\n" + msg + "</body>");
                    displayHtml(html);
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
                    cmdController.disable(false);
                    cancel();
                }
            };
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    public void cancel() {
        ControlStyle.setNameIcon(startButton, message("Start"), "iconStart.png");
        startButton.applyCss();
        startButton.setUserData(null);
        if (process != null) {
            process.destroyForcibly();
            process = null;
        }
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @FXML
    @Override
    public void pasteAction() {
        String string = FxmlControl.getSystemClipboardString();
        if (string != null && !string.isBlank()) {
            cmdController.set(string);
        }
    }

}
