package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
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
import mara.mybox.data2d.tools.Data2DColumnTools;
import mara.mybox.data2d.tools.Data2DExampleTools;
import mara.mybox.data2d.tools.Data2DMenuTools;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.Data2DDefinition.DataType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.FxTask;
import mara.mybox.fxml.PopTools;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.DoubleMatrixTools;
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

    /*
        status
     */
    public boolean isValidPageData() {
        if (!isValidData() || tableData == null) {
            return false;
        }
        data2D.setPageData(tableData);
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

            showPaginationPane(!data2D.isTmpData() && !data2D.isMatrix());
            updateInterface();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean loadDef(Data2DDefinition def) {
        if (!checkBeforeNextAction()) {
            return false;
        }
        resetStatus();
        if (def == null) {
            return loadNull();
        }
        data2D = Data2D.create(def.getType());
        data2D.cloneDef(def);
        setData(data2D);
        readData(true);
        return true;
    }

    public boolean loadNull() {
        return createData(DataType.CSV);
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

    public synchronized void readData(boolean reloadSize) {
        if (data2D == null) {
            loadNull();
            return;
        }
        if (!checkInvalidFile()) {
            return;
        }
        resetStatus();
        resetView(false);
        task = new FxSingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try (Connection conn = DerbyBase.getConnection()) {
                    data2D.startTask(this, null);
                    data2D.readDataDefinition(conn);
                    if (isCancelled()) {
                        return false;
                    }
                    return data2D.readColumns(conn);
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
            if (!isValidData()) {
                resetData();
                return;
            }
            dataSizeLoaded = !readSize;
            data2D.setDataLoaded(!readSize);
            data2D.setTableChanged(false);
            if (readSize) {
                loadPage(0);
            } else {
                loadPage(currentPage);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public boolean checkInvalidFile() {
        if (data2D == null) {
            return false;
        }
        File file = data2D.getFile();
        if (file == null || file.exists()) {
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

    public void loadData(List<String> cols, List<List<String>> data) {
        loadData(null, data2D.toColumns(cols), data);
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
                        Data2DColumn column = new Data2DColumn(data2D.colPrefix() + (i + 1), data2D.defaultColumnType());
                        columns.add(column);
                    }
                }
            } else {
//                data2D.setHasHeader(true);
                for (Data2DColumn col : cols) {
                    columns.add(col.cloneAll());
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
            updateTable(rows);
            dataSizeLoaded = true;
            data2D.setDataLoaded(true);
            postLoadedTableData();
            if (validateTable != null && !validateTable.isEmpty()) {
                validateTable.htmlTable();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void updateTable(List<List<String>> data) {
        try {
            isSettingValues = true;
            tableData.setAll(data);
            isSettingValues = false;

            data2D.setPageData(tableData);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void loadTmpData(int size) {
        loadData(null, data2D.tmpColumns(size), data2D.tmpData(size, 3));
    }

    public void loadMatrix(double[][] matrix) {
        if (matrix == null || matrix.length == 0) {
            loadNull();
            return;
        }
        loadData(null, data2D.tmpColumns(matrix[0].length), DoubleMatrixTools.toList(matrix));
    }

    @Override
    public void sourceFileChanged(File file) {
        try {
            if (!checkBeforeNextAction()) {
                return;
            }
            resetStatus();
            setData(Data2D.create(Data2DDefinition.type(file)));
            beforeOpenFile();
            data2D.initFile(file);
            readData(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void beforeOpenFile() {
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

    /*
        action
     */
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
                if (def.getD2did() == data2D.getD2did()) {
                    data2D.setDataName(newName);
                    if (parent != null) {
                        parent.updateStatus();
                    }
                    updateStatus();
                }

            }

        };
        start(task);
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
            if (data2D == null || !checkBeforeNextAction()) {
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
        DataTableQueryController.open(this);
    }

    @FXML
    public void verifyCurrentPage() {
        StringTable results = verifyTableData();
        if (results.isEmpty()) {
            popInformation(message("AllValuesValid"), 5000);
            return;
        }
        results.htmlTable();
    }

    public StringTable verifyTableData() {
        try {
            List<String> names = new ArrayList<>();
            names.addAll(Arrays.asList(message("Row"), message("Column"), message("Invalid")));
            StringTable stringTable = new StringTable(names, data2D.displayName());
            for (int r = 0; r < tableData.size(); r++) {
                List<String> dataRow = tableData.get(r);
                for (int c = 0; c < data2D.columnsNumber(); c++) {
                    Data2DColumn column = data2D.column(c);
                    if (column.isAuto()) {
                        continue;
                    }
                    String value = dataRow.get(c + 1);
                    String item = null;
                    if (column.isNotNull() && (value == null || value.isBlank())) {
                        item = message("Null");
                    } else if (!column.validValue(value)) {
                        item = message(column.getType().name());
                    } else if (!data2D.validValue(value)) {
                        item = message("TextDataComments");
                    }
                    if (item == null) {
                        continue;
                    }
                    List<String> invalid = new ArrayList<>();
                    invalid.addAll(Arrays.asList((r + 1) + "", column.getColumnName(), item));
                    stringTable.add(invalid);
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
            List<MenuItem> items = new ArrayList<>();
            items.addAll(Data2DExampleTools.examplesMenu(this));

            items.add(new SeparatorMenuItem());

            CheckMenuItem pMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
            pMenu.setSelected(UserConfig.getBoolean(baseName + "ExamplesPopWhenMouseHovering", true));
            pMenu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    UserConfig.setBoolean(baseName + "ExamplesPopWhenMouseHovering", pMenu.isSelected());
                }
            });
            items.add(pMenu);

            popEventMenu(event, items);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    public void showHelps(Event event) {
        List<MenuItem> items = Data2DMenuTools.helpMenus(this);

        items.add(new SeparatorMenuItem());

        CheckMenuItem hoverMenu = new CheckMenuItem(message("PopMenuWhenMouseHovering"), StyleTools.getIconImageView("iconPop.png"));
        hoverMenu.setSelected(UserConfig.getBoolean("Data2DHelpsPopWhenMouseHovering", false));
        hoverMenu.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UserConfig.setBoolean("Data2DHelpsPopWhenMouseHovering", hoverMenu.isSelected());
            }
        });
        items.add(hoverMenu);

        popEventMenu(event, items);
    }

    @FXML
    public void popHelps(Event event) {
        if (UserConfig.getBoolean("Data2DHelpsPopWhenMouseHovering", false)) {
            showHelps(event);
        }
    }

}
