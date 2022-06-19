package mara.mybox.data2d;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.DataFileCSVController;
import mara.mybox.controller.DataFileExcelController;
import mara.mybox.controller.DataFileTextController;
import mara.mybox.controller.DataInMyBoxClipboardController;
import mara.mybox.controller.DataTablesController;
import mara.mybox.controller.MatricesManageController;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.TextClipboardTools;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppPaths;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class DataFileCSV extends DataFileText {

    public DataFileCSV() {
        type = Type.CSV;
    }

    public DataFileCSV(File file) {
        type = Type.CSV;
        this.file = file;
        this.delimiter = guessDelimiter();
    }

    @Override
    public String[] delimters() {
        String[] delimiters = {",", " ", "|", "@", "#", ";", ":", "*",
            "%", "$", "_", "&", "-", "=", "!", "\"", "'", "<", ">"};
        return delimiters;
    }

    public CSVFormat cvsFormat() {
        if (delimiter == null || delimiter.isEmpty()) {
            delimiter = ",";
        }
        return CsvTools.csvFormat(delimiter.charAt(0), hasHeader);
    }

    @Override
    public boolean savePageData(Data2D targetData) {
        if (targetData == null || !(targetData instanceof DataFileCSV)) {
            return false;
        }
        DataFileCSV targetCSVFile = (DataFileCSV) targetData;
        File tmpFile = TmpFileTools.getTempFile();
        File tFile = targetCSVFile.getFile();
        if (tFile == null) {
            return false;
        }
        targetCSVFile.checkForLoad();
        Charset tCharset = targetCSVFile.getCharset();
        boolean tHasHeader = targetCSVFile.isHasHeader();
        CSVFormat tFormat = targetCSVFile.cvsFormat();
        checkForLoad();
        if (file != null && file.exists() && file.length() > 0) {
            File validFile = FileTools.removeBOM(file);
            try ( CSVParser parser = CSVParser.parse(validFile, charset, cvsFormat());
                     CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, tCharset), tFormat)) {
                if (tHasHeader) {
                    writeHeader(csvPrinter);
                }
                long index = -1;
                Iterator<CSVRecord> iterator = parser.iterator();
                if (iterator != null) {
                    while (iterator.hasNext() && task != null && !task.isCancelled()) {
                        try {
                            CSVRecord record = iterator.next();
                            if (record != null) {
                                if (++index < startRowOfCurrentPage || index >= endRowOfCurrentPage) {
                                    writeFileRow(csvPrinter, record);
                                } else if (index == startRowOfCurrentPage) {
                                    if (!writePageData(csvPrinter)) {
                                        return false;
                                    }
                                }
                            }
                        } catch (Exception e) {  // skip  bad lines
//                            MyBoxLog.debug(e);
                        }
                    }
                }
                if (index < 0) {
                    if (!writePageData(csvPrinter)) {
                        return false;
                    }
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }
        } else {
            try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, tCharset), tFormat)) {
                if (tHasHeader) {
                    writeHeader(csvPrinter);
                }
                if (!writePageData(csvPrinter)) {
                    return false;
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }
        }
        return FileTools.rename(tmpFile, tFile, false);
    }

    public boolean writeHeader(CSVPrinter csvPrinter) {
        try {
            if (csvPrinter == null) {
                return false;
            }
            if (!isColumnsValid()) {
                return true;
            }
            List<String> names = columnNames();
            if (names != null) {
                csvPrinter.printRecord(columnNames());
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    public boolean writePageData(CSVPrinter csvPrinter) {
        try {
            if (csvPrinter == null) {
                return false;
            }
            if (!isColumnsValid()) {
                return true;
            }
            for (int r = 0; r < tableRowsNumber(); r++) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                csvPrinter.printRecord(tableRowWithoutNumber(r));
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.error(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    public void writeFileRow(CSVPrinter csvPrinter, CSVRecord record) {
        try {
            List<String> row = new ArrayList<>();
            for (String v : record) {
                row.add(v);
            }
            csvPrinter.printRecord(fileRow(row));
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public File tmpFile(List<String> cols, List<List<String>> data) {
        if (cols == null || cols.isEmpty()) {
            if (data == null || data.isEmpty()) {
                return null;
            }
        }
        File tmpFile = TmpFileTools.csvFile();
        try ( CSVPrinter csvPrinter = CsvTools.csvPrinter(tmpFile)) {
            if (cols != null && !cols.isEmpty()) {
                csvPrinter.printRecord(cols);
            }
            if (data != null) {
                for (int r = 0; r < data.size(); r++) {
                    if (task != null && task.isCancelled()) {
                        break;
                    }
                    csvPrinter.printRecord(data.get(r));
                }
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
        return tmpFile;
    }

    @Override
    public boolean setValue(List<Integer> cols, String value, boolean errorContinue) {
        if (file == null || !file.exists() || file.length() == 0 || cols == null || cols.isEmpty()) {
            return false;
        }
        File tmpFile = TmpFileTools.getTempFile();
        CSVFormat format = cvsFormat();
        File validFile = FileTools.removeBOM(file);
        try ( CSVParser parser = CSVParser.parse(validFile, charset, format);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, charset), format)) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                if (hasHeader) {
                    try {
                        csvPrinter.printRecord(parser.getHeaderNames());
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
                boolean isRandom = false, isRandomNn = false, isBlank = false;
                String script = null;
                if (value != null) {
                    if ("MyBox##blank".equals(value)) {
                        isBlank = true;
                    } else if ("MyBox##random".equals(value)) {
                        isRandom = true;
                    } else if ("MyBox##randomNn".equals(value)) {
                        isRandomNn = true;
                    } else if (value.startsWith("MyBox##Expression##")) {
                        script = value.substring("MyBox##Expression##".length());
                    }
                }
                final Random random = new Random();
                rowIndex = 0;
                boolean needSetValue;
                startFilter();
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    try {
                        CSVRecord record = iterator.next();
                        if (record == null) {
                            continue;
                        }
                        List<String> values = record.toList();
                        filterDataRow(values, ++rowIndex);
                        needSetValue = filterPassed() && !filterReachMaxFilterPassed();
                        if (needSetValue && script != null) {
                            calculateDataRowExpression(script, values, rowIndex);
                            if (error != null) {
                                if (errorContinue) {
                                    continue;
                                } else {
                                    task.setError(error);
                                    return false;
                                }
                            }
                        }
                        List<String> row = new ArrayList<>();
                        for (int i = 0; i < columns.size(); i++) {
                            if (needSetValue && cols.contains(i)) {
                                if (isBlank) {
                                    row.add("");
                                } else if (isRandom) {
                                    row.add(random(random, i, false));
                                } else if (isRandomNn) {
                                    row.add(random(random, i, true));
                                } else if (script != null) {
                                    row.add(getExpressionResult());
                                } else {
                                    row.add(value);
                                }
                            } else if (i < record.size()) {
                                row.add(record.get(i));
                            } else {
                                row.add(null);
                            }
                        }
                        csvPrinter.printRecord(row);
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
        return FileTools.rename(tmpFile, file, false);
    }

    @Override
    public boolean delete(boolean errorContinue) {
        if (file == null || !file.exists() || file.length() == 0) {
            return false;
        }
        File tmpFile = TmpFileTools.getTempFile();
        CSVFormat format = cvsFormat();
        File validFile = FileTools.removeBOM(file);
        try ( CSVParser parser = CSVParser.parse(validFile, charset, format);
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, charset), format)) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                if (hasHeader) {
                    try {
                        csvPrinter.printRecord(parser.getHeaderNames());
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
                if (needFilter()) {
                    rowIndex = 0;
                    startFilter();
                    while (iterator.hasNext() && task != null && !task.isCancelled()) {
                        try {
                            CSVRecord record = iterator.next();
                            if (record == null) {
                                continue;
                            }
                            filterDataRow(record.toList(), ++rowIndex);
                            if (error != null) {
                                if (errorContinue) {
                                    continue;
                                } else {
                                    task.setError(error);
                                    return false;
                                }
                            }
                            if (filterPassed() && !filterReachMaxFilterPassed()) {
                                continue;
                            }
                            List<String> row = new ArrayList<>();
                            for (int i = 0; i < columns.size(); i++) {
                                if (i < record.size()) {
                                    row.add(record.get(i));
                                } else {
                                    row.add(null);
                                }
                            }
                            csvPrinter.printRecord(row);
                        } catch (Exception e) {  // skip  bad lines
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e);
            return false;
        }
        return FileTools.rename(tmpFile, file, false);
    }

    @Override
    public long clearData() {
        File tmpFile = TmpFileTools.getTempFile();
        CSVFormat cvsFormat = cvsFormat();
        checkForLoad();
        if (file != null && file.exists() && file.length() > 0) {
            try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, charset), cvsFormat)) {
                if (hasHeader) {
                    writeHeader(csvPrinter);
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return -1;
            }
            if (FileTools.rename(tmpFile, file, false)) {
                return getDataSize();
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

    public DataFileCSV savePageAs() {
        try {
            DataFileCSV targetData = (DataFileCSV) this.cloneAll();
            File csvFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), ".csv");
            targetData.setFile(csvFile);
            savePageData(targetData);
            return targetData;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    /*
        static
     */
    public static DataFileCSV tmpCSV() {
        try {
            File csvFile = TmpFileTools.getPathTempFile(AppPaths.getGeneratedPath(), ".csv");
            DataFileCSV dataFileCSV = new DataFileCSV();
            dataFileCSV.setFile(csvFile)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",")
                    .setHasHeader(true);
            return dataFileCSV;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static LinkedHashMap<File, Boolean> save(File path, String filePrefix, List<StringTable> tables) {
        if (tables == null || tables.isEmpty()) {
            return null;
        }
        try {
            LinkedHashMap<File, Boolean> files = new LinkedHashMap<>();
            String[][] data;
            int count = 1;
            CSVFormat csvFormat = CsvTools.csvFormat();
            for (StringTable stringTable : tables) {
                List<List<String>> tableData = stringTable.getData();
                if (tableData == null || tableData.isEmpty()) {
                    continue;
                }
                data = TextTools.toArray(tableData);
                if (data == null || data.length == 0) {
                    continue;
                }
                List<String> names = stringTable.getNames();
                boolean withName = names != null && !names.isEmpty();
                String title = stringTable.getTitle();
                File csvFile = new File(path + File.separator
                        + FileNameTools.filter((filePrefix == null || filePrefix.isBlank() ? "" : filePrefix + "_")
                                + (title == null || title.isBlank() ? "_" + count : title))
                        + ".csv");
                try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, Charset.forName("UTF-8")), csvFormat)) {
                    if (withName) {
                        csvPrinter.printRecord(names);
                    }
                    for (int r = 0; r < data.length; r++) {
                        csvPrinter.printRecord(Arrays.asList(data[r]));
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                }
                if (csvFile.exists()) {
                    files.put(csvFile, withName);
                    count++;
                }
            }
            return files;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }

    }

    public static DataFileCSV save(SingletonTask task, List<Data2DColumn> cols, List<List<String>> data) {
        try {
            if (cols == null || cols.isEmpty()) {
                if (data == null || data.isEmpty()) {
                    return null;
                }
            }
            List<Data2DColumn> targetColumns = new ArrayList<>();
            List<String> names = null;
            if (cols != null) {
                names = new ArrayList<>();
                for (Data2DColumn c : cols) {
                    names.add(c.getColumnName());
                    targetColumns.add(c.cloneAll().setD2cid(-1).setD2id(-1));
                }
            }
            DataFileCSV dataFileCSV = new DataFileCSV();
            dataFileCSV.setTask(task);
            File file = dataFileCSV.tmpFile(names, data);
            dataFileCSV.setColumns(targetColumns)
                    .setFile(file)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",")
                    .setHasHeader(!targetColumns.isEmpty())
                    .setColsNumber(targetColumns.size())
                    .setRowsNumber(data.size());
            dataFileCSV.saveAttributes();
            dataFileCSV.setTask(null);
            return dataFileCSV;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static void openCSV(BaseController controller, DataFileCSV csvFile, String target) {
        if (csvFile == null || target == null) {
            return;
        }
        if ("csv".equals(target)) {
            DataFileCSVController.loadCSV(csvFile);
        } else if ("excel".equals(target)) {
            DataFileExcelController.loadCSV(csvFile);
        } else if ("texts".equals(target)) {
            DataFileTextController.loadCSV(csvFile);
        } else if ("matrix".equals(target)) {
            MatricesManageController.loadCSV(csvFile);
        } else if ("systemClipboard".equals(target)) {
            TextClipboardTools.copyToSystemClipboard(controller, TextFileTools.readTexts(csvFile.getFile()));
        } else if ("myBoxClipboard".equals(target)) {
            DataInMyBoxClipboardController.loadCSV(csvFile);
        } else if ("table".equals(target)) {
            DataTablesController.loadCSV(csvFile);
        }
    }

    public static void openDataTable(BaseController controller, DataTable dataTable, String target) {
        if (dataTable == null || target == null) {
            return;
        }
        if ("csv".equals(target)) {
            DataFileCSVController.loadTable(dataTable);
        } else if ("excel".equals(target)) {
            DataFileExcelController.loadTable(dataTable);
        } else if ("texts".equals(target)) {
            DataFileTextController.loadTable(dataTable);
        } else if ("matrix".equals(target)) {
            MatricesManageController.loadTable(dataTable);
        } else if ("systemClipboard".equals(target)) {
            TextClipboardTools.copyToSystemClipboard(controller, DataTable.toString(null, dataTable));
        } else if ("myBoxClipboard".equals(target)) {
            DataInMyBoxClipboardController.loadTable(dataTable);
        } else if ("table".equals(target)) {
            DataTablesController.loadTable(dataTable);
        }
    }

}
