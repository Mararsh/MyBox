package mara.mybox.controller;

import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-11-27
 * @License Apache License Version 2.0
 */
public class Data2DLoadContentInSystemClipboardController extends ControlData2DSource {

    protected BaseData2DLoadController targetController;
    protected List<List<String>> data;

    @FXML
    protected ControlData2DSystemClipboard boardController;

    public Data2DLoadContentInSystemClipboardController() {
        baseTitle = message("LoadContentInSystemClipboard");
    }

    public void setParameters(BaseData2DLoadController target, String text) {
        try {
            targetController = target;

            initParameters();

            boardController.loadNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    loadDef(boardController.textData);
                }
            });
            boardController.load(text);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        data = selectedData(currentTask);
        return data != null && !data.isEmpty();
    }

    @Override
    public void afterSuccess() {
        try {
            targetController.loadData(null, checkedColumns, data);
            close();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
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
