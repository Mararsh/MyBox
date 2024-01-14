package mara.mybox.data2d;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextTools;
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
        return CsvTools.csvFormat(delimiter, hasHeader);
    }

    @Override
    public boolean savePageData(Data2D targetData) {
        if (targetData == null || !(targetData instanceof DataFileCSV)) {
            return false;
        }
        DataFileCSV targetCSVFile = (DataFileCSV) targetData;
        File tmpFile = FileTmpTools.getTempFile();
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
            File validFile = FileTools.removeBOM(task, file);
            if (validFile == null || (task != null && !task.isWorking())) {
                return false;
            }
            try (CSVParser parser = CSVParser.parse(validFile, charset, cvsFormat());
                    CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, tCharset), tFormat)) {
                if (tHasHeader) {
                    writeHeader(csvPrinter);
                }
                long index = -1;
                Iterator<CSVRecord> iterator = parser.iterator();
                if (iterator != null) {
                    while (iterator.hasNext() && task != null && task.isWorking()) {
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
            try (CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, tCharset), tFormat)) {
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
        return FileTools.override(tmpFile, tFile);
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
                csvPrinter.printRecord(tableRow(r, false, false));
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

    public DataFileCSV savePageAs(String dname) {
        try {
            DataFileCSV targetData = (DataFileCSV) this.cloneAll();
            File csvFile = tmpFile(dname, "save", "csv");
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
            File csvFile = dataFileCSV.tmpFile(dname, "tmp", "csv");
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

    public static File csvFile(FxTask task, File tmpFile,
            String delimeter, List<String> cols, List<List<String>> data) {
        if (tmpFile == null) {
            return null;
        }
        if (cols == null || cols.isEmpty()) {
            if (data == null || data.isEmpty()) {
                return null;
            }
        }
        if (delimeter == null || delimeter.isEmpty()) {
            delimeter = ",";
        }
        boolean hasHeader = cols != null && !cols.isEmpty();
        try (CSVPrinter csvPrinter = CsvTools.csvPrinter(tmpFile, delimeter, hasHeader)) {
            if (hasHeader) {
                csvPrinter.printRecord(cols);
            }
            if (data != null) {
                for (int r = 0; r < data.size(); r++) {
                    if (task != null && task.isCancelled()) {
                        csvPrinter.close();
                        return null;
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

    public static DataFileCSV save(String dname, FxTask task, String delimeter,
            List<Data2DColumn> cols, List<List<String>> data) {
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
            File tmpFile = dataFileCSV.tmpFile(dname, null, "csv");
            tmpFile = csvFile(task, tmpFile, delimeter, names, data);
            if (tmpFile == null) {
                return null;
            }
            dataFileCSV.setColumns(targetColumns)
                    .setFile(tmpFile)
                    .setCharset(Charset.forName("UTF-8"))
                    .setDelimiter(delimeter)
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

    public static LinkedHashMap<File, Boolean> save(FxTask task, File path,
            String filePrefix, List<StringTable> tables) {
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
                if (tableData == null || tableData.isEmpty()
                        || (task != null && !task.isWorking())) {
                    continue;
                }
                data = TextTools.toArray(task, tableData);
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
                try (CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(csvFile, Charset.forName("UTF-8")), csvFormat)) {
                    if (withName) {
                        csvPrinter.printRecord(names);
                    }
                    for (int r = 0; r < data.length; r++) {
                        if (task != null && !task.isWorking()) {
                            csvPrinter.close();
                            return null;
                        }
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

}
