package mara.mybox.db.data;

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
import mara.mybox.db.table.TableDataDefinition;
import mara.mybox.dev.MyBoxLog;
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
        String sql = "SELECT * FROM Data_Definition WHERE data_type="
                + DataDefinition.dataType(DataDefinition.DataType.DataClipboard);
        int count = 0;
        try ( Connection conn = DerbyBase.getConnection()) {
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
            conn.commit();
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

    public static DataDefinition createData(TableDataDefinition tableDataDefinition, List<String> colsNames, String[][] data) {
        try {
            if (tableDataDefinition == null || data == null || data.length == 0) {
                return null;
            }
            File tmpFile = TmpFileTools.getTempFile();
            try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, Charset.forName("UTF-8")), CSVFormat.DEFAULT)) {
                if (colsNames != null) {
                    csvPrinter.printRecord(colsNames);
                }
                for (String[] r : data) {
                    csvPrinter.printRecord(Arrays.asList(r));
                }
            } catch (Exception e) {
                MyBoxLog.error(e);
                return null;
            }
            if (tmpFile == null || !tmpFile.exists()) {
                return null;
            }
            return createFile(tableDataDefinition, tmpFile, colsNames != null);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataDefinition createFile(TableDataDefinition tableDataDefinition, File csvFile, boolean withNames) {
        try {
            if (tableDataDefinition == null || csvFile == null || !csvFile.exists()) {
                return null;
            }
            File dpFile = new File(AppPaths.getDataClipboardPath() + File.separator + (new Date()).getTime() + ".csv");
            if (!FileTools.rename(csvFile, dpFile)) {
                return null;
            }
            DataDefinition df = DataDefinition.create()
                    .setDataType(DataType.DataClipboard).setHasHeader(withNames)
                    .setCharset("UTF-8").setDelimiter(",")
                    .setDataName(dpFile.getAbsolutePath());
            return tableDataDefinition.writeData(df);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
