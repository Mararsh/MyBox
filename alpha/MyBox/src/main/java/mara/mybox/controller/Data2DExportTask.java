package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;

/**
 * @Author Mara
 * @CreateDate 2021-12-8
 * @License Apache License Version 2.0
 */
public class Data2DExportTask extends BaseTaskController {

    protected Data2DExportController exportController;

    public void setParameters(Data2DExportController exportController) {
        try {
            this.exportController = exportController;
            startButton = exportController.startButton;
            tabPane = exportController.tabPane;
            logsTab = exportController.logsTab;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void beforeTask() {
        try {
            super.beforeTask();

            exportController.dataBox.setDisable(true);
            exportController.filterVBox.setDisable(true);
            exportController.formatVBox.setDisable(true);
            exportController.targetVBox.setDisable(true);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        return exportController.export();
    }

    @Override
    public void afterSuccess() {
        exportController.afterSuccess();
    }

    @Override
    public void afterTask() {
        try {
            super.afterTask();
            exportController.dataBox.setDisable(false);
            exportController.filterVBox.setDisable(false);
            exportController.formatVBox.setDisable(false);
            exportController.targetVBox.setDisable(false);
            exportController.data2D.stopTask();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void cancelTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        if (exportController.export != null) {
            exportController.export.end();
            exportController.export = null;
        }
    }

    @Override
    public void cancelAction() {
        cancelTask();
    }

}
