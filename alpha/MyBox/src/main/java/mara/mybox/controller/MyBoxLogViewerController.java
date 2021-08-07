package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.stage.Window;
import mara.mybox.db.data.BaseDataTools;
import mara.mybox.db.table.TableMyBoxLog;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.AppVariables;

import mara.mybox.value.Fxmls;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-11-27
 * @License Apache License Version 2.0
 */
public class MyBoxLogViewerController extends HtmlTableController {

    protected TableMyBoxLog logTable;

    @FXML
    protected CheckBox popCheck;

    public MyBoxLogViewerController() {
        baseTitle = Languages.message("MyBoxLogsViewer");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();
            logTable = new TableMyBoxLog();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            popCheck.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    AppVariables.popErrorLogs = popCheck.isSelected();
                }
            });

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterSceneLoaded() {
        try {
            super.afterSceneLoaded();

            myStage.setWidth(600);
            myStage.setHeight(500);
            myStage.setY(5);
            myStage.setX(NodeTools.getScreen().getWidth() - 610);

        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    @FXML
    public void dataAction(ActionEvent event) {
        MyBoxLogController controller = MyBoxLogController.oneOpen();
        if (controller != null) {
            controller.getMyStage().requestFocus();
        }
    }

    @FXML
    public void messageAction(ActionEvent event) {
        MessageAuthorController controller = (MessageAuthorController) WindowTools.openStage(Fxmls.MessageAuthorFxml);
        controller.loadMessage("MyBox Logs", html);
    }

    @FXML
    public void clearAction(ActionEvent event) {
        body = null;
        displayHtml(null);
    }

    public void addLog(MyBoxLog myboxLog) {
        body = (body != null ? body : "")
                + "</br><hr></br>\n" + BaseDataTools.htmlData(logTable, myboxLog);
        loadBody(body);
    }

    public void setLogs(List<MyBoxLog> logs) {
        body = "";
        for (MyBoxLog log : logs) {
            body += "</br><hr></br>\n" + BaseDataTools.htmlData(logTable, log);
        }
        loadBody(body);
    }

    public static MyBoxLogViewerController oneOpen() {
        MyBoxLogViewerController controller = null;
        List<Window> windows = new ArrayList<>();
        windows.addAll(Window.getWindows());
        for (Window window : windows) {
            Object object = window.getUserData();
            if (object != null && object instanceof MyBoxLogViewerController) {
                try {
                    controller = (MyBoxLogViewerController) object;
                    controller.toFront();
                    break;
                } catch (Exception e) {
                }
            }
        }
        if (controller == null) {
            controller = (MyBoxLogViewerController) WindowTools.openStage(Fxmls.MyBoxLogViewerFxml);
        }
        return controller;
    }

}
