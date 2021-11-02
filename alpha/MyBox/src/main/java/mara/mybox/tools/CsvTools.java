package mara.mybox.tools;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import mara.mybox.data.StringTable;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.dev.MyBoxLog;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2021-10-11
 * @License Apache License Version 2.0
 */
public class CsvTools {

    public static boolean save(TableDataDefinition tableDataDefinition, TableData2DColumn tableDataColumn,
            File file, String[][] data, List<ColumnDefinition> dataColumns) {
        if (file == null || data == null) {
            return false;
        }
        CSVFormat csvFormat = CSVFormat.DEFAULT
                .withDelimiter(',')
                .withIgnoreEmptyLines().withTrim().withNullString("");
        List<String> names = null;
        if (dataColumns != null && !dataColumns.isEmpty()) {
            names = new ArrayList<>();
            for (ColumnDefinition col : dataColumns) {
                names.add(col.getName());
            }
            csvFormat = csvFormat.withFirstRecordAsHeader();
        }
        boolean withName = names != null && !names.isEmpty();
        try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(file, Charset.forName("UTF-8")), csvFormat)) {
            if (withName) {
                csvPrinter.printRecord(names);
            }
            for (int r = 0; r < data.length; r++) {
                csvPrinter.printRecord(Arrays.asList(data[r]));
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
            return false;
        }
        if (names != null) {
//            DataDefinition.saveDefinition(tableDataDefinition, tableDataColumn,
//                    file.getAbsolutePath(), DataDefinition.DataType.DataFile,
//                    Charset.forName("UTF-8"), ",", true, dataColumns);
        }
        return true;
    }

    public static LinkedHashMap<File, Boolean> save(File path, String filePrefix, List<StringTable> tables) {
        if (tables == null || tables.isEmpty()) {
            return null;
        }
        try {
            LinkedHashMap<File, Boolean> files = new LinkedHashMap<>();
            String[][] data;
            int count = 1;
            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .withDelimiter(',')
                    .withIgnoreEmptyLines().withTrim().withNullString("");
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

}
