package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-11-27
 * @License Apache License Version 2.0
 */
public class Data2DLoadContentInSystemClipboardController extends BaseChildController {

    protected BaseData2DLoadController targetController;

    @FXML
    protected ControlData2DSystemClipboard boardController;
    @FXML
    protected BaseData2DSelectRowsController sourceController;

    public Data2DLoadContentInSystemClipboardController() {
        baseTitle = message("LoadContentInSystemClipboard");
    }

    public void setParameters(BaseData2DLoadController target, String text) {
        try {
            targetController = target;

            sourceController.setParameters(this);

            boardController.loadNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    sourceController.loadDef(boardController.textData);
                }
            });
            boardController.load(text);

        } catch (Exception e) {
            MyBoxLog.error(e);
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
        task = new FxSingletonTask<Void>(this) {
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
                    targetController.loadData(sourceController.checkedColsNames, data);
                    close();
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
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
    public static Data2DLoadContentInSystemClipboardController open(BaseData2DLoadController parent, String text) {
        try {
            Data2DLoadContentInSystemClipboardController controller
                    = (Data2DLoadContentInSystemClipboardController) WindowTools.childStage(
                            parent, Fxmls.Data2DLoadContentInSystemClipboardFxml);
            controller.setParameters(parent, text);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
