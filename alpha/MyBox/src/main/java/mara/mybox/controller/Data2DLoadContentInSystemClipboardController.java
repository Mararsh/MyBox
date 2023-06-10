package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-11-27
 * @License Apache License Version 2.0
 */
public class Data2DLoadContentInSystemClipboardController extends BaseChildController {

    protected ControlData2DLoad targetController;

    @FXML
    protected ControlData2DSystemClipboard boardController;
    @FXML
    protected BaseData2DSourceController sourceController;

    public Data2DLoadContentInSystemClipboardController() {
        baseTitle = message("LoadContentInSystemClipboard");
    }

    public void setParameters(ControlData2DLoad target, String text) {
        try {
            targetController = target;

            sourceController.setParameters(this);

            boardController.loadNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    sourceController.loadData(boardController.textData);
                }
            });
            boardController.load(text);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (!sourceController.hasData()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {
            List<List<String>> data;

            @Override
            protected boolean handle() {
                try {
                    data = sourceController.selectedData(this);
                    return data != null && !data.isEmpty();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                try {
                    targetController.loadTmpData(sourceController.checkedColsNames, data);
                    close();
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                task = null;
            }

        };
        start(task);
    }

    @FXML
    public void editAction() {
        boardController.editAction();
    }

    /*
        static
     */
    public static Data2DLoadContentInSystemClipboardController open(ControlData2DLoad parent, String text) {
        try {
            Data2DLoadContentInSystemClipboardController controller
                    = (Data2DLoadContentInSystemClipboardController) WindowTools.openChildStage(
                            parent.getMyWindow(), Fxmls.Data2DLoadContentInSystemClipboardFxml, true);
            controller.setParameters(parent, text);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
