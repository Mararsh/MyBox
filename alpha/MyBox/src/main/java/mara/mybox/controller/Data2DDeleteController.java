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
public class Data2DDeleteController extends BaseData2DTaskTargetsController {

    protected Data2DManufactureController editor;
    protected boolean clearAll;

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
    public boolean checkOptions() {
        try {
            if (!super.checkOptions()) {
                return false;
            }
            clearAll = false;
            if (sourceController.isAllPages()) {
                clearAll = !filterController.filter.needFilter();
                if (clearAll) {
                    if (!PopTools.askSure(getTitle(), message("SureClearData"))) {
                        return false;
                    }
                } else {
                    if (!PopTools.askSure(getTitle(), message("SureDelete"))) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public synchronized void handleRowsTask() {
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            List<List<String>> data;
            List<Integer> filteredRowsIndices;

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(this, filterController.filter);
                    data = new ArrayList<>();
                    filteredRowsIndices = sourceController.filteredRowsIndices();
                    for (int i = 0; i < dataController.tableData.size(); i++) {
                        if (!filteredRowsIndices.contains(i)) {
                            data.add(dataController.tableData.get(i));
                        }
                    }
                    data2D.stopFilter();
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                try {
                    dataController.updateTable(data);
                    dataController.tableChanged(true);
                    dataController.requestMouse();
                    dataController.alertInformation(message("DeletedRowsNumber") + ": " + filteredRowsIndices.size());
                    sourceController.selectedRowsIndices = null;
                    tabPane.getSelectionModel().select(sourceTab);
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                closeTask();
            }

        };
        start(task, false);
    }

    @Override
    public void handleAllTask() {
        if (task != null) {
            task.cancel();
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
                        count = data2D.clearData(this);
                    } else {
                        count = data2D.deleteRows(this, errorContinueCheck.isSelected());
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
                tabPane.getSelectionModel().select(sourceTab);
                sourceController.selectedRowsIndices = null;
                dataController.data2D.cloneData(data2D);
                dataController.dataSizeLoaded = false;
                dataController.goPage();
                dataController.requestMouse();
                dataController.alertInformation(message("DeletedRowsNumber") + ": " + count);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                closeTask();
            }

        };
        start(task, false);
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
