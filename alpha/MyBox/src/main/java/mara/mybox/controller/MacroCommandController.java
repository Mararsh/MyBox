package mara.mybox.controller;

import java.io.BufferedWriter;
import java.nio.charset.Charset;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import mara.mybox.db.table.TableStringValues;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.SystemTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2025-8-29
 * @License Apache License Version 2.0
 */
public class MacroCommandController extends RunCommandController {

    @FXML
    protected TextField cmdInput;
    @FXML
    protected Button plusButton;
    @FXML
    protected ComboBox<String> charsetSelector;

    public MacroCommandController() {
        baseTitle = message("RunSystemCommand");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            cmdInput.setText(example());

            outputs = "";
            if (plusButton != null) {
                plusButton.setDisable(true);
            }

            charsetSelector.getItems().addAll(TextTools.getCharsetNames());
            Charset sc = SystemTools.ConsoleCharset();
            try {
                charset = Charset.forName(UserConfig.getString(baseName + "TextCharset", sc.name()));
            } catch (Exception e) {
                charset = sc;
            }
            charsetSelector.setValue(charset.name());
            charsetSelector.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue ov, String oldValue, String newValue) {
                    charset = Charset.forName(charsetSelector.getSelectionModel().getSelectedItem());
                    UserConfig.setString(baseName + "TextCharset", charset.name());
                }
            });

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    public String example() {
        return "ping sourceforge.net";
    }

    @FXML
    @Override
    public void startAction() {
        if (process != null && process.isAlive()) {
            cancelCommand();
            return;
        }
        String cmd = cmdInput.getText();
        if (cmd == null || cmd.isBlank()) {
            popError(message("InvalidParameters") + ": " + message("Command"));
            return;
        }
        TableStringValues.add("RunSystemCommandHistories", cmd);
        run(cmd);
    }

    @Override
    public boolean beforeRun() {
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
                try (BufferedWriter writer = process.outputWriter()) {
                    writer.append(cmdInput.getText());
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

    @FXML
    protected void showCmdHistories(Event event) {
        PopTools.popSavedValues(this, cmdInput, event, "RunSystemCommandHistories", true);
    }

    @FXML
    protected void popCmdHistories(Event event) {
        if (UserConfig.getBoolean("RunSystemCommandHistoriesPopWhenMouseHovering", false)) {
            showCmdHistories(event);
        }
    }

}
