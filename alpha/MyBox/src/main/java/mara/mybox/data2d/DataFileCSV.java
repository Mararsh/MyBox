package mara.mybox.data2d;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.writer.Data2DWriter;
import mara.mybox.data2d.writer.DataFileCSVWriter;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxTask;
import mara.mybox.tools.CsvTools;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.TextTools;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class DataFileCSV extends DataFileText {

    public DataFileCSV() {
        dataType = DataType.CSV;
    }

    @Override
    public String[] delimters() {
        String[] delimiters = {",", " ", "|", "@", ";", ":", "*",
            "%", "$", "_", "&", "-", "=", "!", "\"", "'", "<", ">", "#"};
        return delimiters;
    }

    public CSVFormat cvsFormat() {
        if (delimiter == null || delimiter.isEmpty()) {
            delimiter = ",";
        }
        return CsvTools.csvFormat(delimiter, hasHeader);
    }

    @Override
    public Data2DWriter selfWriter() {
        DataFileCSVWriter writer = new DataFileCSVWriter();
        writer.setCharset(charset)
                .setDelimiter(delimiter)
                .setWriteHeader(hasHeader)
                .setTargetData(this)
                .setPrintFile(file)
                .setColumns(columns)
                .setHeaderNames(columnNames())
                .setRecordTargetFile(true)
                .setRecordTargetData(true);
        return writer;
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

    public static DataFileCSV save(FxTask task, File dfile, String dname, String delimeter,
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
            if (dfile == null) {
                dfile = FileTmpTools.getTempFile(".csv");
            }
            dfile = csvFile(task, dfile, delimeter, names, data);
            if (dfile == null) {
                return null;
            }
            dataFileCSV.setColumns(targetColumns)
                    .setFile(dfile)
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
