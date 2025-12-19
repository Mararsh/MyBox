package mara.mybox.controller;

import java.util.ArrayList;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-11-28
 * @License Apache License Version 2.0
 */
public class Data2DDeleteController extends BaseData2DTaskTargetsController {

    protected Data2DManufactureController editor;
    protected boolean clearAll;

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
    public boolean handleRows() {
        try {
            outputData = new ArrayList<>();
            for (int i = 0; i < sourceController.tableData.size(); i++) {
                if (!sourceController.filteredRowsIndices.contains(i)) {
                    outputData.add(sourceController.tableData.get(i));
                }
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            outputData = null;
            return false;
        }
    }

    @Override
    public void outputRows() {
        try {
            int count = sourceController.tableData.size() - outputData.size();
            sourceController.selectedRowsIndices = null;
            editor.updateTable(outputData);
            editor.tableChanged(true);
            editor.requestMouse();
            editor.alertInformation(message("DeletedRowsNumber") + ": " + count);
            tabPane.getSelectionModel().select(sourceTab);
        } catch (Exception e) {
            popError(message(e.toString()));
        }
    }

    @Override
    public void handleAllTask() {
        if (task != null) {
            task.cancel();
        }
        taskSuccessed = false;
        task = new FxSingletonTask<Void>(this) {

            private long count;
            private boolean needBackup = false;

            @Override
            protected boolean handle() {
                try {
                    needBackup = data2D.needBackup();
                    if (needBackup) {
                        addBackup(this, data2D.getFile());
                    }
                    data2D.startTask(this, filterController.filter);
                    if (clearAll) {
                        count = data2D.clearData(this);
                    } else {
                        count = data2D.deleteRows(this);
                    }
                    data2D.stopFilter();
                    taskSuccessed = count >= 0;
                    return taskSuccessed;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                sourceController.selectedRowsIndices = null;
                tabPane.getSelectionModel().select(sourceTab);
                editor.data2D.cloneDataFrom(data2D);
                editor.data2D.setTableChanged(false);
                editor.dataSizeLoaded = false;
                editor.goPage();
                editor.requestMouse();
                editor.alertInformation(message("DeletedRowsNumber") + ": " + count);
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                closeTask(ok);
            }

        };
        start(task, false);
    }

    /*
        static
     */
    public static Data2DDeleteController open(Data2DManufactureController tableController) {
        try {
            Data2DDeleteController controller = (Data2DDeleteController) WindowTools.referredStage(
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
