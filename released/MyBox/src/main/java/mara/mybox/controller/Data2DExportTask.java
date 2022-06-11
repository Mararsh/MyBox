package mara.mybox.controller;

import mara.mybox.dev.MyBoxLog;

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
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean checkOptions() {
        return exportController.checkOptions();
    }

    @Override
    public void beforeTask() {
        try {
            exportController.dataBox.setDisable(true);
            exportController.filterVBox.setDisable(true);
            exportController.formatVBox.setDisable(true);
            exportController.targetVBox.setDisable(true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean doTask() {
        return exportController.export();
    }

    @Override
    public void afterSuccess() {
        exportController.afterSuccess();
    }

    @Override
    public void afterTask() {
        try {
            exportController.dataBox.setDisable(false);
            exportController.filterVBox.setDisable(false);
            exportController.formatVBox.setDisable(false);
            exportController.targetVBox.setDisable(false);
            exportController.data2D.setTask(null);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void cancelTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        exportController.convertController.closeWriters();
    }

    @Override
    public void cancelAction() {
        cancelTask();
    }

}
