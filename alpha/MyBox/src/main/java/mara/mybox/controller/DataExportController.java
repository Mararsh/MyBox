package mara.mybox.controller;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import mara.mybox.data2d.reader.Data2DExport;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.BaseData;
import mara.mybox.db.data.BaseDataAdaptor;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.QueryCondition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.BaseTable;
import mara.mybox.db.table.TableGeographyCode;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-5-2
 * @License Apache License Version 2.0
 */
public class DataExportController extends BaseTaskController {

    protected BaseDataManageController dataController;
    protected Data2DExport export;
    protected BaseTable table;
    protected String currentSQL;
    protected long startTime, dataSize;
    protected boolean currentPage = false;
    protected List<ColumnDefinition> columns;
    protected int top;

    @FXML
    protected Tab fieldsTab, targetTab, formatsTab;
    @FXML
    protected ControlDataQuery queryController;
    @FXML
    protected ControlDataConvert convertController;
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

            convertController.setControls(this);

            fieldsPane.getChildren().clear();
            List<ColumnDefinition> tableColumns = table.getColumns();
            for (ColumnDefinition column : tableColumns) {
                CheckBox cb = new CheckBox(column.getColumnName());
                cb.setUserData(column);
                cb.setSelected(UserConfig.getBoolean(baseName + column.getColumnName(), false));
                cb.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) -> {
                    UserConfig.setBoolean(baseName + column.getColumnName(), cb.isSelected());
                });
                fieldsPane.getChildren().add(cb);
            }

            StyleTools.setNameIcon(startButton, Languages.message("Start"), "iconStart.png");
            startButton.applyCss();
            startButton.setUserData(null);
            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(targetPathController.valid.not());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void currentPage(BaseDataManageController dataController,
            QueryCondition initCondition, String tableDefinition,
            boolean prefixEditable, boolean supportTop) {
        setValues(dataController, initCondition, tableDefinition, prefixEditable, supportTop);
        currentPage = true;
//        queryController.thisPane.setDisable(true);
    }

    @Override
    public boolean checkOptions() {
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
        export = convertController.pickParameters(null);
        if (export == null) {
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
        export.setNames(names);
        return true;
    }

    @FXML
    @Override
    public void startAction() {
        queryController.savedCondition = queryController.save();
        if (queryController.savedCondition == null) {
            return;
        }
        if (!checkOptions()) {
            return;
        }
        targetFilesCount = 0;
        targetFiles = new LinkedHashMap<>();
        top = queryController.savedCondition.getTop();
        Platform.runLater(() -> {
            if (startButton.getUserData() == null) {
                runTask();
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

    @Override
    public void runTask() {
        if (task != null) {
            task.cancel();
        }
        cancelled = false;
        tabPane.getSelectionModel().select(logsTab);
        startTime = new Date().getTime();
        beforeTask();
        task = new FxSingletonTask<Void>(this) {

            private final boolean skip = targetPathController.isSkip();

            @Override
            protected boolean handle() {
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
                try (Connection conn = DerbyBase.getConnection()) {
                    conn.setReadOnly(true);
                    int count = 0;
                    if (!export.openWriters(filePrefix, skip)) {
                        return false;
                    }
                    try (ResultSet results = conn.createStatement().executeQuery(currentSQL)) {
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
                    export.closeWriters();
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
                        Object value = BaseDataAdaptor.getColumnValue(data, column.getColumnName());
                        String display = BaseDataAdaptor.displayColumn(data, column, value);
                        if (display == null || display.isBlank()) {
                            display = "";
                        }
                        row.add(display);
                    }
                    export.writeRow(row);
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
                super.finalAction();
                StyleTools.setNameIcon(startButton, Languages.message("Start"), "iconStart.png");
                startButton.applyCss();
                startButton.setUserData(null);
                afterTask();
            }
        };
        start(task, false);
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
