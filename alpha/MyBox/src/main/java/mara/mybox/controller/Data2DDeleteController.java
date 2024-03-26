package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-11-28
 * @License Apache License Version 2.0
 */
public class Data2DDeleteController extends BaseData2DTargetsController {

    protected Data2DManufactureController editor;

    @FXML
    protected CheckBox errorContinueCheck;

    public Data2DDeleteController() {
        baseTitle = message("Delete");
    }

    public void setParameters(Data2DManufactureController tableController) {
        try {
            editor = tableController;
            super.setParameters(tableController);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public synchronized void handleRowsTask() {
        task = new FxSingletonTask<Void>(this) {

            List<List<String>> data;

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(this, filterController.filter);
                    data = new ArrayList<>();
                    filteredRowsIndices = filteredRowsIndices();
                    for (int i = 0; i < tableController.tableData.size(); i++) {
                        if (!filteredRowsIndices.contains(i)) {
                            data.add(tableController.tableData.get(i));
                        }
                    }
                    data2D.stopFilter();
                    return ok;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                try {
                    tableController.updateTable(data);
                    selectedRowsIndices = null;
                    tableController.tableChanged(true);
                    tableController.requestMouse();
                    tabPane.getSelectionModel().select(dataTab);
                    alertInformation(message("DeletedRowsNumber") + ": " + filteredRowsIndices.size());
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                if (targetController != null) {
                    targetController.refreshControls();
                }
            }

        };
        start(task);
    }

    @Override
    public void handleAllTask() {
        boolean clearAll = !data2D.needFilter();
        if (clearAll) {
            if (!PopTools.askSure(getTitle(), message("SureDeleteAll"))) {
                return;
            }
        } else {
            if (!PopTools.askSure(getTitle(), message("SureDelete"))) {
                return;
            }
        }
        task = new FxSingletonTask<Void>(this) {

            private long count;
            private boolean needBackup = false;

            @Override
            protected boolean handle() {
                try {
                    needBackup = data2D.isDataFile() && !data2D.isTmpData()
                            && UserConfig.getBoolean(baseName + "BackupWhenSave", true);
                    if (needBackup) {
                        addBackup(this, data2D.getFile());
                    }
                    data2D.startTask(this, filterController.filter);
                    if (clearAll) {
                        count = data2D.clearData();
                    } else {
                        count = data2D.deleteRows(errorContinueCheck.isSelected());
                    }
                    data2D.stopFilter();
                    return count >= 0;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                selectedRowsIndices = null;
                tableController.dataSizeLoaded = false;
                tableController.goPage();
                tableController.requestMouse();
                tabPane.getSelectionModel().select(dataTab);
                alertInformation(message("DeletedRowsNumber") + ": " + count);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
            }

        };
        start(task);
    }

    /*
        static
     */
    public static Data2DDeleteController open(Data2DManufactureController tableController) {
        try {
            Data2DDeleteController controller = (Data2DDeleteController) WindowTools.branchStage(
                    tableController, Fxmls.Data2DDeleteFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
