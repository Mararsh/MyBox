package mara.mybox.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.BaseData;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.EpidemicReport;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.QueryCondition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.DataFactory;
import mara.mybox.db.table.TableEpidemicReport;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.fxml.StyleTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-5-2
 * @License Apache License Version 2.0
 */
public class DataExportController extends BaseTaskController {

    protected BaseDataManageController dataController;
    protected BaseTable table;
    protected String currentSQL;
    protected long startTime, dataSize;
    protected boolean currentPage = false, epidemicReportTop;
    protected List<ColumnDefinition> columns;
    protected int top;

    @FXML
    protected Tab fieldsTab, targetTab, formatsTab;
    @FXML
    protected ControlDataQuery queryController;
    @FXML
    protected ControlDataConvert convertController;
    @FXML
    protected ControlPdfWriteOptions pdfOptionsController;
    @FXML
    protected FlowPane fieldsPane;
    @FXML
    protected TextField targetNameInput;

    public DataExportController() {
        baseTitle = Languages.message("Export");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    public void setValues(BaseDataManageController dataController,
            QueryCondition initCondition, String tableDefinition,
            boolean prefixEditable, boolean supportTop) {
        if (dataController == null || initCondition == null) {
            return;
        }
        try {
            this.baseName = dataController.baseName;
            this.baseTitle = dataController.baseTitle + " " + baseTitle;
            getMyStage().setTitle(dataController.baseTitle + " " + baseTitle);

            this.dataController = dataController;
            table = dataController.tableDefinition;

            if (queryController.titleInput != null && targetNameInput != null) {
                queryController.titleInput.textProperty().addListener(
                        (ObservableValue<? extends String> ov, String oldv, String newv) -> {
                            if (newv == null || newv.trim().isBlank()) {
                                return;
                            }
                            targetNameInput.setText(newv.trim().replaceAll("\\\"|\n|:", ""));
                        });
            }
            queryController.setControls(dataController, initCondition, tableDefinition, prefixEditable, supportTop);

            convertController.setControls(this, pdfOptionsController);
            pdfOptionsController.pixSizeRadio.setDisable(true);
            pdfOptionsController.standardSizeRadio.fire();

            fieldsPane.getChildren().clear();
            List<ColumnDefinition> tableColumns = table.getColumns();
            for (ColumnDefinition column : tableColumns) {
                CheckBox cb = new CheckBox(column.getLabel());
                cb.setUserData(column);
                cb.setSelected(UserConfig.getBoolean(baseName + column.getName(), false));
                cb.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + column.getName(), cb.isSelected());
                });
                fieldsPane.getChildren().add(cb);
            }

            StyleTools.setNameIcon(startButton, Languages.message("Start"), "iconStart.png");
            startButton.applyCss();
            startButton.setUserData(null);
            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(targetPathInput.textProperty())
                            .or(targetPathInput.styleProperty().isEqualTo(UserConfig.badStyle()))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void currentPage(BaseDataManageController dataController,
            QueryCondition initCondition, String tableDefinition,
            boolean prefixEditable, boolean supportTop) {
        setValues(dataController, initCondition, tableDefinition, prefixEditable, supportTop);
        currentPage = true;
//        queryController.thisPane.setDisable(true);
    }

    @FXML
    @Override
    public void startAction() {
        queryController.savedCondition = queryController.save();
        if (queryController.savedCondition == null) {
            return;
        }
        if (!checkSettings()) {
            return;
        }
        top = queryController.savedCondition.getTop();
        epidemicReportTop = false;
        Platform.runLater(() -> {
            if (startButton.getUserData() == null) {
                if (table instanceof TableEpidemicReport && top > 0) {
                    epidemicReportTop = true;
                    if (!validTopOrder()) {
                        alertError(Languages.message("TimeAsOrderWhenSetTop"));
                        return;
                    }
                }
                start();
                StyleTools.setNameIcon(startButton, Languages.message("Stop"), "iconStop.png");
                startButton.applyCss();
                startButton.setUserData("stop");
            } else {
                cancelled = true;
                if (task != null) {
                    task.cancel();
                    task = null;
                }
                StyleTools.setNameIcon(startButton, Languages.message("Start"), "iconStart.png");
                startButton.applyCss();
                startButton.setUserData(null);
            }
        });
    }

    public void start() {
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            cancelled = false;
            tabPane.getSelectionModel().select(logsTab);
            startTime = new Date().getTime();
            initLogs();
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    if (epidemicReportTop) {
                        return handleEpidemicReportTop();
                    } else {
                        return commonHandle();
                    }
                }

                private boolean commonHandle() {
                    try {
                        String filePrefix = targetNameInput.getText().trim();
                        if (currentPage) {
                            currentSQL = dataController.pageQuerySQL;
                            dataSize = dataController.tableData.size();
                            writeFiles(filePrefix);
                            return true;
                        }
                        String where = queryController.savedCondition.getWhere();
                        String order = queryController.savedCondition.getOrder();
                        String fetch = queryController.savedCondition.getFetch();
                        if (fetch == null || fetch.isBlank()) {
                            String sizeSql = dataController.sizePrefix
                                    + (where == null || where.isBlank() ? "" : " WHERE " + where);
                            updateLogs(Languages.message("CountingDataSize") + ": " + sizeSql);
                            dataSize = DerbyBase.size(sizeSql);
                            updateLogs(Languages.message("DataSize") + ": " + dataSize);
                        } else {
                            dataSize = -1;
                        }
                        currentSQL = queryController.savedCondition.getPrefix() + " "
                                + (where == null || where.isBlank() ? "" : " WHERE " + where)
                                + (order == null || order.isBlank() ? "" : " ORDER BY " + order)
                                + (fetch == null || fetch.isBlank() ? "" : " " + fetch);
                        if (cancelled) {
                            updateLogs(Languages.message("Cancelled"));
                            return false;
                        }
                        if (dataSize <= 0 || convertController.maxLines <= 0) {
                            writeFiles(filePrefix);
                            return true;
                        }
                        int offset = 0, index = 1;
                        String baseSQL = currentSQL;
                        while (true) {
                            if (task == null || isCancelled()) {
                                return false;
                            }
                            currentSQL = baseSQL + " OFFSET " + offset + " ROWS FETCH NEXT " + convertController.maxLines + " ROWS ONLY";
                            writeFiles(filePrefix + "_" + (index++));
                            offset += convertController.maxLines;
                            if (offset >= dataSize) {
                                break;
                            }
                        }
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        updateLogs(error);
                        return false;
                    }
                }

                private boolean writeFiles(String filePrefix) {
                    updateLogs(currentSQL);
                    try ( Connection conn = DerbyBase.getConnection()) {
                        conn.setReadOnly(true);
                        int count = 0;
                        if (!convertController.openWriters(filePrefix)) {
                            return false;
                        }
                        try ( ResultSet results = conn.createStatement().executeQuery(currentSQL)) {
                            while (results.next()) {
                                if (cancelled) {
                                    updateLogs(Languages.message("Cancelled") + " " + filePrefix);
                                    return false;
                                }
                                BaseData data;
                                if (table instanceof TableGeographyCode) {
                                    GeographyCode code = TableGeographyCode.readResults(results);
                                    TableGeographyCode.decodeAncestors(conn, code);
                                    data = code;
                                } else if (table instanceof TableEpidemicReport) {
                                    data = TableEpidemicReport.statisticViewQuery(conn, results, true);
                                } else {
                                    data = (BaseData) (table.readData(results));
                                }
                                writeRow(data);
                                count++;
                                if (verboseCheck.isSelected() && (count % 50 == 0)) {
                                    updateLogs(Languages.message("Exported") + " " + count + ": " + filePrefix);
                                }
                            }
                        }
                        convertController.closeWriters();
                    } catch (Exception e) {
                        updateLogs(e.toString());
                        return false;
                    }
                    return true;
                }

                private boolean writeRow(BaseData data) {
                    try {
                        if (data == null) {
                            return false;
                        }
                        List<String> row = new ArrayList<>();
                        for (ColumnDefinition column : columns) {
                            Object value = DataFactory.getColumnValue(data, column.getName());
                            String display = DataFactory.displayColumn(data, column, value);
                            if (display == null || display.isBlank()) {
                                display = "";
                            }
                            row.add(display);
                        }
                        convertController.writeRow(row);
                        return true;
                    } catch (Exception e) {
                        updateLogs(e.toString());
                        return false;
                    }
                }

                private boolean handleEpidemicReportTop() {
                    try ( Connection conn = DerbyBase.getConnection()) {
                        conn.setReadOnly(true);
                        String where = queryController.savedCondition.getWhere();
                        String order = queryController.savedCondition.getOrder();
                        currentSQL = queryController.savedCondition.getPrefix() + " "
                                + (where == null || where.isBlank() ? "" : " WHERE " + where)
                                + (order == null || order.isBlank() ? "" : " ORDER BY " + order);
                        updateLogs(currentSQL + "\n" + Languages.message("NumberTopDataDaily") + ": " + top);
                        List<EpidemicReport> reports = new ArrayList();
                        try ( ResultSet results = conn.createStatement().executeQuery(currentSQL)) {
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            String lastDate = null;
                            List<EpidemicReport> timeReports = new ArrayList();
                            while (results.next()) {
                                if (task == null || isCancelled()) {
                                    return false;
                                }
                                EpidemicReport report = TableEpidemicReport.statisticViewQuery(conn, results, false);
                                String date = dateFormat.format(report.getTime());
                                long locationid = report.getLocationid();
                                boolean existed = false;
                                if (lastDate == null || !date.equals(lastDate)) {
                                    if (timeReports.size() > 0) {
                                        updateLogs(MessageFormat.format(Languages.message("ReadTopDateData"), timeReports.size(), lastDate));
                                        reports.addAll(timeReports);
                                    }
                                    timeReports = new ArrayList();
                                    lastDate = date;
                                } else {
                                    if (timeReports.size() >= top) {
                                        continue;
                                    }
                                    for (EpidemicReport timeReport : timeReports) {
                                        if (task == null || isCancelled()) {
                                            return false;
                                        }
                                        if (timeReport.getDataSet().equals(report.getDataSet())
                                                && timeReport.getLocationid() == locationid) {
                                            existed = true;
                                            break;
                                        }
                                    }
                                }
                                if (!existed) {
                                    GeographyCode location = TableGeographyCode.readCode(conn, locationid, true);
                                    report.setLocation(location);
                                    timeReports.add(report);
                                }
                            }
                            if (timeReports.size() > 0) {
                                updateLogs(MessageFormat.format(Languages.message("ReadTopDateData"), timeReports.size(), lastDate));
                                reports.addAll(timeReports);
                            }
                        }

                        dataSize = reports.size();
                        updateLogs(Languages.message("DataSize") + ": " + dataSize);
                        if (dataSize == 0) {
                            return false;
                        }
                        return writeFiles(reports);
                    } catch (Exception e) {
                        MyBoxLog.error(e.toString());
                        return false;
                    }
                }

                private boolean writeFiles(List<EpidemicReport> reports) {
                    try {
                        String filePrefix = targetNameInput.getText().trim();
                        int count = 0;
                        if (!convertController.openWriters(filePrefix)) {
                            return false;
                        }
                        for (EpidemicReport report : reports) {
                            if (cancelled) {
                                updateLogs(Languages.message("Cancelled") + " " + filePrefix);
                                return false;
                            }
                            writeRow(report);
                            count++;
                            if (verboseCheck.isSelected() && (count % 50 == 0)) {
                                updateLogs(Languages.message("Exported") + " " + count + ": " + filePrefix);
                            }
                        }
                        convertController.closeWriters();
                        return true;
                    } catch (Exception e) {
                        updateLogs(e.toString());
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    browseURI(targetPath.toURI());
                    updateLogs(Languages.message("Completed"));
                }

                @Override
                protected void finalAction() {
                    StyleTools.setNameIcon(startButton, Languages.message("Start"), "iconStart.png");
                    startButton.applyCss();
                    startButton.setUserData(null);
                }
            };
            start(task, false);
        }
    }

    @FXML
    @Override
    public void selectAllAction() {
        for (Node node : fieldsPane.getChildren()) {
            CheckBox cb = (CheckBox) node;
            cb.setSelected(true);
        }
    }

    @FXML
    @Override
    public void selectNoneAction() {
        for (Node node : fieldsPane.getChildren()) {
            CheckBox cb = (CheckBox) node;
            cb.setSelected(false);
        }
    }

    public boolean checkSettings() {
        if (targetPath == null) {
            tabPane.getSelectionModel().select(targetTab);
            popError(Languages.message("InvalidTargetPath"));
            return false;
        }
        if (targetNameInput.getText().trim().isBlank()) {
            tabPane.getSelectionModel().select(targetTab);
            popError(Languages.message("TargetPrefixEmpty"));
            return false;
        }
        if (!currentPage) {
            queryController.savedCondition = queryController.save();
            if (queryController.savedCondition == null) {
                popError(Languages.message("InvalidParameters"));
                return false;
            } else {
                queryController.loadList();
            }
        }
        if (!convertController.initParameters()) {
            tabPane.getSelectionModel().select(formatsTab);
            return false;
        }
        columns = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (Node node : fieldsPane.getChildren()) {
            CheckBox cb = (CheckBox) node;
            if (cb.isSelected()) {
                columns.add((ColumnDefinition) (cb.getUserData()));
                names.add(cb.getText());
            }
        }
        if (columns.isEmpty()) {
            tabPane.getSelectionModel().select(fieldsTab);
            popError(Languages.message("NoData"));
            return false;
        }
        convertController.names = names;
        return true;
    }

    protected boolean validTopOrder() {
        if (queryController.savedCondition == null || top <= 0) {
            return false;
        }
        String order = queryController.savedCondition.getOrder();
        if (order == null || order.isBlank()) {
            return false;
        }
        order = order.trim().toLowerCase();
        return order.startsWith("time ") || order.startsWith("time,");
    }

    @Override
    public void cleanPane() {
        cancelled = true;
        super.cleanPane();
    }

}
