package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.CSV;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.DatabaseTable;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Excel;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.HTML;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.PDF;
import static mara.mybox.data2d.Data2D_Attributes.TargetType.Text;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.data2d.TmpTable;
import mara.mybox.data2d.reader.Data2DExport;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.InvalidAs;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2022-4-5
 * @License Apache License Version 2.0
 */
public class Data2DSaveAsController extends BaseDataConvertController {

    protected BaseData2DLoadController tableController;
    protected Data2DExport export;
    protected Data2D data2D;
    protected TargetType format;
    protected String targetName;
    protected File targetFile;
    protected List<List<String>> dataRows;
    protected List<Data2DColumn> dataColumns;

    protected boolean forData;
    protected InvalidAs invalidAs = InvalidAs.Blank;
    protected DataTable dataTable;

    @FXML
    protected ControlData2DTarget targetController;
    @FXML
    protected ControlNewDataTable dbController;
    @FXML
    protected CheckBox currentSheetOnlyCheck;
    @FXML
    protected Tab csvTab, excelTab, textTab, htmlTab, pdfTab, dbTab;
    @FXML
    protected RadioButton zeroNonnumericRadio, blankNonnumericRadio;

    public Data2DSaveAsController() {
        baseTitle = message("SaveAs");
        forData = false;
    }

    public void setParameters(BaseData2DLoadController controller) {
        try {
            if (controller == null || controller.getData2D() == null) {
                close();
                return;
            }
            tableController = controller;
            data2D = tableController.data2D;
            if (data2D == null || !data2D.isValid()) {
                close();
                return;
            }

            initControls(baseName);

            targetController.setParameters(this, tableController);
            targetController.formatNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue v, Boolean ov, Boolean nv) {
                    formatChanged();
                }
            });
            format = targetController.target;

            checkTargets();
            dbController.setParameters(this, data2D);
            dbController.setColumns(data2D.columnIndices());
            dbController.nameInput.setText(targetName);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(DataFileCSV csvData, TargetType inFormat,
            String inTargetName, File inTargetFile) {
        try {
            if (csvData == null || !csvData.isValid() || inFormat == null) {
                close();
                return;
            }
            forData = true;
            data2D = csvData;
            format = inFormat;
            targetName = inTargetName;
            targetFile = inTargetFile;
            formatChanged();
            checkTargets();
            if (format != TargetType.DatabaseTable) {
                export = Data2DExport.create(data2D);
                export.initParameters(format);
                export.setDataName(targetName);
                export.setTargetFile(targetFile);
                export.setColumns(data2D.getColumns());
                export.setNames(data2D.columnNames());
                export.setSkip(targetPathController.isSkip());
            }
            startAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setParameters(TargetType inFormat,
            List<Data2DColumn> outputColumns, List<List<String>> outputData, String inTargetName) {
        try {
            if (outputColumns == null || outputColumns.isEmpty()
                    || outputData == null || outputData.isEmpty()
                    || inFormat == null) {
                close();
                return;
            }
            forData = true;
            format = inFormat;
            targetName = inTargetName;
            dataColumns = outputColumns;
            dataRows = outputData;
            formatChanged();
            checkTargets();
            if (format != TargetType.DatabaseTable) {
                export = new Data2DExport();
                export.initParameters(format);
                export.setDataName(targetName);
                export.setTargetFile(targetFile);
                export.setColumns(dataColumns);
                List<String> names = new ArrayList<>();
                for (Data2DColumn c : dataColumns) {
                    names.add(c.getColumnName());
                }
                export.setNames(names);
                export.setSkip(targetPathController.isSkip());
            }
            startAction();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void formatChanged() {
        try {
            if (tableController == null || !tableController.isShowing()) {
                close();
                return;
            }
            data2D = tableController.data2D;
            if (data2D == null || !data2D.isValid()) {
                close();
                return;
            }
            tabPane.getTabs().removeAll(csvTab, excelTab, textTab, htmlTab, pdfTab, dbTab);
            format = targetController.target;
            if (format == null) {
                return;
            }
            switch (format) {
                case CSV:
                    tabPane.getTabs().add(1, csvTab);
                    break;
                case Excel:
                    tabPane.getTabs().add(1, excelTab);
                    currentSheetOnlyCheck.setVisible(data2D.isExcel());
                    break;
                case Text:
                    tabPane.getTabs().add(1, textTab);
                    break;
                case HTML:
                    tabPane.getTabs().add(1, htmlTab);
                    break;
                case PDF:
                    tabPane.getTabs().add(1, pdfTab);
                    break;
                case DatabaseTable:
                    tabPane.getTabs().add(1, dbTab);
                    break;
                default:
                    break;
            }
            refreshStyle(tabPane);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkOptions() {
        if (forData) {
            return true;
        } else {
            return pickParameters();
        }
    }

    public void checkTargets() {
        if (targetName == null || targetName.isBlank()) {
            targetName = data2D.dataName();
        }
        if (targetName == null || targetName.isBlank()) {
            targetName = "Data2D";
        } else if (targetName.startsWith(TmpTable.TmpTablePrefix)
                || targetName.startsWith(TmpTable.TmpTablePrefix.toLowerCase())) {
            targetName = targetName.substring(TmpTable.TmpTablePrefix.length());
        }
        if (targetFile == null) {
            targetFile = Data2D.targetFile(targetName, format);
        }
    }

    public boolean pickParameters() {
        try {
            if (tableController == null || !tableController.isShowing()) {
                close();
                return false;
            }
            data2D = tableController.data2D;
            if (data2D == null || !data2D.isValid()) {
                close();
                return false;
            }
            if (!targetController.validateTarget()) {
                return false;
            }
            format = targetController.target;
            if (format == TargetType.DatabaseTable) {
                return pickDB();
            }
            export = Data2DExport.create(data2D);
            export.initParameters(format);
            switch (format) {
                case CSV:
                    if (!pickCSV(export)) {
                        return false;
                    }
                    break;
                case Excel:
                    if (!pickExcel(export)) {
                        return false;
                    }
                    break;
                case Text:
                    if (!pickText(export)) {
                        return false;
                    }
                    break;
                case HTML:
                    if (!pickHtml(export)) {
                        return false;
                    }
                    break;
                case PDF:
                    if (!pickPDF(export)) {
                        return false;
                    }
                    break;
            }

            export.setDataName(targetController.name());
            export.setTargetFile(targetController.file());
            export.setColumns(data2D.getColumns());
            export.setNames(data2D.columnNames());
            export.setSkip(targetPathController.isSkip());
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public boolean pickDB() {
        try (Connection conn = DerbyBase.getConnection()) {
            boolean ok = dbController.checkOptions(conn, false);
            if (!ok) {
                tabPane.getSelectionModel().select(dbTab);
            }
            return ok;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public void beforeTask() {
        super.beforeTask();
        if (zeroNonnumericRadio.isSelected()) {
            invalidAs = InvalidAs.Zero;
        } else {
            invalidAs = InvalidAs.Blank;
        }
        tabPane.getSelectionModel().select(logsTab);
    }

    @Override
    public boolean doTask(FxTask currentTask) {
        try {
            if (format == TargetType.DatabaseTable) {
                return createTable(currentTask);
            } else {
                if (dataRows != null) {
                    for (List<String> row : dataRows) {
                        if (currentTask == null || !currentTask.isWorking()) {
                            break;
                        }
                        export.writeRow(row);
                    }
                    export.closeWriters();
                    return true;
                } else {
                    data2D.startTask(currentTask, null);
                    data2D.stopTask();
                    return data2D.export(export, data2D.columnIndices());
                }
            }
        } catch (Exception e) {
            error = e.toString();
            return false;
        }
    }

    public boolean createTable(FxTask currentTask) {
        dataTable = null;
        if (forData) {
            if (dataRows != null) {
                dataTable = Data2D.createTable(currentTask,
                        dataColumns, dataRows, targetName, invalidAs);
            } else {
                dataTable = data2D.toTable(currentTask, targetName);
            }
        } else {
            try (Connection conn = DerbyBase.getConnection()) {
                if (!dbController.createTable(currentTask, conn)) {
                    return false;
                }
                if (data2D.isMutiplePages()) {
                    dbController.importAllData(currentTask, conn, invalidAs);
                } else {
                    dbController.importData(conn, null, invalidAs);
                }
                dataTable = dbController.dataTable;
            } catch (Exception e) {
                updateLogs(e.toString());
            }
        }
        return dataTable != null;
    }

    @Override
    public void afterSuccess() {
        if (format == TargetType.DatabaseTable) {
            if (dataTable != null) {
                Data2DManufactureController.openDef(dataTable);
            }
        } else if (export != null) {
            export.openResults(this);
        }
    }

    @Override
    public void afterTask() {
        if (data2D != null) {
            data2D.stopTask();
        }
        export = null;
    }

    /*
        static
     */
    public static Data2DSaveAsController open() {
        try {
            Data2DSaveAsController controller
                    = (Data2DSaveAsController) WindowTools.openStage(Fxmls.Data2DSaveAsFxml);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static Data2DSaveAsController open(BaseData2DLoadController tableController) {
        try {
            Data2DSaveAsController controller = open();
            controller.setParameters(tableController);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static Data2DSaveAsController createData(DataFileCSV sourceData,
            TargetType format, String targetName, File targetFile) {
        try {
            Data2DSaveAsController controller = open();
            controller.setParameters(sourceData, format, targetName, targetFile);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static Data2DSaveAsController createData(TargetType format,
            List<Data2DColumn> outputColumns, List<List<String>> outputData, String targetName) {
        try {
            Data2DSaveAsController controller = open();
            controller.setParameters(format, outputColumns, outputData, targetName);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
