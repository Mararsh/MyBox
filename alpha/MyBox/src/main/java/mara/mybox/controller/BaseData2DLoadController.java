package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.Clipboard;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.Data2D_Attributes.TargetType;
import mara.mybox.data2d.DataClipboard;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.data2d.DataFileText;
import mara.mybox.data2d.example.Data2DExampleTools;
import mara.mybox.data2d.operate.Data2DVerify;
import mara.mybox.data2d.tools.Data2DColumnTools;
import mara.mybox.data2d.tools.Data2DMenuTools;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DDefinition.DataType;
import mara.mybox.db.table.TableData2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.menu.MenuTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-12-17
 * @License Apache License Version 2.0
 */
public class BaseData2DLoadController extends BaseData2DTableController {

    protected boolean forConvert = false;

    /*
        status
     */
    public boolean isValidPageData() {
        if (!hasColumns() || tableData == null) {
            return false;
        }
        return true;
    }

    /*
        data
     */
    public boolean createData(DataType type) {
        if (!checkBeforeNextAction()) {
            return false;
        }
        resetStatus();
        if (type == null) {
            type = DataType.CSV;
        }
        data2D = Data2D.create(type);
        loadTmpData(3);
        return true;
    }

    public void setData(Data2D data) {
        try {
            if (data == null) {
                data2D = Data2D.create(DataType.CSV);
            } else {
                data2D = data;
            }
            tableData2DDefinition = data2D.getTableData2DDefinition();
            tableData2DColumn = data2D.getTableData2DColumn();
            data2D.setController(this);
            data2D.pagination.pageSize = pagination.pageSize;
            pagination = data2D.pagination;
            if (paginationController != null) {
                paginationController.pagination = data2D.pagination;
            }
            updateStatus();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean loadDef(Data2DDefinition def) {
        if (forConvert && def != null && def instanceof DataFileCSV) {
            data2D = (DataFileCSV) def;
            notifyLoaded();
            return true;
        } else {
            return loadDef(def, true);
        }
    }

    public boolean loadDef(Data2DDefinition def, boolean checkUpdated) {
        if (checkUpdated && !checkBeforeNextAction()) {
            return false;
        }
        resetStatus();
        if (def == null) {
            return loadNull();
        }
        Data2D data = Data2D.create(def.getType());
        data.cloneFrom(def);
        data.setTableChanged(false);
        setData(data);
        readData(true);
        return true;
    }

    public synchronized void readData(boolean reloadSize) {
        if (data2D == null) {
            loadNull();
            return;
        }
        if (!checkFileValid()) {
            return;
        }
        resetStatus();
        resetView(false);
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    data2D.startTask(this, null);
                    data2D.loadDataDefinition(conn);
                    if (isCancelled()) {
                        return false;
                    }
                    return data2D.loadColumns(conn);
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                loadPage(reloadSize);
            }

        };
        start(task, thisPane);
    }

    public void loadPage(boolean readSize) {
        try {
            resetStatus();
            makeColumns();
            if (invalidData()) {
                resetData();
                postLoadedTableData();
                return;
            }
            dataSizeLoaded = !readSize;
            data2D.setDataLoaded(!readSize);
            data2D.setTableChanged(false);
            if (readSize) {
                loadPage(0);
            } else {
                loadPage(pagination.currentPage);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean checkFileValid() {
        if (data2D == null) {
            return false;
        }
        if (!data2D.isDataFile()) {
            return true;
        }
        File file = data2D.getFile();
        if (file == null) {
            return false;
        }
        if (!file.isDirectory() && file.exists()) {
            return true;
        }
        FxTask nullTask = new FxTask<Void>(this) {
            @Override
            protected boolean handle() {
                try {
                    tableData2DDefinition.deleteData(data2D);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                loadNull();
            }
        };
        start(nullTask, false);
        return false;
    }

    public void loadType(DataType type, String name, List<Data2DColumn> cols, List<List<String>> data) {
        if (!checkBeforeNextAction()) {
            return;
        }
        data2D = Data2D.create(type);
        loadData(name, cols, data);
    }

    public void loadData(String name, List<Data2DColumn> cols, List<List<String>> data) {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            resetStatus();
            if (data2D == null) {
                data2D = Data2D.create(DataType.CSV);
            } else {
                data2D.resetData();
            }
            List<Data2DColumn> columns = new ArrayList<>();
            if (cols == null || cols.isEmpty()) {
//                data2D.setHasHeader(false);
                if (data != null && !data.isEmpty()) {
                    for (int i = 0; i < data.get(0).size(); i++) {
                        Data2DColumn column = new Data2DColumn(
                                data2D.colPrefix() + (i + 1), data2D.defaultColumnType());
                        columns.add(column);
                    }
                }
            } else {
//                data2D.setHasHeader(true);
                for (Data2DColumn col : cols) {
                    columns.add(col.copy());
                }
            }
            for (Data2DColumn column : columns) {
                column.setIndex(data2D.newColumnIndex());
            }
            data2D.setColumns(columns);
            StringTable validateTable = Data2DColumnTools.validate(columns);
            List<List<String>> rows = new ArrayList<>();
            if (data != null) {
                for (int i = 0; i < data.size(); i++) {
                    List<String> row = new ArrayList<>();
                    row.add("-1");
                    row.addAll(data.get(i));
                    rows.add(row);
                }
            }
            data2D.checkForLoad();
            data2D.setDataName(name);
            resetView(false);
            setData(data2D);
            makeColumns();
            dataSizeLoaded = true;
            data2D.setDataLoaded(true);
            updateTable(rows);
            if (validateTable != null && !validateTable.isEmpty()) {
                validateTable.htmlTable();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean loadNull() {
        return createData(null);
    }

    public void loadTmpData(int size) {
        loadData(null, data2D.tmpColumns(size), data2D.tmpData(size, 3));
    }

    @Override
    public void sourceFileChanged(File file) {
        if (file == null || !checkBeforeNextAction()) {
            return;
        }
        resetStatus();
        task = new FxSingletonTask<Void>(this) {

            protected Data2D fileData;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    if (tableData2DDefinition == null) {
                        tableData2DDefinition = new TableData2DDefinition();
                    }
                    Data2DDefinition def = tableData2DDefinition.queryFile(conn, file);
                    if (def == null) {
                        fileData = Data2D.create(Data2DDefinition.type(file));
                    } else {
                        fileData = Data2D.create(def.dataType);
                        fileData.cloneFrom(def);
                    }
                    fileData.initFile(file);
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                if (fileData != null) {
                    setData(fileData);
                    beforeOpenFile();
                    readData(true);
                }
            }

        };
        start(task, thisPane);
    }

    public void beforeOpenFile() {
    }

    public void resetCSVFile(File file, Charset charset, boolean withNames, String delimiter) {
        try {
            if (file == null || !checkBeforeNextAction()) {
                return;
            }
            DataFileCSV data = new DataFileCSV();
            data.setFile(file).setCharset(charset).setHasHeader(withNames).setDelimiter(delimiter);
            applyOptions(data);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadCSVFile(File file, Charset charset, boolean withNames, String delimiter) {
        try {
            if (file == null || !checkBeforeNextAction()) {
                return;
            }
            DataFileCSV data = new DataFileCSV();
            data.setFile(file).setCharset(charset).setHasHeader(withNames).setDelimiter(delimiter);
            applyOptions(data);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadExcelFile(File file, String sheet, boolean withNames) {
        try {
            if (file == null || !checkBeforeNextAction()) {
                return;
            }
            DataFileExcel data = new DataFileExcel();
            data.setFile(file).setSheet(sheet).setHasHeader(withNames);
            applyOptions(data);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadTextFile(File file, Charset charset, boolean withNames, String delimiter) {
        try {
            if (file == null || !checkBeforeNextAction()) {
                return;
            }
            DataFileText data = new DataFileText();
            data.setFile(file).setCharset(charset).setHasHeader(withNames).setDelimiter(delimiter);
            applyOptions(data);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void applyOptions(Data2D data) {
        if (data == null) {
            return;
        }
        resetStatus();
        task = new FxSingletonTask<Void>(this) {
            Data2DDefinition def;

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    def = data.queryDefinition(conn);
                    tableData2DDefinition = data.tableData2DDefinition;
                    if (def == null) {
                        def = tableData2DDefinition.insertData(conn, data);
                    } else {
                        def.setCharset(data.getCharset())
                                .setHasHeader(data.isHasHeader())
                                .setFile(data.getFile())
                                .setSheet(data.getSheet())
                                .setDelimiter(data.getDelimiter());
                        def = tableData2DDefinition.updateData(conn, def);
                    }
                    if (isCancelled()) {
                        return false;
                    }
                    conn.commit();
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                loadDef(def);
            }

        };
        start(task, thisPane);

    }

    public void loadTableData(String prefix, List<StringTable> tables) {
        if (tables == null || tables.isEmpty() || !checkBeforeNextAction()) {
            return;
        }
        resetStatus();
        task = new FxSingletonTask<Void>(this) {

            private File filePath;
            private int count;
            private String info;
            private DataFileCSV firstData;

            @Override
            protected boolean handle() {
                try {
                    filePath = new File(FileTmpTools.generatePath("csv"));
                    LinkedHashMap<File, Boolean> files = DataFileCSV.save(this, filePath,
                            prefix == null || prefix.isBlank() ? "tmp" : prefix, tables);
                    count = files != null ? files.size() : 0;
                    if (count == 0) {
                        return false;
                    }
                    Iterator<File> iterator = files.keySet().iterator();
                    File firstFile = iterator.next();
                    firstData = new DataFileCSV();
                    firstData.setFile(firstFile).setHasHeader(files.get(firstFile)).setDelimiter(",");
                    if (count > 1) {
                        info = MessageFormat.format(message("GeneratedFilesResult"),
                                count, "\"" + filePath + "\"");
                        int num = 1;
                        info += "\n    " + firstFile.getName();
                        while (iterator.hasNext()) {
                            info += "\n    " + iterator.next().getName();
                            if (++num > 10) {
                                info += "\n    ......";
                                break;
                            }
                        }
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                applyOptions(firstData);
                if (count > 1) {
                    browseURI(filePath.toURI());
                    alertInformation(info);
                }
            }

        };
        start(task, thisPane);
    }

    public void updateTable(List<List<String>> data) {
        setPageData(data);
        postLoadedTableData();
    }

    public void setPageData(List<List<String>> data) {
        try {
            isSettingValues = true;
            tableData.setAll(data);
            data2D.setPageData(tableData);
            isSettingValues = false;
            tableView.refresh();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void dataSaved() {
        try {
            popInformation(message("Saved"));
            notifySaved();
            readData(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    /*
        action
     */
    @Override
    public List<MenuItem> fileMenuItems(Event fevent) {
        return Data2DMenuTools.fileMenus(this);
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DSaveAsController.open(this);
    }

    @FXML
    public void renameAction(BaseTablePagesController parent, int index, Data2DDefinition targetData) {
        String newName = PopTools.askValue(getTitle(), message("CurrentName") + ":" + targetData.getDataName(),
                message("NewName"), targetData.getDataName() + "m");
        if (newName == null || newName.isBlank()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            private Data2DDefinition def;

            @Override
            protected boolean handle() {
                targetData.setDataName(newName);
                def = tableData2DDefinition.updateData(targetData);
                return def != null;
            }

            @Override
            protected void whenSucceeded() {
                popSuccessful();
                if (parent != null) {
                    parent.tableData.set(index, def);
                }
                if (def.getDataID() == data2D.getDataID()) {
                    data2D.setDataName(newName);
                    if (parent != null) {
                        parent.updateStatus();
                    }
                    updateStatus();
                }

            }

        };
        start(task, thisPane);
    }

    @FXML
    @Override
    public void copyToSystemClipboard() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DCopyController controller = Data2DCopyController.open(this);
        controller.targetController.setTarget(TargetType.SystemClipboard);
        controller.setBaseTitle(message("CopyToSystemClipboard"));
    }

    public void copyToSystemClipboard(List<String> names, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            String text = TextTools.rowsText(null, data, ",", names, null);
            TextClipboardTools.copyToSystemClipboard(this, text);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void copyToMyBoxClipboard(List<String> names, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            toMyBoxClipboard(null, data2D.toColumns(names), data);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void toMyBoxClipboard(String name, List<Data2DColumn> cols, List<List<String>> data) {
        try {
            if (data == null || data.isEmpty()) {
                popError(message("NoData"));
                return;
            }
            FxTask copyTask = new FxTask<Void>(this) {

                private DataClipboard clip;

                @Override
                protected boolean handle() {
                    clip = DataClipboard.create(task, name, cols, data);
                    return clip != null;
                }

                @Override
                protected void whenSucceeded() {
                    DataInMyBoxClipboardController.open(clip);
                    popDone();
                }

            };
            start(copyTask, false);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void loadContentInSystemClipboard() {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            String text = Clipboard.getSystemClipboard().getString();
            if (text == null || text.isBlank()) {
                popError(message("NoTextInClipboard"));
            }
            Data2DLoadContentInSystemClipboardController.open(this, text);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void selectAction() {
        Data2DSelectController.open(this);
    }

    @FXML
    public void sortAction() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DSortController.open(this);
    }

    @FXML
    public void transposeAction() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DTransposeController.open(this);
    }

    @FXML
    public void normalizeAction() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DNormalizeController.open(this);
    }

    @FXML
    public void groupAction() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DGroupController.open(this);
    }

    @FXML
    public void rowExpressionAction() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DRowExpressionController.open(this);
    }

    @FXML
    public void descriptiveStatisticAction() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DStatisticController.open(this);
    }

    @FXML
    public void groupStatisticAction() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DGroupStatisticController.open(this);
    }

    @FXML
    public void simpleLinearRegression() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DSimpleLinearRegressionController.open(this);
    }

    @FXML
    public void simpleLinearRegressionCombination() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DSimpleLinearRegressionCombinationController.open(this);
    }

    @FXML
    public void multipleLinearRegression() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DMultipleLinearRegressionController.open(this);
    }

    @FXML
    public void multipleLinearRegressionCombination() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DMultipleLinearRegressionCombinationController.open(this);
    }

    @FXML
    public void frequencyDistributions() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DFrequencyController.open(this);
    }

    @FXML
    public void valuePercentage() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DPercentageController.open(this);
    }

    @FXML
    public void xyChart() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DChartXYController.open(this);
    }

    @FXML
    public void bubbleChart() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DChartBubbleController.open(this);
    }

    @FXML
    public void pieChart() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DChartPieController.open(this);
    }

    @FXML
    public void boxWhiskerChart() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DChartBoxWhiskerController.open(this);
    }

    @FXML
    public void selfComparisonBarsChart() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DChartSelfComparisonBarsController.open(this);
    }

    @FXML
    public void comparisonBarsChart() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DChartComparisonBarsController.open(this);
    }

    @FXML
    public void xyzChart() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DChartXYZController.open(this);
    }

    @FXML
    public void locationDistribution() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DLocationDistributionController.open(this);
    }

    @FXML
    public void groupXYChart() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DChartGroupXYController.open(this);
    }

    @FXML
    public void groupBubbleChart() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DChartGroupBubbleController.open(this);
    }

    @FXML
    public void groupPieChart() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DChartGroupPieController.open(this);
    }

    @FXML
    public void groupBoxWhiskerChart() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DChartGroupBoxWhiskerController.open(this);
    }

    @FXML
    public void groupSelfComparisonBars() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DChartGroupSelfComparisonBarsController.open(this);
    }

    @FXML
    public void groupComparisonBars() {
        if (!isValidPageData()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DChartGroupComparisonBarsController.open(this);
    }

    @FXML
    public void queryTable() {
        if (!isValidPageData() || !data2D.isTable()) {
            popError(message("InvalidData"));
            return;
        }
        Data2DTableQueryController.open(this);
    }

    @FXML
    public void verifyCurrentPage() {
        if (data2D == null) {
            popError(message("InvalidData"));
            return;
        }
        StringTable results = verifyTableData();
        if (results.isEmpty()) {
            popInformation(message("RowsNumber") + ": " + tableData.size() + "\n"
                    + message("AllValuesValid"), 5000);
            return;
        }
        results.htmlTable();
    }

    public StringTable verifyTableData() {
        try {
            StringTable stringTable = new StringTable(Data2DVerify.columnNames(), data2D.displayName());
            for (int r = 0; r < tableData.size(); r++) {
                List<String> row = data2D.dataRow(r);
                List<List<String>> invalids = Data2DVerify.verify(data2D, r, row);
                if (invalids != null) {
                    for (List<String> invalid : invalids) {
                        stringTable.add(invalid);
                    }
                }
            }
            return stringTable;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    @FXML
    public void verifyAllData() {
        if (data2D == null) {
            popError(message("InvalidData"));
            return;
        }
        FxTask verifyTask = new FxTask<Void>(this) {
            Data2DVerify verify;

            @Override
            protected boolean handle() {
                try {
                    verify = Data2DVerify.create(data2D);
                    verify.setTask(this).start();
                    if (isCancelled()) {
                        return false;
                    }
                    return !verify.isFailed();
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                verify.openResults();
            }

        };
        start(verifyTask, false);
    }

    @FXML
    protected void popExamplesMenu(Event event) {
        if (UserConfig.getBoolean(baseName + "ExamplesPopWhenMouseHovering", true)) {
            showExamplesMenu(event);
        }
    }

    @FXML
    protected void showExamplesMenu(Event event) {
        try {
            List<MenuItem> items = MenuTools.initMenu(message("Examples"));

            items.addAll(Data2DExampleTools.examplesMenu(this));

            items.add(new SeparatorMenuItem());

            items.add(MenuTools.popCheckMenu(baseName + "Examples"));

            popEventMenu(event, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void showHelps(Event event) {
        List<MenuItem> items = Data2DMenuTools.helpMenus(this);

        items.add(new SeparatorMenuItem());

        items.add(MenuTools.popCheckMenu("Data2DHelps"));

        popEventMenu(event, items);
    }

    @FXML
    public void popHelps(Event event) {
        if (UserConfig.getBoolean("Data2DHelpsPopWhenMouseHovering", true)) {
            showHelps(event);
        }
    }

}
