package mara.mybox.controller;

import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MainMenuController_Base extends BaseController {

    @FXML
    protected Pane mainMenuPane;
    @FXML
    protected MenuBar menuBar;

    @Override
    public Stage getMyStage() {
        if (myStage == null) {
            if (mainMenuPane != null && mainMenuPane.getScene() != null) {
                myStage = (Stage) mainMenuPane.getScene().getWindow();
            }
        }
        return myStage;
    }

    @Override
    public BaseController loadScene(String newFxml) {
        try {
            if (AppVariables.closeCurrentWhenOpenTool) {
                return parentController.loadScene(newFxml);
            } else {
                return parentController.openStage(newFxml);
            }
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    @Override
    public BaseController openStage(String newFxml) {
        return parentController.openStage(newFxml);
    }

}
