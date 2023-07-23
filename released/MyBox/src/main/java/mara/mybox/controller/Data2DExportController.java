package mara.mybox.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
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
public class Data2DExportController extends BaseData2DHandleController {

    protected String filePrefix;

    @FXML
    protected VBox filterVBox, formatVBox, targetVBox;
    @FXML
    protected ControlDataConvert convertController;
    @FXML
    protected CheckBox openCheck;
    @FXML
    protected Tab targetTab, logsTab;
    @FXML
    protected Data2DExportTask taskController;

    public Data2DExportController() {
        baseTitle = message("Export");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            notSelectColumnsInTable(false);

            okButton = startButton;

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setParameters(ControlData2DLoad editController) {
        try {
            convertController.setControls(taskController);
            taskController.setParameters(this);

            super.setParameters(editController);

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
                outOptionsError(message("InvalidParameters") + ": " + message("TargetPath"));
                tabPane.getSelectionModel().select(targetTab);
                return false;
            }
            filePrefix = data2D.getDataName();
            if (filePrefix == null || filePrefix.isBlank()) {
                filePrefix = DateTools.nowFileString();
            }
            return convertController.initParameters();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @FXML
    @Override
    public void startAction() {
        okAction();
    }

    @Override
    protected void startOperation() {
        taskController.startAction();
    }

    public boolean export() {
        try {
            convertController.setExport(targetPath, checkedColumns, filePrefix, targetPathController.isSkip());
            data2D.startTask(taskController.task, filterController.filter);
            if (!isAllPages() || !data2D.isMutiplePages()) {
                filteredRowsIndices = filteredRowsIndices();
                for (Integer row : filteredRowsIndices) {
                    List<String> dataRow = tableData.get(row);
                    List<String> exportRow = new ArrayList<>();
                    for (Integer col : checkedColsIndices) {
                        exportRow.add(dataRow.get(col + 1));
                    }
                    convertController.writeRow(exportRow);
                }

            } else {
                data2D.export(convertController, checkedColsIndices);
            }
            data2D.stopTask();

            convertController.closeWriters();
            return true;
        } catch (Exception e) {
            if (taskController.task != null) {
                taskController.task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
    }

    public void afterSuccess() {
        try {
            SoundTools.miao3();
            if (openCheck.isSelected()) {
                new Timer().schedule(new TimerTask() {

                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            convertController.openFiles();
                        });
                    }

                }, 1000);

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

    /*
        static
     */
    public static Data2DExportController open(ControlData2DLoad tableController) {
        try {
            Data2DExportController controller = (Data2DExportController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DExportFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
