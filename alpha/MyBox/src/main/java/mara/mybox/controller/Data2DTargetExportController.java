package mara.mybox.controller;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-11-23
 * @License Apache License Version 2.0
 */
public class Data2DTargetExportController extends BaseTaskController {

    protected DataFileCSV csvFile;
    protected List<List<String>> dataRows;
    protected List<Data2DColumn> columns;
    protected String filePrefix, format;

    @FXML
    protected VBox formatVBox, targetVBox;
    @FXML
    protected ControlDataConvert convertController;
    @FXML
    protected Tab targetTab;

    public Data2DTargetExportController() {
        baseTitle = message("Export");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();
            formatVBox.setDisable(true);
            targetVBox.setDisable(true);
            okButton = startButton;

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(DataFileCSV data, String format) {
        try {
            if (data == null || !data.isValid()
                    || data.getFile() == null || !data.getFile().exists()) {
                close();
                return;
            }
            csvFile = data;
            columns = csvFile.getColumns();
            dataRows = null;
            this.format = format;
            targetPath = new File(FileTmpTools.generatePath(format));
            filePrefix = data.getDataName();

            startAction();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void setParameters(List<Data2DColumn> outputColumns, List<List<String>> outputData,
            String format, String name) {
        try {
            if (outputColumns == null || outputColumns.isEmpty()
                    || outputData == null || outputData.isEmpty()
                    || format == null || format.isBlank()) {
                close();
                return;
            }
            csvFile = null;
            columns = outputColumns;
            dataRows = outputData;
            this.format = format;
            targetPath = new File(FileTmpTools.generatePath("data2d"));
            filePrefix = name;

            startAction();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void beforeTask() {
        super.beforeTask();
        if (filePrefix == null || filePrefix.isBlank()) {
            filePrefix = DateTools.nowFileString();
        }
        tabPane.getSelectionModel().select(logsTab);
        convertController.setControls(this, format);
        convertController.initParameters();
    }

    @Override
    public boolean doTask() {
        try {
            convertController.setExport(targetPath, columns, filePrefix, targetPathController.isSkip());
            if (csvFile != null) {
                csvFile.startTask(task, null);
                csvFile.export(convertController, csvFile.columnIndices());
                csvFile.stopTask();
            } else {
                for (List<String> row : dataRows) {
                    convertController.writeRow(row);
                }
            }
            convertController.closeWriters();
            return true;
        } catch (Exception e) {
            this.updateLogs(e.toString());
            return false;
        }
    }

    @Override
    public void afterSuccess() {
        try {
            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {
                    Platform.runLater(() -> {
                        convertController.openFiles();
                        close();
                    });
                }

            }, 1000);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void afterTask() {
        super.afterTask();
        if (csvFile != null) {
            csvFile.stopTask();
        }
    }

    /*
        static
     */
    public static Data2DTargetExportController open(DataFileCSV data, String format) {
        try {
            Data2DTargetExportController controller = (Data2DTargetExportController) WindowTools.openStage(Fxmls.Data2DTargetExportFxml);
            controller.setParameters(data, format);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static Data2DTargetExportController open(List<Data2DColumn> outputColumns,
            List<List<String>> outputData, String format, String name) {
        try {
            Data2DTargetExportController controller = (Data2DTargetExportController) WindowTools.openStage(Fxmls.Data2DTargetExportFxml);
            controller.setParameters(outputColumns, outputData, format, name);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
