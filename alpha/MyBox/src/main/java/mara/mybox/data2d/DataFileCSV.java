package mara.mybox.data2d;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import mara.mybox.controller.BaseController;
import mara.mybox.controller.DataFileCSVController;
import mara.mybox.controller.DataFileExcelController;
import mara.mybox.controller.DataInMyBoxClipboardController;
import mara.mybox.controller.DataTablesController;
import mara.mybox.controller.MatricesManageController;
import mara.mybox.data.FindReplaceString;
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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class DataFileCSV extends DataFile {

    public DataFileCSV() {
        type = Type.CSV;
    }

    public DataFileCSV(File file) {
        type = Type.CSV;
        this.file = file;
        this.delimiter = guessDelimiter();
    }

    public String[] delimters() {
        String[] delimiters = {",", " ", "    ", "        ", "\t", "|", "@",
            "#", ";", ":", "*", "%", "$", "_", "&", "-", "=", "!", "\"",
            "'", "<", ">"};
        return delimiters;
    }

    public final String guessDelimiter() {
        if (file == null) {
            return null;
        }
        String[] delimiters = delimters();
        if (charset == null) {
            charset = TextFileTools.charset(file);
        }
        if (charset == null) {
            charset = Charset.forName("UTF-8");
        }
        File validFile = FileTools.removeBOM(file);
        try ( BufferedReader reader = new BufferedReader(new FileReader(validFile, charset))) {
            String line1 = reader.readLine();
            if (line1 == null) {
                return null;
            }
            int[] count1 = new int[delimiters.length];
            int maxCount1 = 0, maxCountIndex1 = -1;
            for (int i = 0; i < delimiters.length; i++) {
                count1[i] = FindReplaceString.count(line1, delimiters[i]);
//                MyBoxLog.console(">>" + values[i] + "<<<   " + count1[i]);
                if (count1[i] > maxCount1) {
                    maxCount1 = count1[i];
                    maxCountIndex1 = i;
                }
            }
//            MyBoxLog.console(maxCount1);
            String line2 = reader.readLine();
            if (line2 == null) {
                if (maxCountIndex1 >= 0) {
                    return delimiters[maxCountIndex1];
                } else {
                    hasHeader = false;
                    return null;
                }
            }
            int[] count2 = new int[delimiters.length];
            int maxCount2 = 0, maxCountIndex2 = -1;
            for (int i = 0; i < delimiters.length; i++) {
                count2[i] = FindReplaceString.count(line2, delimiters[i]);
//                MyBoxLog.console(">>" + values[i] + "<<<   " + count1[i]);
                if (count1[i] == count2[i] && count2[i] > maxCount2) {
                    maxCount2 = count2[i];
                    maxCountIndex2 = i;
                }
            }
//            MyBoxLog.console(maxCount2);
            if (maxCountIndex2 >= 0) {
                return delimiters[maxCountIndex2];
            } else {
                if (maxCountIndex1 >= 0) {
                    return delimiters[maxCountIndex1];
                } else {
                    hasHeader = false;
                    return null;
                }
            }
        } catch (Exception e) {
//            MyBoxLog.console(e.toString());
        }
        hasHeader = false;
        return null;
    }

    public CSVFormat cvsFormat() {
        if (delimiter == null || delimiter.isEmpty()) {
            delimiter = ",";
        }
        return CsvTools.csvFormat(delimiter, hasHeader);
    }

    public void setOptions(boolean hasHeader, Charset charset, String delimiter) {
        options = new HashMap<>();
        options.put("hasHeader", hasHeader);
        options.put("charset", charset);
        options.put("delimiter", delimiter);
    }

    @Override
    public void applyOptions() {
        try {
            if (options == null) {
                return;
            }
            if (options.containsKey("hasHeader")) {
                hasHeader = (boolean) (options.get("hasHeader"));
            }
            if (options.containsKey("charset")) {
                charset = (Charset) (options.get("charset"));
            }
            if (options.containsKey("delimiter")) {
                delimiter = (String) (options.get("delimiter"));
            }
        } catch (Exception e) {
        }
    }

    @Override
    public boolean checkForLoad() {
        if (charset == null && file != null) {
            charset = TextFileTools.charset(file);
        }
        if (charset == null) {
            charset = Charset.forName("UTF-8");
        }
        if (delimiter == null || delimiter.isEmpty()) {
            delimiter = guessDelimiter();
        }
        if (delimiter == null || delimiter.isEmpty()) {
            delimiter = ",";
        }
        return super.checkForLoad();
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

    public File tmpFile(String dname, List<String> cols, List<List<String>> data) {
        if (cols == null || cols.isEmpty()) {
            if (data == null || data.isEmpty()) {
                return null;
            }
        }
        File tmpFile = tmpFile(dname, "tmp", ".csv");
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

    public DataFileCSV savePageAs(String dname) {
        try {
            DataFileCSV targetData = (DataFileCSV) this.cloneAll();
            File csvFile = tmpFile(dname, "save", ".csv");
            targetData.setFile(csvFile).setDataName(dname);
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
    public static DataFileCSV tmpCSV(String dname) {
        try {
            DataFileCSV dataFileCSV = new DataFileCSV();
            File csvFile = dataFileCSV.tmpFile(dname, "tmp", ".csv");
            dataFileCSV.setFile(csvFile).setDataName(dname)
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

    public static DataFileCSV save(String dname, SingletonTask task, List<Data2DColumn> cols, List<List<String>> data) {
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
            File file = dataFileCSV.tmpFile(dname, names, data);
            dataFileCSV.setColumns(targetColumns)
                    .setFile(file)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(",")
                    .setHasHeader(!targetColumns.isEmpty())
                    .setColsNumber(targetColumns.size())
                    .setRowsNumber(data.size());
            dataFileCSV.saveAttributes();
            dataFileCSV.stopTask();
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
        if ("csv".equals(target) || "texts".equals(target)) {
            DataFileCSVController.loadCSV(csvFile);
        } else if ("excel".equals(target)) {
            DataFileExcelController.loadCSV(csvFile);
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
        if ("csv".equals(target) || "texts".equals(target)) {
            DataFileCSVController.loadTable(dataTable);
        } else if ("excel".equals(target)) {
            DataFileExcelController.loadTable(dataTable);
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
