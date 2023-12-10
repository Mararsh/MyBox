package mara.mybox.controller;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2DExampleTools;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.Database;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DRow;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.db.table.TableData2D;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.CsvTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2022-2-21
 * @License Apache License Version 2.0
 */
public abstract class BaseData2DController extends BaseFileController {

    protected Data2D.Type type;
    protected TableData2DDefinition tableData2DDefinition;
    protected Data2D data2D;
    protected FxTask parseTask;

    @FXML
    protected ControlData2DList listController;
    @FXML
    protected ControlData2D dataController;
    @FXML
    protected ControlData2DLoad loadController;
    @FXML
    protected Label nameLabel;

    public BaseData2DController() {
        type = Data2DDefinition.Type.Texts;
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            initData();

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    public void initData() {
        try {
            setDataType(type);

            if (listController != null) {
                listController.setParameters(this);
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    // subclass should call this
    public void setDataType(Data2D.Type type) {
        try {
            this.type = type;
            if (dataController != null) {
                dataController.setDataType(this, type);
                loadController = dataController.editController.tableController;

            } else if (loadController != null) {
                loadController.setData(Data2D.create(type));

            }

            tableData2DDefinition = loadController.tableData2DDefinition;
            data2D = loadController.data2D;

            loadController.dataLabel = nameLabel;
            loadController.baseTitle = baseTitle;
            checkButtons();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            if (dataController != null) {
                dataController.setParameters(this);
            }

            if (listController != null) {
                rightPaneControl = listController.rightPaneControl;
                initRightPaneControl();
            }

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void checkButtons() {
        if (saveButton != null) {
            saveButton.setDisable(loadController.data2D == null || !loadController.data2D.isValid());
        }
        if (recoverButton != null) {
            recoverButton.setDisable(loadController.data2D == null || loadController.data2D.isTmpData());
        }
    }

    public void loadDef(Data2DDefinition def) {
        if (loadController == null || !checkBeforeNextAction()) {
            return;
        }
        loadController.loadDef(def);
        checkButtons();
    }

    public void loadCSVData(DataFileCSV csvData) {
        if (csvData == null || loadController == null || !checkBeforeNextAction()) {
            return;
        }
        loadController.loadCSVData(csvData);
    }

    public void loadTableData(DataTable dataTable) {
        if (dataTable == null || loadController == null || !checkBeforeNextAction()) {
            return;
        }
        loadController.loadTableData(dataTable);
    }

    public void loadData(List<Data2DColumn> cols, List<List<String>> data) {
        if (loadController == null || !checkBeforeNextAction()) {
            return;
        }
        loadController.loadTmpData(null, cols, data);
        checkButtons();
    }

    @FXML
    @Override
    public void createAction() {
        if (dataController == null) {
            return;
        }
        dataController.create();
    }

    @FXML
    @Override
    public void recoverAction() {
        if (dataController == null) {
            return;
        }
        dataController.recover();
    }

    @FXML
    @Override
    public void refreshAction() {
        if (listController == null) {
            return;
        }
        listController.refreshAction();
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        if (dataController == null) {
            return;
        }
        dataController.loadContentInSystemClipboard();
    }

    @FXML
    public void editAction() {
        Data2DDefinition.open(loadController.data2D);
    }

    @FXML
    @Override
    public void saveAction() {
        if (dataController == null) {
            return;
        }
        dataController.save();
    }

    @FXML
    protected void popExamplesMenu(MouseEvent mouseEvent) {
        if (UserConfig.getBoolean("Data2DExamplesPopWhenMouseHovering", true)) {
            examplesMenu(mouseEvent);
        }
    }

    @FXML
    protected void showExamplesMenu(ActionEvent event) {
        examplesMenu(event);
    }

    @FXML
    protected void examplesMenu(Event event) {
        try {
            List<MenuItem> items = new ArrayList<>();
            items.addAll(Data2DExampleTools.examplesMenu(dataController));

            items.add(new SeparatorMenuItem());

            CheckMenuItem pMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            pMenu.setSelected(UserConfig.getBoolean("Data2DExamplesPopWhenMouseHovering", true));
            pMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean("Data2DExamplesPopWhenMouseHovering", pMenu.isSelected());
                }
            });
            items.add(pMenu);

            popEventMenu(event, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public boolean checkBeforeNextAction() {
        if (dataController != null) {
            return dataController.checkBeforeNextAction();
        } else {
            return true;
        }
    }

    @Override
    public boolean keyEventsFilter(KeyEvent event) {
        if (!super.keyEventsFilter(event)) {
            if (dataController != null) {
                return dataController.keyEventsFilter(event);
            } else if (loadController != null) {
                return loadController.keyEventsFilter(event);
            }
        }
        return true;
    }

    @Override
    public void myBoxClipBoard() {
        if (dataController != null) {
            dataController.myBoxClipBoard();
        } else if (loadController != null) {
            loadController.myBoxClipBoard();
        }

    }

    @FXML
    public void importAction() {
        if (parseTask != null && !parseTask.isQuit()) {
            return;
        }
        parseTask = new FxTask<Void>(this) {

            DataTable dataTable;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    List<Data2DColumn> columns = new ArrayList<>();
                    columns.add(new Data2DColumn("skey", ColumnDefinition.ColumnType.String, true, true));
                    columns.add(new Data2DColumn("Date", ColumnDefinition.ColumnType.String));
                    columns.add(new Data2DColumn("Country", ColumnDefinition.ColumnType.String));
                    columns.add(new Data2DColumn("Province", ColumnDefinition.ColumnType.String));
                    columns.add(new Data2DColumn("Confirmed", ColumnDefinition.ColumnType.String));
                    columns.add(new Data2DColumn("Recovered", ColumnDefinition.ColumnType.String));
                    columns.add(new Data2DColumn("Dead", ColumnDefinition.ColumnType.String));
                    columns.add(new Data2DColumn("Longitude", ColumnDefinition.ColumnType.String));
                    columns.add(new Data2DColumn("Latitude", ColumnDefinition.ColumnType.String));

                    dataTable = DataTable.createTable(parseTask, conn, "CSSEGISandData", columns, null, null, null, true);
                    TableData2D tableCountry = dataTable.getTableData2D();

                    conn.setAutoCommit(false);

                    long recoveryCount = 0;
                    parseTask.setInfo("D:/下载/time_series_covid19_recovered_global.csv");
                    File srcFile = new File("D:/下载/time_series_covid19_recovered_global.csv");
                    try (PreparedStatement countryInsert = conn.prepareStatement(tableCountry.insertStatement());
                            CSVParser parser = CsvTools.csvParser(srcFile, ",", true)) {
                        List<String> header = parser.getHeaderNames();
                        for (CSVRecord record : parser) {
                            if (parseTask == null || isCancelled()) {
                                return false;
                            }
                            String country = record.get("Country/Region");
                            String provice = record.get("Province/State");
                            String latitude = record.get("Lat");
                            String longitude = record.get("Long");
                            parseTask.setInfo(country + "  " + provice + "  " + recoveryCount);
                            for (int i = 4; i < record.size(); i++) {
                                if (parseTask == null || isCancelled()) {
                                    return false;
                                }
                                Data2DRow countryRow = tableCountry.newRow();
                                String value = record.get(i);
                                if (value == null || value.isBlank() || "0".equals(value)) {
                                    continue;
                                }
                                String date = header.get(i);
                                countryRow.setColumnValue("skey", date + country + provice);
                                countryRow.setColumnValue("Date", date);
                                countryRow.setColumnValue("Country", country);
                                countryRow.setColumnValue("Province", provice);
                                countryRow.setColumnValue("Longitude", longitude);
                                countryRow.setColumnValue("Latitude", latitude);
                                countryRow.setColumnValue("Recovered", value);
                                if (tableCountry.setInsertStatement(conn, countryInsert, countryRow)) {
                                    countryInsert.addBatch();
                                    if (++recoveryCount % Database.BatchSize == 0) {
                                        countryInsert.executeBatch();
                                        conn.commit();
                                        parseTask.setInfo(message("Inserted") + ": " + "recoveryCount " + recoveryCount);
                                    }
                                }
                            }
                        }
                        countryInsert.executeBatch();
                        conn.commit();
                        parseTask.setInfo(message("Inserted") + ": " + "recoveryCount " + recoveryCount);
                    } catch (Exception e) {
                        parseTask.setInfo(e.toString());
                    }

                    long deadCount = 0;
                    parseTask.setInfo("D:/下载/time_series_covid19_deaths_global.csv");
                    srcFile = new File("D:/下载/time_series_covid19_deaths_global.csv");
                    try (PreparedStatement countryQuery = conn.prepareStatement("SELECT * FROM CSSEGISandData WHERE skey=?");
                            PreparedStatement countryInsert = conn.prepareStatement(tableCountry.insertStatement());
                            PreparedStatement countryUpdate = conn.prepareStatement(tableCountry.updateStatement());
                            CSVParser parser = CsvTools.csvParser(srcFile, ",", true)) {
                        List<String> header = parser.getHeaderNames();
                        for (CSVRecord record : parser) {
                            if (parseTask == null || isCancelled()) {
                                return false;
                            }
                            String country = record.get("Country/Region");
                            String provice = record.get("Province/State");
                            String latitude = record.get("Lat");
                            String longitude = record.get("Long");
                            parseTask.setInfo(country + "  " + provice + "  " + deadCount + "    " + (int) (deadCount * 100 / recoveryCount) + "%");
                            for (int i = 4; i < record.size(); i++) {
                                if (parseTask == null || isCancelled()) {
                                    return false;
                                }
                                String value = record.get(i);
                                if (value == null || value.isBlank() || "0".equals(value)) {
                                    continue;
                                }
                                String date = header.get(i);
                                String key = date + country + provice;
                                countryQuery.setString(1, key);
                                Data2DRow countryRow = tableCountry.query(conn, countryQuery);
                                if (countryRow == null) {
                                    countryRow = tableCountry.newRow();
                                    countryRow.setColumnValue("skey", key);
                                    countryRow.setColumnValue("Date", date);
                                    countryRow.setColumnValue("Country", country);
                                    countryRow.setColumnValue("Province", provice);
                                    countryRow.setColumnValue("Longitude", longitude);
                                    countryRow.setColumnValue("Latitude", latitude);
                                    countryRow.setColumnValue("Dead", value);
                                    if (tableCountry.setInsertStatement(conn, countryInsert, countryRow)) {
                                        countryInsert.addBatch();
                                        if (++deadCount % Database.BatchSize == 0) {
                                            countryInsert.executeBatch();
                                            conn.commit();
                                            parseTask.setInfo(message("Inserted") + ": " + "deadCount " + deadCount
                                                    + "    " + (int) (deadCount * 100 / recoveryCount) + "%");
                                        }
                                    }
                                } else {
                                    countryRow.setColumnValue("Dead", value);
                                    if (tableCountry.setUpdateStatement(conn, countryUpdate, countryRow)) {
                                        countryUpdate.addBatch();
                                        if (++deadCount % Database.BatchSize == 0) {
                                            countryUpdate.executeBatch();
                                            conn.commit();
                                            parseTask.setInfo(message("Updated") + ": " + "deadCount " + deadCount
                                                    + "    " + (int) (deadCount * 100 / recoveryCount) + "%");
                                        }
                                    }
                                }
                            }
                        }
                        countryInsert.executeBatch();
                        countryUpdate.executeBatch();
                        conn.commit();
                        parseTask.setInfo(message("Updated") + ": " + "deadCount " + deadCount);
                    } catch (Exception e) {
                        parseTask.setInfo(e.toString());
                    }

                    long confirmedCount = 0;
                    parseTask.setInfo("D:/下载/time_series_covid19_confirmed_global.csv");
                    srcFile = new File("D:/下载/time_series_covid19_confirmed_global.csv");
                    try (PreparedStatement countryQuery = conn.prepareStatement("SELECT * FROM CSSEGISandData WHERE skey=?");
                            PreparedStatement countryInsert = conn.prepareStatement(tableCountry.insertStatement());
                            PreparedStatement countryUpdate = conn.prepareStatement(tableCountry.updateStatement());
                            CSVParser parser = CsvTools.csvParser(srcFile, ",", true)) {
                        List<String> header = parser.getHeaderNames();
                        for (CSVRecord record : parser) {
                            if (parseTask == null || isCancelled()) {
                                return false;
                            }
                            String country = record.get("Country/Region");
                            String provice = record.get("Province/State");
                            String latitude = record.get("Lat");
                            String longitude = record.get("Long");
                            parseTask.setInfo(country + "  " + provice + "  " + confirmedCount + "    " + (int) (confirmedCount * 100 / deadCount) + "%");
                            for (int i = 4; i < record.size(); i++) {
                                if (parseTask == null || isCancelled()) {
                                    return false;
                                }
                                String value = record.get(i);
                                if (value == null || value.isBlank() || "0".equals(value)) {
                                    continue;
                                }
                                String date = header.get(i);
                                String key = date + country + provice;
                                countryQuery.setString(1, key);
                                Data2DRow countryRow = tableCountry.query(conn, countryQuery);
                                if (countryRow == null) {
                                    countryRow = tableCountry.newRow();
                                    countryRow.setColumnValue("skey", key);
                                    countryRow.setColumnValue("Date", date);
                                    countryRow.setColumnValue("Country", country);
                                    countryRow.setColumnValue("Province", provice);
                                    countryRow.setColumnValue("Longitude", longitude);
                                    countryRow.setColumnValue("Latitude", latitude);
                                    countryRow.setColumnValue("Confirmed", value);
                                    if (tableCountry.setInsertStatement(conn, countryInsert, countryRow)) {
                                        countryInsert.addBatch();
                                        if (++confirmedCount % Database.BatchSize == 0) {
                                            countryInsert.executeBatch();
                                            conn.commit();
                                            parseTask.setInfo(message("Inserted") + ": " + "confirmedCount " + confirmedCount
                                                    + "    " + (int) (confirmedCount * 100 / deadCount) + "%");
                                        }
                                    }
                                } else {
                                    countryRow.setColumnValue("Confirmed", value);
                                    if (tableCountry.setUpdateStatement(conn, countryUpdate, countryRow)) {
                                        countryUpdate.addBatch();
                                        if (++confirmedCount % Database.BatchSize == 0) {
                                            countryUpdate.executeBatch();
                                            conn.commit();
                                            parseTask.setInfo(message("Updated") + ": " + "confirmedCount " + confirmedCount
                                                    + "    " + (int) (confirmedCount * 100 / deadCount) + "%");
                                        }
                                    }
                                }
                            }
                        }
                        countryInsert.executeBatch();
                        countryUpdate.executeBatch();
                        conn.commit();
                        parseTask.setInfo(message("Updated") + ": " + "confirmedCount " + confirmedCount);
                    } catch (Exception e) {
                        parseTask.setInfo(e.toString());
                    }

                    try (Statement delete = conn.createStatement()) {
                        String sql = "DELETE FROM CSSEGISandData WHERE Confirmed='0' AND  Recovered='0' AND  Dead='0' ";
                        parseTask.setInfo(sql);
                        int count = delete.executeUpdate(sql);
                        parseTask.setInfo("Deleted: " + count);
                    } catch (Exception e) {
                        parseTask.setInfo(e.toString());
                    }

                    return true;

                } catch (Exception e) {
                    parseTask.setInfo(e.toString());
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                Data2D.open(dataTable);
            }
        };
        start(parseTask);
    }

    @Override
    public void cleanPane() {
        try {
            if (parseTask != null) {
                parseTask.cancel();
                parseTask = null;
            }
            dataController = null;
            loadController = null;
        } catch (Exception e) {
        }
        super.cleanPane();
    }

}
