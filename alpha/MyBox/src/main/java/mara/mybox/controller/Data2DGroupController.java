package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.FileDeleteTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2022-9-23
 * @License Apache License Version 2.0
 */
public class Data2DGroupController extends BaseData2DHandleController {

    protected List<Integer> dataColsIndices;
    protected DataFileCSV resultsFile;
    protected List<DataFileCSV> files;
    protected File currentFile;
    protected CSVPrinter csvPrinter;
    protected long rowIndex, startIndex, currentSize;
    protected String prefix;

    @FXML
    protected ControlData2DGroup groupController;
    @FXML
    protected ControlSelection sortController;
    @FXML
    protected RadioButton fileRadio, filesRadio;

    public Data2DGroupController() {
        baseTitle = message("SplitGroup");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            notSelectColumnsInTable(true);

            groupController.setParameters(this);
            groupController.columnsController.selectedNotify.addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue ov, Boolean oldValue, Boolean newValue) {
                    makeSortList();
                }
            });
            groupController.typeGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue ov, Toggle oldValue, Toggle newValue) {
                    makeSortList();
                }
            });

            sortController.setParameters(this, message("Sort"), message("Sort"));

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void refreshControls() {
        try {
            super.refreshControls();

            groupController.refreshControls();

            makeSortList();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void makeSortList() {
        try {
            if (!data2D.isValid()) {
                sortController.loadNames(null);
                return;
            }
            List<String> names = new ArrayList<>();
            for (String name : data2D.columnNames()) {
                names.add(name + "-" + message("Descending"));
                names.add(name + "-" + message("Ascending"));
            }
            sortController.loadNames(names);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean initData() {
        try {
            if (!groupController.pickValues() || super.initData()) {
                return false;
            }
            resultsFile = null;

            List<String> colsNames = new ArrayList<>();

            if (groupController.byEqualValues()) {
                colsNames.addAll(groupController.groupNames);

            } else if (groupController.byConditions()) {
                colsNames = data2D.columnNames();

            } else {
                colsNames.add(groupController.groupName);

            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    protected void startOperation() {
        if (task != null) {
            task.cancel();
        }
        resultsFile = null;
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    Data2D tmp2D = data2D.cloneAll();
                    List<Data2DColumn> tmpColumns = new ArrayList<>();
                    for (Data2DColumn column : data2D.columns) {
                        Data2DColumn tmpColumn = column.cloneAll();
                        String name = tmpColumn.getColumnName();
                        if (groupController.groupName != null && groupController.groupName.equals(name)) {
                            tmpColumn.setType(ColumnDefinition.ColumnType.Double);
                        }
                        tmpColumns.add(tmpColumn);
                    }
                    tmp2D.setColumns(tmpColumns);
                    tmp2D.startTask(task, filterController.filter);
                    DataTable tmpTable;
                    List<Integer> colIndices = data2D.columnIndices();
                    if (isAllPages()) {
                        tmpTable = tmp2D.toTmpTable(task, colIndices, false, false, invalidAs);
                    } else {
                        outputData = filtered(colIndices, false);
                        if (outputData == null || outputData.isEmpty()) {
                            error = message("NoData");
                            return false;
                        }
                        tmpTable = tmp2D.toTmpTable(task, colIndices, outputData, false, false, invalidAs);
                        outputData = null;
                    }
                    tmp2D.stopFilter();
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
                data2D.stopTask();
                task = null;
            }

        };
        start(task);
    }

    @Override
    public boolean handleRows() {
        try {
//            switch (splitController.splitType) {
//                case Size:
//                    return handleRowsBySize((int) splitController.size);
//                case Number:
//                    return handleRowsBySize(splitController.size(tableData.size(), splitController.number));
//                case List:
//                    return handleRowsByList();
//            }
            return false;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    public boolean handleRowsBySize(int splitSize) {
        try {
            if (selectedRowsIndices == null || selectedRowsIndices.isEmpty()
                    || checkedColsIndices == null || checkedColsIndices.isEmpty()) {
                return false;
            }
            int total = tableData.size();
            boolean showRowNumber = showRowNumber();
            prefix = data2D.dataName();
            startIndex = 1;
            currentSize = 0;
            files = new ArrayList<>();
            currentFile = null;
            csvPrinter = null;
            data2D.resetFilterNumber();
            for (int r : selectedRowsIndices) {
                if (r < 0 || r >= total) {
                    continue;
                }
                List<String> tableRow = tableData.get(r);
                if (!data2D.filterTableRow(tableRow, r)) {
                    continue;
                }
                if (data2D.filterReachMaxPassed()) {
                    break;
                }
                List<String> row = new ArrayList<>();
                if (data2D.isTmpData()) {
                    rowIndex = r + 1;
                } else {
                    rowIndex = Long.valueOf(tableRow.get(0));
                }
                if (showRowNumber) {
                    row.add(rowIndex + "");
                }
                for (int c : checkedColsIndices) {
                    int index = c + 1;
                    if (index < 0 || index >= tableRow.size()) {
                        continue;
                    }
                    row.add(tableRow.get(index));
                }
                if (csvPrinter == null) {
                    currentFile = TmpFileTools.getTempFile(".csv");
                    csvPrinter = CsvTools.csvPrinter(currentFile);
                    csvPrinter.printRecord(checkedColsNames);
                    startIndex = rowIndex;
                }
                if (showRowNumber) {
                    row.add(0, rowIndex + "");
                }
                csvPrinter.printRecord(row);
                currentSize++;
                if (currentSize % splitSize == 0) {
                    closeFile();
                }
            }
            closeFile();
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
    }

    public void closeFile() {
        try {
            if (csvPrinter == null) {
                return;
            }
            csvPrinter.flush();
            csvPrinter.close();
            csvPrinter = null;
            File file = data2D.tmpFile(prefix + "_" + startIndex + "-" + rowIndex, null, ".csv");
            if (FileTools.rename(currentFile, file) && file.exists()) {
                DataFileCSV dataFileCSV = new DataFileCSV();
                dataFileCSV.setTask(task);
                dataFileCSV.setColumns(outputColumns)
                        .setFile(file)
                        .setCharset(Charset.forName("UTF-8"))
                        .setDelimiter(",")
                        .setHasHeader(true)
                        .setColsNumber(outputColumns.size())
                        .setRowsNumber(currentSize);
                dataFileCSV.saveAttributes();
                dataFileCSV.stopTask();
                files.add(dataFileCSV);
            }
            currentFile = null;
            currentSize = 0;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
        }
    }

    public boolean handleRowsByList() {
        try {
            if (selectedRowsIndices == null || selectedRowsIndices.isEmpty()
                    || checkedColsIndices == null || checkedColsIndices.isEmpty()) {
                return false;
            }
            int total = tableData.size();
            boolean showRowNumber = showRowNumber();
            prefix = data2D.dataName();
            files = new ArrayList<>();
            data2D.resetFilterNumber();
            dataSize = data2D.isTmpData() ? total : data2D.dataSize;
            for (int i = 0; i < groupController.splitController.list.size();) {
                long start = Math.round(groupController.splitController.list.get(i++));
                long end = Math.round(groupController.splitController.list.get(i++));
                if (start <= 0) {
                    start = 1;
                }
                if (end > dataSize) {
                    end = dataSize;
                }
                if (start > end) {
                    continue;
                }
                File csvfile = data2D.tmpFile(prefix + "_" + start + "-" + end, null, ".csv");
                boolean empty = true;
                try ( CSVPrinter printer = CsvTools.csvPrinter(csvfile)) {
                    printer.printRecord(checkedColsNames);
                    for (int r : selectedRowsIndices) {
                        if (r < 0 || r >= total) {
                            continue;
                        }
                        List<String> tableRow = tableData.get(r);
                        if (data2D.isTmpData()) {
                            rowIndex = r + 1;
                        } else {
                            rowIndex = Long.valueOf(tableRow.get(0));
                        }
                        if (rowIndex < start) {
                            continue;
                        }
                        if (rowIndex > end) {
                            break;
                        }
                        if (!data2D.filterTableRow(tableRow, r)) {
                            continue;
                        }
                        if (data2D.filterReachMaxPassed()) {
                            break;
                        }
                        List<String> row = new ArrayList<>();
                        if (showRowNumber) {
                            row.add(rowIndex + "");
                        }
                        for (int c : checkedColsIndices) {
                            int index = c + 1;
                            if (index < 0 || index >= tableRow.size()) {
                                continue;
                            }
                            row.add(tableRow.get(index));
                        }
                        printer.printRecord(row);
                        empty = false;
                    }
                } catch (Exception e) {
                    if (task != null) {
                        task.setError(e.toString());
                    }
                    MyBoxLog.error(e);
                }
                if (empty) {
                    FileDeleteTools.delete(csvfile);
                } else {
                    DataFileCSV dataFileCSV = new DataFileCSV();
                    dataFileCSV.setTask(task);
                    dataFileCSV.setColumns(outputColumns)
                            .setFile(csvfile)
                            .setCharset(Charset.forName("UTF-8"))
                            .setDelimiter(",")
                            .setHasHeader(true)
                            .setColsNumber(outputColumns.size())
                            .setRowsNumber(end - start + 1);
                    dataFileCSV.saveAttributes();
                    dataFileCSV.stopTask();
                    files.add(dataFileCSV);
                }
            }
            return true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            } else {
                MyBoxLog.error(e);
            }
            return false;
        }
    }

    @Override
    public void ouputRows() {
        ouputFiles();
    }

    public void ouputFiles() {
        if (files == null || files.isEmpty()) {
            popError(message("NoFileGenerated"));
            return;
        }
        browse(files.get(0).getFile().getParentFile());
        popInformation(MessageFormat.format(message("FilesGenerated"), files.size()));
    }

    @Override
    public void handleAllTask() {
        if (task != null) {
            task.cancel();
        }
        files = null;
        task = new SingletonTask<Void>(this) {

            @Override
            protected boolean handle() {
                try {
                    data2D.startTask(task, filterController.filter);
//                    switch (splitController.splitType) {
//                        case Size:
//                            files = data2D.splitBySize(checkedColsIndices, showRowNumber(), (int) splitController.size);
//                            break;
//                        case Number:
//                            files = data2D.splitBySize(checkedColsIndices, showRowNumber(),
//                                    splitController.size(data2D.dataSize, splitController.number));
//                            break;
//                        case List:
//                            files = data2D.splitByList(checkedColsIndices, showRowNumber(), splitController.list);
//                            break;
//                    }
                    data2D.stopFilter();
                    return files != null;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                ouputFiles();
            }

            @Override
            protected void finalAction() {
                super.finalAction();
                data2D.stopTask();
                task = null;
            }

        };
        start(task);
    }

    /*
        static
     */
    public static Data2DGroupController open(ControlData2DLoad tableController) {
        try {
            Data2DGroupController controller = (Data2DGroupController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DGroupFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
