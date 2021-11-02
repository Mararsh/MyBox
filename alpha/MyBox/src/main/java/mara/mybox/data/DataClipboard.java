package mara.mybox.data;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.table.TableData2DColumn;
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppPaths;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2021-8-25
 * @License Apache License Version 2.0
 */
public class DataClipboard extends DataDefinition {

    /*
        static methods
     */
    public static int checkValid(TableDataDefinition tableDataDefinition) {
        if (tableDataDefinition == null) {
            return -1;
        }
        int count = 0;
        try ( Connection conn = DerbyBase.getConnection()) {
            count = checkValid(conn, tableDataDefinition);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return count;
    }

    public static int checkValid(Connection conn, TableDataDefinition tableDataDefinition) {
        if (tableDataDefinition == null) {
            return -1;
        }
        String sql = "SELECT * FROM Data_Definition WHERE data_type="
                + DataDefinition.dataType(DataDefinition.DataType.DataClipboard);
        int count = 0;
        try {
            conn.setAutoCommit(true);
            List<DataDefinition> invalid = new ArrayList<>();
            try ( PreparedStatement statement = conn.prepareStatement(sql);
                     ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    DataDefinition data = tableDataDefinition.readData(results);
                    try {
                        File file = new File(data.getDataName());
                        if (file.exists()) {
                            count++;
                        } else {
                            invalid.add(data);
                        }
                    } catch (Exception e) {
                        invalid.add(data);
                    }
                }
            }
            tableDataDefinition.deleteData(conn, invalid);
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return count;
    }

    public static int size(TableDataDefinition tableDataDefinition) {
        if (tableDataDefinition == null) {
            return -1;
        }
        String condition = "data_type=" + DataDefinition.dataType(DataDefinition.DataType.DataClipboard);
        return tableDataDefinition.conditionSize(condition);
    }

    public static List<DataDefinition> queryPage(TableDataDefinition tableDataDefinition, int start, int size) {
        if (tableDataDefinition == null) {
            return null;
        }
        String condition = "data_type=" + DataDefinition.dataType(DataDefinition.DataType.DataClipboard)
                + " ORDER BY dfid DESC ";
        return tableDataDefinition.queryConditions(condition, start, size);
    }

    public static DataDefinition lastItem(TableDataDefinition tableDataDefinition) {
        if (tableDataDefinition == null) {
            return null;
        }
        String sql = "SELECT * FROM Data_Definition WHERE data_type="
                + DataDefinition.dataType(DataDefinition.DataType.DataClipboard)
                + " ORDER BY dfid DESC";
        return tableDataDefinition.queryOne(sql);
    }

    public static List<List<String>> lastData(TableDataDefinition tableDataDefinition, int maxRow, int maxCol) {
        try {
            DataDefinition item = lastItem(tableDataDefinition);
            if (item == null) {
                return null;
            }
            List<List<String>> data = new ArrayList<>();
            CSVFormat csvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withTrim().withNullString("")
                    .withDelimiter(item.getDelimiter().charAt(0));
            try ( CSVParser parser = CSVParser.parse(new File(item.getDataName()), Charset.forName(item.getCharset()), csvFormat)) {
                for (CSVRecord record : parser) {
                    List<String> row = new ArrayList<>();
                    for (int i = 0; i < Math.min(record.size(), maxCol); i++) {
                        row.add(record.get(i));
                    }
                    data.add(row);
                    if (data.size() >= maxRow) {
                        break;
                    }
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
            }
            return data;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static File writeFile(String[][] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, Charset.forName("UTF-8")), CSVFormat.DEFAULT)) {
            for (String[] r : data) {
                csvPrinter.printRecord(Arrays.asList(r));
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
    }

    public static DataDefinition create(TableDataDefinition tableDataDefinition, TableData2DColumn tableData2DColumn,
            File csvFile, List<ColumnDefinition> columns) {
        if (tableDataDefinition == null || tableData2DColumn == null || csvFile == null || !csvFile.exists()) {
            return null;
        }
        File dpFile = new File(AppPaths.getDataClipboardPath() + File.separator + (new Date()).getTime() + ".csv");
        if (!FileTools.rename(csvFile, dpFile)) {
            return null;
        }
        DataDefinition def = null;
        try ( Connection conn = DerbyBase.getConnection()) {
//            tableDataDefinition.deleteClipboard(conn, csvFile);
            def = DataDefinition.create()
                    .setDataType(DataType.DataClipboard)
                    .setFile(csvFile).setDataName(FileNameTools.getFilePrefix(csvFile.getName()))
                    .setHasHeader(false).setCharset("UTF-8").setDelimiter(",");
            tableDataDefinition.insertData(conn, def);
//            if (columns != null && !columns.isEmpty()) {
//                StringTable validateTable = ColumnDefinition.validate(columns);
//                if (validateTable != null) {
//                    if (validateTable.isEmpty()) {
////                        tableDataColumn.save(conn, def.getDfid(), columns);
//                        conn.commit();
//                    } else {
//                        Platform.runLater(() -> {
//                            validateTable.htmlTable();
//                        });
//                    }
//                }
//            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return def;
    }

}
