package mara.mybox.data;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppPaths;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2021-8-25
 * @License Apache License Version 2.0
 */
public class DataClipboard extends DataFileText {

    public DataClipboard() {
        type = Type.DataClipboard;
    }

    public int type() {
        return type(Data2DDefinition.Type.DataClipboard);
    }

    public File newFile() {
        return new File(AppPaths.getDataClipboardPath() + File.separator + (new Date()).getTime() + ".txt");
    }

    public File systemClipboardFile() {
        return new File(AppPaths.getDataClipboardPath() + File.separator + "SystemClipboard.txt");
    }

    @Override
    public boolean isTmpFile() {
        if (file == null) {
            return true;
        }
        if (super.isTmpFile()) {
            return true;
        }
        return file.getAbsolutePath().equals(systemClipboardFile().getAbsolutePath());
    }

    public int checkValid() {
        if (tableData2DDefinition == null) {
            return -1;
        }
        String sql = "SELECT * FROM Data2D_Definition WHERE data_type=" + type();
        int count = 0;
        try ( Connection conn = DerbyBase.getConnection()) {
            conn.setAutoCommit(true);
            List<Data2DDefinition> invalid = new ArrayList<>();
            try ( PreparedStatement statement = conn.prepareStatement(sql);
                     ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    Data2DDefinition data = tableData2DDefinition.readData(results);
                    try {
                        File dfile = new File(data.getDataName());
                        if (dfile.exists()) {
                            count++;
                        } else {
                            invalid.add(data);
                        }
                    } catch (Exception e) {
                        invalid.add(data);
                    }
                }
            }
            tableData2DDefinition.deleteData(conn, invalid);
            conn.setAutoCommit(true);
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
        return count;
    }

    public Data2DDefinition lastItem() {
        if (tableData2DDefinition == null) {
            return null;
        }
        String sql = "SELECT * FROM Data_Definition WHERE data_type="
                + type() + " ORDER BY d2did DESC";
        return tableData2DDefinition.queryOne(sql);
    }

    public List<List<String>> lastData(int maxRow, int maxCol) {
        try {
            Data2DDefinition item = lastItem();
            if (item == null) {
                return null;
            }
            List<List<String>> data = new ArrayList<>();
            CSVFormat csvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withTrim().withNullString("")
                    .withDelimiter(item.getDelimiter().charAt(0));
            try ( CSVParser parser = CSVParser.parse(new File(item.getDataName()), item.getCharset(), csvFormat)) {
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

}
