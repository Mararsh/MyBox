package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.stage.Window;
import mara.mybox.MyBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;

/**
 * @Author Mara
 * @CreateDate 2021-7-29
 * @License Apache License Version 2.0
 */
public abstract class MainMenuController_Window extends MainMenuController_Base {

    @FXML
    protected Menu homeMenu;
    @FXML
    protected CheckMenuItem alwayOnTopCheck;

    @Override
    public void initControls() {
        try {
            super.initControls();

            homeMenu.setOnShowing((Event e) -> {
                checkTop();
            });
            checkTop();

        } catch (Exception e) {
            MyBoxLog.debug(e);
        }
    }

    protected void checkTop() {
        if (getMyStage() == null) {
            return;
        }
        alwayOnTopCheck.setSelected(myStage.isAlwaysOnTop());
    }

    @FXML
    protected void showHome(ActionEvent event) {
        openStage(Fxmls.MyboxFxml);
    }

    @FXML
    protected void SnapshotWindow(ActionEvent event) {
        ImageEditorController.openImage(NodeTools.snap(parentController.thisPane));
    }

    @FXML
    protected void SnapshotPane(ActionEvent event) {
        thisPane.getChildren().remove(mainMenuPane);
        ImageEditorController.openImage(NodeTools.snap(parentController.thisPane));
        thisPane.getChildren().add(0, mainMenuPane);
    }

    @FXML
    protected void resetWindows(ActionEvent event) {
        WindowTools.resetWindows();
        refreshInterface();
    }

    @FXML
    protected void fullScreen(ActionEvent event) {
        parentController.getMyStage().setFullScreen(true);
    }

    @FXML
    protected void restart(ActionEvent event) {
        MyBox.restart();
    }

    @FXML
    protected void closeWindow(ActionEvent event) {
        parentController.closeStage();
    }

    @FXML
    protected void closeOtherWindows(ActionEvent event) {
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            if (parentController != null) {
                if (!window.equals(parentController.getMyStage())) {
                    window.hide();
                }
            } else {
                if (!window.equals(myStage)) {
                    window.hide();
                }
            }
        }
    }

    @FXML
    protected void WindowsList(ActionEvent event) {
        openStage(Fxmls.WindowsListFxml);
    }

    @Override
    public BaseController refreshInterface() {
        parentController.refreshInterface();
        return super.refreshInterface();
    }

    @FXML
    @Override
    public BaseController refreshInterfaceAndFile() {
        parentController.refreshInterfaceAndFile();
        return super.refreshInterface();
    }

    @FXML
    @Override
    public BaseController reload() {
        return parentController.reload();
    }

    @FXML
    public void AlwayOnTop() {
        if (getMyStage() == null) {
            return;
        }
        setAlwaysTop(alwayOnTopCheck.isSelected(), true);
    }

    @FXML
    protected void exit(ActionEvent event) {
        WindowTools.appExit();
    }

}
