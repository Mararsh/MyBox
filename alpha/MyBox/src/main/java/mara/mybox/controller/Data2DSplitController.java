package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import mara.mybox.data2d.DataFileCSV;
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
public class Data2DSplitController extends BaseData2DHandleController {

    protected List<DataFileCSV> files;
    protected File currentFile;
    protected CSVPrinter csvPrinter;
    protected long rowIndex, startIndex, currentSize;
    protected String prefix;

    @FXML
    protected ControlSplit splitController;

    public Data2DSplitController() {
        baseTitle = message("Split");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            notSelectColumnsInTable(true);

            splitController.setParameters(this);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean initData() {
        try {
            if (!splitController.valid.get()) {
                popError(message("InvalidParameters") + ": " + message("Split"));
                return false;
            }
            files = null;
            return super.initData();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    @Override
    public boolean handleRows() {
        try {
            switch (splitController.splitType) {
                case Size:
                    return handleRowsBySize(splitController.size);
                case Number:
                    return handleRowsBySize(splitController.size(tableData.size(), splitController.number));
                case List:
                    return handleRowsByList();
            }
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
            for (int i = 0; i < splitController.list.size();) {
                long start = splitController.list.get(i++);
                long end = splitController.list.get(i++);
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
                    switch (splitController.splitType) {
                        case Size:
                            files = data2D.splitBySize(checkedColsIndices, showRowNumber(), splitController.size);
                            break;
                        case Number:
                            files = data2D.splitBySize(checkedColsIndices, showRowNumber(),
                                    splitController.size(data2D.dataSize, splitController.number));
                            break;
                        case List:
                            files = data2D.splitByList(checkedColsIndices, showRowNumber(), splitController.list);
                            break;
                    }
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
    public static Data2DSplitController open(ControlData2DLoad tableController) {
        try {
            Data2DSplitController controller = (Data2DSplitController) WindowTools.openChildStage(
                    tableController.getMyWindow(), Fxmls.Data2DSplitFxml, false);
            controller.setParameters(tableController);
            controller.requestMouse();
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

}
