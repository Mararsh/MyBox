package mara.mybox.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mara.mybox.data.BaseTask;
import mara.mybox.data.QueryCondition;
import mara.mybox.data.StringTable;
import mara.mybox.data.TableData;
import mara.mybox.data.VisitHistory;
import mara.mybox.db.ColumnDefinition;
import mara.mybox.db.DerbyBase;
import static mara.mybox.db.DerbyBase.dbHome;
import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.fxml.FxmlControl;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.DateTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.VisitHistoryTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonFxValues;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @Author Mara
 * @CreateDate 2020-5-2
 * @License Apache License Version 2.0
 */
public class DataExportController extends DataQueryController {

    protected String currentSQL;
    protected int maxLines, taskCount;
    protected long startTime, dataSize;
    protected int logsMaxLines, logsTotalLines, logsCacheLines = 200;
    protected boolean cancelled, currentPage = false;
    protected List<String> columnNames;

    @FXML
    protected TabPane tabPane;
    @FXML
    protected Tab logsTab;
    @FXML
    protected CheckBox internalCheck, externalCheck, xmlCheck, jsonCheck, xlsxCheck, htmlCheck, verboseCheck;
    @FXML
    protected TextField targetNameInput, maxLinesinput;
    @FXML
    protected TextArea logsTextArea;
    @FXML
    protected ComboBox<String> maxLinesSelector;

    public DataExportController() {
        baseTitle = message("Export");

        SourceFileType = VisitHistory.FileType.Text;
        SourcePathType = VisitHistory.FileType.Text;
        TargetPathType = VisitHistory.FileType.Text;
        TargetFileType = VisitHistory.FileType.Text;
        AddFileType = VisitHistory.FileType.Text;
        AddPathType = VisitHistory.FileType.Text;

        targetPathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Text);
        sourcePathKey = VisitHistoryTools.getPathKey(VisitHistory.FileType.Text);

        sourceExtensionFilter = CommonFxValues.TextExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;
    }


    /*
        Methods need implementation
     */
    protected List<String> columnLabels() {
        if (columnNames == null) {
            columnNames = new ArrayList<>();
            List<ColumnDefinition> columns = dataController.tableDefinition.getColumns();
            for (ColumnDefinition column : columns) {
                columnNames.add(column.getLabel());
            }
        }
        return columnNames;
    }

    protected void writeInternalCSVHeader(CSVPrinter printer) {

    }

    protected void writeInternalCSV(Connection conn, CSVPrinter printer, ResultSet results) {

    }

    protected void writeExternalCSVHeader(CSVPrinter printer) {
        try {
            printer.printRecord(columnLabels());
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void writeExternalCSV(Connection conn, CSVPrinter printer, ResultSet results) {
        try {
            TableData data = (TableData) (dataController.tableDefinition.readData(results));
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    protected void writeXML(Connection conn, FileWriter writer, ResultSet results, String indent) {

    }

    protected String writeJSON(Connection conn, FileWriter writer, ResultSet results, String indent) {
        return null;
    }

    protected void writeExcel(Connection conn, XSSFSheet sheet, ResultSet results, int count) {
    }

    protected List<String> htmlRow(Connection conn, ResultSet results) {
        return null;
    }

    /*
        Common methods
     */
    @Override
    protected void setControls() {
        try {
            initExportOptions();

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    protected void initExportOptions() {
        try {
            if (titleInput != null && targetNameInput != null) {
                titleInput.textProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                            if (newv == null || newv.trim().isBlank()) {
                                return;
                            }
                            targetNameInput.setText(newv.trim().replaceAll("\\\"|\n|:", ""));
                        });
            }

            if (internalCheck != null) {
                internalCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) -> {
                            AppVariables.setUserConfigValue(baseName + "ExportInternal", newv);
                        });
                internalCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ExportInternal", true));
            }
            if (externalCheck != null) {
                externalCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) -> {
                            AppVariables.setUserConfigValue(baseName + "ExportExternal", newv);
                        });
                externalCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ExportExternal", true));
            }

            if (xmlCheck != null) {
                xmlCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) -> {
                            AppVariables.setUserConfigValue(baseName + "ExportXml", newv);
                        });
                xmlCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ExportXml", false));
            }

            if (jsonCheck != null) {
                jsonCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) -> {
                            AppVariables.setUserConfigValue(baseName + "ExportJson", newv);
                        });
                jsonCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ExportJson", false));
            }

            if (xlsxCheck != null) {
                xlsxCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) -> {
                            AppVariables.setUserConfigValue(baseName + "ExportXlsx", newv);
                        });
                xlsxCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ExportXlsx", false));
            }

            if (htmlCheck != null) {
                htmlCheck.selectedProperty().addListener(
                        (ObservableValue<? extends Boolean> ov, Boolean oldv, Boolean newv) -> {
                            AppVariables.setUserConfigValue(baseName + "ExportHtml", newv);
                        });
                htmlCheck.setSelected(AppVariables.getUserConfigBoolean(baseName + "ExportHtml", false));
            }

            if (maxLinesSelector != null) {
                maxLines = -1;
                maxLinesSelector.getItems().addAll(Arrays.asList(
                        message("NotSplit"), "1000", "500", "200", "300", "800", "2000", "3000", "5000", "8000"
                ));
                maxLinesSelector.getSelectionModel().selectedItemProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldValue, String newValue) -> {
                            if (newValue == null || newValue.isEmpty()) {
                                return;
                            }
                            AppVariables.setUserConfigValue(baseName + "ExportMaxLines", newValue);
                            if (message("NotSplit").equals(newValue)) {
                                maxLines = -1;
                                FxmlControl.setEditorNormal(maxLinesSelector);
                                return;
                            }
                            try {
                                int v = Integer.valueOf(newValue);
                                if (v > 0) {
                                    maxLines = v;
                                    FxmlControl.setEditorNormal(maxLinesSelector);
                                } else {
                                    FxmlControl.setEditorBadStyle(maxLinesSelector);
                                }
                            } catch (Exception e) {
                                FxmlControl.setEditorBadStyle(maxLinesSelector);
                            }
                        });
                maxLinesSelector.getSelectionModel().select(
                        AppVariables.getUserConfigValue(baseName + "ExportMaxLines", message("NotSplit")));

            }

            if (okButton != null && targetPathInput != null) {
                okButton.disableProperty().unbind();
                okButton.disableProperty().bind(
                        Bindings.isEmpty(targetPathInput.textProperty())
                                .or(targetPathInput.styleProperty().isEqualTo(badStyle))
                );

            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public void currentPage(DataAnalysisController dataController,
            QueryCondition initCondition, String tableDefinition,
            boolean prefixEditable, boolean supportTop) {
        setValue(dataController, initCondition, tableDefinition, prefixEditable, supportTop);
        currentPage = true;
        listView.setDisable(true);
        titleInput.setDisable(true);
        prefixInput.setDisable(true);
        whereInput.setDisable(true);
        orderInput.setDisable(true);
        fetchInput.setDisable(true);
        topInput.setDisable(true);
    }

    @FXML
    @Override
    public void createAction() {
        currentPage = false;
        qcid = -1;
        titleInput.clear();
        if (prefixInput.isEditable()) {
            prefixInput.clear();
        }
        whereInput.clear();
        orderInput.clear();
        fetchInput.clear();
        topInput.clear();
        listView.setDisable(false);
        titleInput.setDisable(false);
        whereInput.setDisable(false);
        orderInput.setDisable(false);
        fetchInput.setDisable(false);
        topInput.setDisable(false);
    }

    @Override
    public void okAction() {
        if (targetPath == null) {
            popError(message("InvalidTargetPath"));
            return;
        }
        if (targetNameInput.getText().trim().isBlank()) {
            popError(message("TargetPrefixEmpty"));
            return;
        }
        if (!currentPage) {
            savedCondition = save();
            if (savedCondition == null) {
                popError(message("InvalidParameters"));
                return;
            } else {
                loadList();
            }
        }
        Platform.runLater(() -> {
            if (!okButton.getText().equals(message("Cancel"))) {
                start();
                okButton.setText(message("Cancel"));
            } else {
                cancelled = true;
                if (task != null) {
                    task.cancel();
                }
                okButton.setText(buttonName());
            }
        });
    }

    public void start() {
        synchronized (this) {
            if (task != null) {
                return;
            }
            cancelled = false;
            synchronized (this) {
                taskCount = 0;
            }
            tabPane.getSelectionModel().select(logsTab);
            startTime = new Date().getTime();
            initLogs();
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        String filePrefix = targetPath.getAbsolutePath() + File.separator;
                        filePrefix += targetNameInput.getText().trim();
                        if (currentPage) {
                            currentSQL = dataController.pageQuerySQL;
                            dataSize = dataController.tableData.size();
                            writeFiles(filePrefix);
                            return true;
                        }
                        String where = savedCondition.getWhere();
                        String order = savedCondition.getOrder();
                        String fetch = savedCondition.getFetch();
                        if (fetch == null || fetch.isBlank()) {
                            String sizeSql = dataController.sizePrefix
                                    + (where == null || where.isBlank() ? "" : " WHERE " + where);
                            updateLogs(message("CountingDataSize") + ": " + sizeSql);
                            dataSize = DerbyBase.size(sizeSql);
                            updateLogs(message("DataSize") + ": " + dataSize);
                        } else {
                            dataSize = -1;
                        }
                        currentSQL = savedCondition.getPrefix() + " "
                                + (where == null || where.isBlank() ? "" : " WHERE " + where)
                                + (order == null || order.isBlank() ? "" : " ORDER BY " + order)
                                + (fetch == null || fetch.isBlank() ? "" : fetch);
                        if (cancelled) {
                            updateLogs(message("Cancelled"));
                            return false;
                        }
                        if (dataSize <= 0 || maxLines <= 0) {
                            writeFiles(filePrefix);
                            return true;
                        }
                        int offset = 0, index = 1;
                        String baseSQL = currentSQL;
                        while (true) {
                            if (isCancelled()) {
                                return false;
                            }
                            currentSQL = baseSQL + " OFFSET " + offset + " ROWS FETCH NEXT " + maxLines + " ROWS ONLY";
                            writeFiles(filePrefix + "_" + (index++));
                            offset += maxLines;
                            if (offset >= dataSize) {
                                break;
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        updateLogs(error);
                        return false;
//                        logger.debug(e.toString());
                    }
                }

                protected boolean writeFiles(String filePrefix) {
                    updateLogs(currentSQL);
                    if (internalCheck != null && internalCheck.isSelected()) {
                        writeInternalCSVTask(filePrefix, currentSQL);
                    }
                    if (externalCheck.isSelected()) {
                        writeExternalCSVTask(filePrefix, currentSQL);
                    }
                    if (xmlCheck.isSelected()) {
                        writeXMLTask(filePrefix, currentSQL);
                    }
                    if (jsonCheck.isSelected()) {
                        writeJSONTask(filePrefix, currentSQL);
                    }
                    if (xlsxCheck.isSelected()) {
                        writeExcelTask(filePrefix, currentSQL);
                    }
                    if (htmlCheck.isSelected()) {
                        writeHTMLTask(filePrefix, currentSQL);
                    }
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    browseURI(targetPath.toURI());
                }
            };
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    protected boolean writeInternalCSV(File file, String sql) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 CSVPrinter printer = new CSVPrinter(new FileWriter(file, Charset.forName("utf-8")), CSVFormat.DEFAULT)) {
            String filename = file.getAbsolutePath();
            conn.setReadOnly(true);
            writeInternalCSVHeader(printer);
            int count = 0;
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                while (results.next()) {
                    if (cancelled) {
                        updateLogs(message("Cancelled") + " " + filename);
                        return false;
                    }
                    writeInternalCSV(conn, printer, results);
                    count++;
                    if (verboseCheck.isSelected() && (count % 50 == 0)) {
                        updateLogs(message("Exported") + " " + count + ": " + filename);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            updateLogs(e.toString());
            return false;
        }
    }

    protected void writeInternalCSVTask(String filePrefix, String sql) {
        File file = new File(filePrefix + "_internal.csv");
        Platform.runLater(() -> {
            String filename = file.getAbsolutePath();
            updateLogs(message("Exporting") + " " + filename);
            synchronized (this) {
                ++taskCount;
            }
            BaseTask dTask = new BaseTask<Void>() {
                @Override
                protected boolean handle() {
                    return writeInternalCSV(file, sql);
                }

                @Override
                protected void whenSucceeded() {
                    if (!cancelled) {
                        updateLogs(message("Generated") + "." + file.getAbsolutePath());
                    }
                }

                @Override
                protected void finalAction() {
                    checkFinished();
                }
            };
            Thread thread = new Thread(dTask);
            thread.setDaemon(true);
            thread.start();
        });
    }

    protected boolean writeExternalCSV(File file, String sql) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 CSVPrinter printer = new CSVPrinter(new FileWriter(file, Charset.forName("utf-8")), CSVFormat.DEFAULT)) {
            String filename = file.getAbsolutePath();
            conn.setReadOnly(true);
            writeExternalCSVHeader(printer);
            int count = 0;
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                while (results.next()) {
                    if (cancelled) {
                        updateLogs(message("Cancelled") + " " + filename);
                        return false;
                    }
                    writeExternalCSV(conn, printer, results);
                    count++;
                    if (verboseCheck.isSelected() && (count % 50 == 0)) {
                        updateLogs(message("Exported") + " " + count + ": " + filename);
                    }
                }
            }
            return true;
        } catch (Exception e) {
            updateLogs(e.toString());
            return false;
        }
    }

    protected void writeExternalCSVTask(String filePrefix, String sql) {
        File file = new File(filePrefix + ".csv");
        Platform.runLater(() -> {
            synchronized (this) {
                ++taskCount;
            }
            String filename = file.getAbsolutePath();
            updateLogs(message("Exporting") + " " + filename);
            BaseTask dTask = new BaseTask<Void>() {
                @Override
                protected boolean handle() {
                    return writeExternalCSV(file, sql);
                }

                @Override
                protected void whenSucceeded() {
                    if (!cancelled) {
                        updateLogs(message("Generated") + "." + file.getAbsolutePath());
                    }
                }

                @Override
                protected void finalAction() {
                    checkFinished();
                }

            };
            Thread thread = new Thread(dTask);
            thread.setDaemon(true);
            thread.start();
        });
    }

    protected boolean writeXML(File file, String sql) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 FileWriter writer = new FileWriter(file, Charset.forName("utf-8"))) {
            String filename = file.getAbsolutePath();
            conn.setReadOnly(true);
            StringBuilder s = new StringBuilder();
            String indent = "    ";
            s.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                    .append("<").append(baseName).append("s>\n");
            writer.write(s.toString());
            int count = 0;
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                while (results.next()) {
                    if (cancelled) {
                        updateLogs(message("Cancelled") + " " + filename);
                        return false;
                    }
                    writeXML(conn, writer, results, indent);
                    count++;
                    if (verboseCheck.isSelected() && (count % 50 == 0)) {
                        updateLogs(message("Exported") + " " + count + ": " + filename);
                    }
                }
            }
            writer.write("</" + baseName + "s>\n");
            return true;
        } catch (Exception e) {
            updateLogs(e.toString());
            return false;
        }
    }

    protected void writeXMLTask(String filePrefix, String sql) {
        File file = new File(filePrefix + ".xml");
        Platform.runLater(() -> {
            synchronized (this) {
                ++taskCount;
            }
            String filename = file.getAbsolutePath();
            updateLogs(message("Exporting") + " " + filename);
            BaseTask dTask = new BaseTask<Void>() {
                @Override
                protected boolean handle() {
                    return writeXML(file, sql);
                }

                @Override
                protected void whenSucceeded() {
                    if (!cancelled) {
                        updateLogs(message("Generated") + "." + file.getAbsolutePath());
                    }
                }

                @Override
                protected void finalAction() {
                    checkFinished();
                }
            };
            Thread thread = new Thread(dTask);
            thread.setDaemon(true);
            thread.start();
        });
    }

    protected boolean writeJSON(File file, String sql) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login);
                 FileWriter writer = new FileWriter(file, Charset.forName("utf-8"))) {
            String filename = file.getAbsolutePath();
            conn.setReadOnly(true);
            StringBuilder s = new StringBuilder();
            String indent = "    ";
            s.append("{\"").append(baseName).append("s\": [\n");
            writer.write(s.toString());
            int count = 0;
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                while (results.next()) {
                    if (cancelled) {
                        updateLogs(message("Cancelled") + " " + filename);
                        return false;
                    }
                    s = new StringBuilder();
                    if (count > 0) {
                        s.append(indent).append("},\n");
                    }
                    s.append(writeJSON(conn, writer, results, indent));
                    writer.write(s.toString());
                    count++;
                    if (verboseCheck.isSelected() && (count % 50 == 0)) {
                        updateLogs(message("Exported") + " " + count + ": " + filename);
                    }
                }
            }
            if (count > 0) {
                writer.write(indent + "}\n");
            }
            writer.write("]}\n");
            return true;
        } catch (Exception e) {
            updateLogs(e.toString());
            return false;
        }
    }

    protected void writeJSONTask(String filePrefix, String sql) {
        File file = new File(filePrefix + ".json");
        Platform.runLater(() -> {
            synchronized (this) {
                ++taskCount;
            }
            String filename = file.getAbsolutePath();
            updateLogs(message("Exporting") + " " + filename);
            BaseTask dTask = new BaseTask<Void>() {
                @Override
                protected boolean handle() {
                    return writeJSON(file, sql);
                }

                @Override
                protected void whenSucceeded() {
                    if (!cancelled) {
                        updateLogs(message("Generated") + "." + file.getAbsolutePath());
                    }
                }

                @Override
                protected void finalAction() {
                    checkFinished();
                }
            };
            Thread thread = new Thread(dTask);
            thread.setDaemon(true);
            thread.start();
        });
    }

    protected boolean writeExcel(File file, String sql) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            String filename = file.getAbsolutePath();
            conn.setReadOnly(true);
            XSSFWorkbook wb = new XSSFWorkbook();
            XSSFSheet sheet = wb.createSheet("sheet1");
            List<String> columns = columnLabels();
            XSSFRow titleRow = sheet.createRow(0);
            XSSFCellStyle horizontalCenter = wb.createCellStyle();
            horizontalCenter.setAlignment(HorizontalAlignment.CENTER);
            for (int i = 0; i < columns.size(); i++) {
                XSSFCell cell = titleRow.createCell(i);
                cell.setCellValue(columns.get(i));
                cell.setCellStyle(horizontalCenter);
            }
            int count = 0;
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                while (results.next()) {
                    if (cancelled) {
                        updateLogs(message("Cancelled") + " " + filename);
                        return false;
                    }
                    writeExcel(conn, sheet, results, count);
                    count++;
                    if (verboseCheck.isSelected() && (count % 50 == 0)) {
                        updateLogs(message("Exported") + " " + count + ": " + filename);
                    }
                }
            }
            for (int i = 0; i < columns.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            try ( OutputStream fileOut = new FileOutputStream(file)) {
                wb.write(fileOut);
            }
            return true;
        } catch (Exception e) {
            updateLogs(e.toString());
            return false;
        }
    }

    protected void writeExcelTask(String filePrefix, String sql) {
        File file = new File(filePrefix + ".xlsx");
        Platform.runLater(() -> {
            synchronized (this) {
                ++taskCount;
            }
            String filename = file.getAbsolutePath();
            updateLogs(message("Exporting") + " " + filename);
            BaseTask dTask = new BaseTask<Void>() {
                @Override
                protected boolean handle() {
                    return writeExcel(file, sql);
                }

                @Override
                protected void whenSucceeded() {
                    if (!cancelled) {
                        updateLogs(message("Generated") + "." + file.getAbsolutePath());
                    }
                }

                @Override
                protected void finalAction() {
                    checkFinished();
                }

            };
            Thread thread = new Thread(dTask);
            thread.setDaemon(true);
            thread.start();
        });
    }

    protected boolean writeHTML(File file, String sql) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            String filename = file.getAbsolutePath();
            conn.setReadOnly(true);
            List<String> names = columnLabels();
            StringTable table = new StringTable(names, titleInput.getText() + "<br>" + sql);
            int count = 0;
            try ( ResultSet results = conn.createStatement().executeQuery(sql)) {
                while (results.next()) {
                    if (cancelled) {
                        updateLogs(message("Cancelled") + " " + filename);
                        return false;
                    }
                    List<String> row = htmlRow(conn, results);
                    table.add(row);
                    count++;
                    if (verboseCheck.isSelected() && (count % 50 == 0)) {
                        updateLogs(message("Exported") + " " + count + ": " + filename);
                    }
                }
            }
            FileTools.writeFile(file, StringTable.tableHtml(table));
            return true;
        } catch (Exception e) {
            updateLogs(e.toString());
            return false;
        }
    }

    protected void writeHTMLTask(String filePrefix, String sql) {
        File file = new File(filePrefix + ".htm");
        Platform.runLater(() -> {
            synchronized (this) {
                ++taskCount;
            }
            String filename = file.getAbsolutePath();
            updateLogs(message("Exporting") + " " + filename);
            BaseTask dTask = new BaseTask<Void>() {
                @Override
                protected boolean handle() {
                    return writeHTML(file, sql);
                }

                @Override
                protected void whenSucceeded() {
                    if (!cancelled) {
                        updateLogs(message("Generated") + "." + file.getAbsolutePath());
                    }
                }

                @Override
                protected void finalAction() {
                    checkFinished();
                }
            };
            Thread thread = new Thread(dTask);
            thread.setDaemon(true);
            thread.start();
        });
    }

    protected void checkFinished() {
        synchronized (this) {
            --taskCount;
            if (taskCount <= 0) {
                if (!cancelled) {
                    updateLogs(message("MissionCompleted"));
                }
                okButton.setText(buttonName());
            }
        }
    }

    protected void initLogs() {
        logsTextArea.setText("");
        logsTotalLines = 0;
        if (maxLinesinput != null) {
            try {
                logsMaxLines = Integer.parseInt(maxLinesinput.getText());
            } catch (Exception e) {
                logsMaxLines = 5000;
            }
        }
    }

    protected void updateLogs(final String line) {
        try {
            if (logsTextArea == null) {
                return;
            }
            Platform.runLater(() -> {
                String s = DateTools.datetimeToString(new Date()) + "  " + line + "\n";
                logsTextArea.appendText(s);
                logsTotalLines++;
                if (logsTotalLines > logsMaxLines + logsCacheLines) {
                    logsTextArea.deleteText(0, 1);
                }
            });
        } catch (Exception e) {
            logger.debug(e.toString());
        }
    }

    @FXML
    protected void clearLogs() {
        logsTextArea.setText("");
    }

    @Override
    public boolean leavingScene() {
        cancelled = true;
        return super.leavingScene();
    }

}
