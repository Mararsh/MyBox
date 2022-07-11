package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-11-28
 * @License Apache License Version 2.0
 */
public class Data2DDeleteController extends BaseData2DHandleController {

    @FXML
    protected CheckBox errorContinueCheck;

    public Data2DDeleteController() {
        baseTitle = message("Delete");
    }

    @Override
    public void initControls() {
        super.initControls();
        noColumnSelection(true);
    }

    @Override
    public synchronized void handleRowsTask() {
        try {
            tableController.isSettingValues = true;
            List<List<String>> data = new ArrayList<>();
            for (int i = 0; i < tableController.tableData.size(); i++) {
                if (!checkedRowsIndices.contains(i)) {
                    data.add(tableController.tableData.get(i));
                }
            }
            tableController.tableData.setAll(data);
            tableController.isSettingValues = false;
            tableController.tableChanged(true);
            tableController.requestMouse();
            tabPane.getSelectionModel().select(dataTab);
            popDone();
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void handleAllTask() {
        if (!tableController.checkBeforeNextAction()) {
            return;
        }
        if (isAllPages() && !data2D.needFilter()) {
            if (!PopTools.askSure(this, baseTitle, message("SureDeleteAll"))) {
                return;
            }
        }
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    if (!data2D.isTmpData() && tableController.dataController.backupController != null
                            && tableController.dataController.backupController.isBack()) {
                        tableController.dataController.backupController.addBackup(task, data2D.getFile());
                    }
                    data2D.setTask(task);
                    data2D.startFilterService(task);
                    ok = data2D.delete(errorContinueCheck.isSelected());
                    data2D.stopFilterService();
                    return ok;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                popDone();
                tableController.dataSizeLoaded = false;
                tableController.dataController.goPage();
                tableController.requestMouse();
                tabPane.getSelectionModel().select(dataTab);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopFilterService();
                data2D.setTask(null);
                task = null;
            }

        };
        start(task);
    }

    /*
        static
     */
    public static Data2DDeleteController open(ControlData2DEditTable tableController) {
        try {
            Data2DDeleteController controller = (Data2DDeleteController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DDeleteFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
