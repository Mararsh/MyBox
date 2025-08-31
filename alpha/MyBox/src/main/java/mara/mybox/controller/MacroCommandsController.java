package mara.mybox.controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2025-8-29
 * @License Apache License Version 2.0
 */
public class MacroCommandsController extends BaseTaskController {

    @FXML
    protected TextArea cmdsInput;

    public MacroCommandsController() {
        baseTitle = message("MacroCommands");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            cmdsInput.setText(example());

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

    }

    @FXML
    protected void showCmdHistories(Event event) {
        PopTools.popSavedValues(this, cmdsInput, event, "MacroCommandsHistories", true);
    }

    @FXML
    protected void popCmdHistories(Event event) {
        if (UserConfig.getBoolean("MacroCommandsHistoriesPopWhenMouseHovering", false)) {
            showCmdHistories(event);
        }
    }

}
