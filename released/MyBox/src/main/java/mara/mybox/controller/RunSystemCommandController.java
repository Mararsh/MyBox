package mara.mybox.controller;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-3-27
 * @License Apache License Version 2.0
 */
public class RunSystemCommandController extends RunCommandController {

    @FXML
    protected ControlStringSelector cmdController;
    @FXML
    protected Button plusButton;

    public RunSystemCommandController() {
        baseTitle = message("RunSystemCommand");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            cmdController.init(this, baseName + "Saved", example(), 20);

            outputs = "";
            if (plusButton != null) {
                plusButton.setDisable(true);
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
    }

    @Override
    public void setStageStatus() {
        setAsNormal();
    }

    public String example() {
        return "ping github.com";
    }

    public String makeCmd() {
        return cmdController.value();
    }

    @FXML
    @Override
    public void startAction() {
        if (process != null && process.isAlive()) {
            cancelCommand();
            return;
        }
        run(makeCmd());
    }

    @Override
    public boolean beforeRun() {
        cmdController.refreshList();
        StyleTools.setNameIcon(startButton, message("Stop"), "iconStop.png");
        startButton.applyCss();
        if (plusButton != null) {
            plusButton.setDisable(false);
        }
        return true;
    }

    @FXML
    public void plusAction() {
        try {
            if (process != null && process.isAlive()) {
                try ( BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(process.getOutputStream(), Charset.defaultCharset()));) {
                    MyBoxLog.console(cmdController.value());
                    writer.append(cmdController.value());
                } catch (Exception e) {
                    popError(e.toString());
                }
            } else {
                plusButton.setDisable(true);
            }
        } catch (Exception e) {
            popError(e.toString());
        }
    }

    @Override
    public void cancelCommand() {
        super.cancelCommand();
        StyleTools.setNameIcon(startButton, message("Start"), "iconStart.png");
        startButton.applyCss();
        if (plusButton != null) {
            plusButton.setDisable(true);
        }
    }

}
