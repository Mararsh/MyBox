package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.operate.Data2DExport;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.SoundTools;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-12-8
 * @License Apache License Version 2.0
 */
public class Data2DExportController extends BaseData2DTaskController {

    protected Data2DExport export;
    protected String filePrefix;

    @FXML
    protected BaseData2DRowsColumnsController rowsColumnsController;
    @FXML
    protected VBox dataBox, filterVBox, formatVBox, targetVBox;
    @FXML
    protected ControlDataExport convertController;
    @FXML
    protected Tab targetTab;

    public Data2DExportController() {
        baseTitle = message("Export");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            sourceController = rowsColumnsController;
            formatValuesCheck = convertController.formatValuesCheck;
            rowNumberCheck = convertController.rowNumberCheck;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setParameters(BaseData2DLoadController controller) {
        try {
            super.setParameters(controller);

            convertController.setParameters(this);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        try {
            if (isSettingValues) {
                return true;
            }
            if (!super.checkOptions()) {
                return false;
            }
            targetPath = targetPathController.file();
            if (targetPath == null) {
                popError(message("InvalidParameters") + ": " + message("TargetPath"));
                tabPane.getSelectionModel().select(targetTab);
                return false;
            }
            filePrefix = data2D.getDataName();
            if (filePrefix == null || filePrefix.isBlank()) {
                filePrefix = DateTools.nowFileString();
            }
            export = convertController.pickParameters(data2D);
            export.setInvalidAs(invalidAs);
            export.setController(this);
            return export.setColumns(targetPathController, checkedColumns, filePrefix);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void beforeTask() {
        try {
            super.beforeTask();

            dataBox.setDisable(true);
            filterVBox.setDisable(true);
            formatVBox.setDisable(true);
            targetVBox.setDisable(true);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        taskSuccessed = false;
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(task, filterController.filter);
                    if (!isAllPages() || !data2D.isMutiplePages()) {
                        List<Integer> filteredRowsIndices = sourceController.filteredRowsIndices();
                        export.openWriters();
                        for (Integer row : filteredRowsIndices) {
                            List<String> dataRow = sourceController.tableData.get(row);
                            List<String> exportRow = new ArrayList<>();
                            for (Integer col : checkedColsIndices) {
                                exportRow.add(dataRow.get(col + 1));
                            }
                            export.writeRow(exportRow);
                        }
                        export.end();
                    } else {
                        export.setCols(checkedColsIndices).setTask(task).start();
                    }
                    taskSuccessed = !export.isFailed();
                    return taskSuccessed;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                afterSuccess();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                closeTask(ok);
            }

        };
        start(task, false);

    }

    @Override
    public void afterSuccess() {
        try {
            SoundTools.miao3();
            if (openCheck.isSelected()) {
                export.openResults();
            }
            if (targetPath != null && targetPath.exists()) {
                browseURI(targetPath.toURI());
                recordFileOpened(targetPath);
            } else {
                popInformation(message("NoFileGenerated"));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void afterTask(boolean ok) {
        try {
            if (export != null) {
                export.end();
                export = null;
            }
            data2D.stopTask();
            super.afterTask(ok);
            dataBox.setDisable(false);
            filterVBox.setDisable(false);
            formatVBox.setDisable(false);
            targetVBox.setDisable(false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        static
     */
    public static Data2DExportController open(BaseData2DLoadController tableController) {
        try {
            Data2DExportController controller = (Data2DExportController) WindowTools.branchStage(
                    tableController, Fxmls.Data2DExportFxml);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
