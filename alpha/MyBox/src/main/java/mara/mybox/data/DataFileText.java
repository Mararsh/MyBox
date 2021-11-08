package mara.mybox.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class DataFileText extends DataFile {

    public DataFileText() {
        type = Type.DataFileText;
    }

    @Override
    public long readDataDefinition() {
        d2did = -1;
        if (file == null) {
            return -1;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            Data2DDefinition definition = tableData2DDefinition.queryFile(conn, type, file);
            if (userSavedDataDefinition) {
                if (definition != null) {
                    hasHeader = definition.isHasHeader();
                    charset = definition.getCharset();
                    delimiter = definition.getDelimiter();
                } else {
                    hasHeader = true;
                    charset = TextFileTools.charset(file);
                    delimiter = guessDelimiter();
                }
            }
            if (charset == null) {
                charset = Charset.defaultCharset();
            }
            if (delimiter == null) {
                delimiter = ",";
            }
            if (definition == null) {
                definition = Data2DDefinition.create()
                        .setType(type).setFile(file)
                        .setCharset(charset)
                        .setHasHeader(hasHeader)
                        .setDelimiter(delimiter);
                definition = tableData2DDefinition.insertData(conn, definition);
                conn.commit();
            } else {
                if (!userSavedDataDefinition) {
                    definition.setCharset(charset)
                            .setHasHeader(hasHeader)
                            .setDelimiter(delimiter);
                    definition = tableData2DDefinition.updateData(conn, definition);
                    conn.commit();
                }
                savedColumns = tableData2DColumn.read(conn, definition.getD2did());
            }
            d2did = definition.getD2did();
            userSavedDataDefinition = true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return -1;
        }
        return d2did;
    }

    @Override
    public List<Data2DColumn> readColumns() {
        List<String> names = null;
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            names = readNames(reader);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
        }
        if (names != null) {
            List<Data2DColumn> fileColumns = new ArrayList<>();
            for (String name : names) {
                Data2DColumn column = new Data2DColumn(name, Data2DColumn.ColumnType.String);
                fileColumns.add(column);
            }
            return fileColumns;
        } else {
            hasHeader = false;
            return null;
        }
    }

    @Override
    public long readTotal() {
        dataSize = 0;
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (backgroundTask == null || backgroundTask.isCancelled()) {
                    dataSize = 0;
                    break;
                }
                List<String> row = parseFileLine(line);
                if (row != null && !row.isEmpty()) {
                    dataSize++;
                }
            }
        } catch (Exception e) {
            if (backgroundTask != null) {
                backgroundTask.setError(e.toString());
            }
            MyBoxLog.console(e);
            return -1;
        }
        if (hasHeader && dataSize > 0) {
            dataSize--;
        }
        return dataSize;
    }

    @Override
    public List<List<String>> readPageData() {
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            if (hasHeader) {
                readNames(reader);
            }
            String line;
            long rowIndex = -1;
            long end = startRowOfCurrentPage + pageSize;
            List<List<String>> rows = new ArrayList<>();
            int columnsNumber = columnsNumber();
            while ((line = reader.readLine()) != null && task != null && !task.isCancelled()) {
                List<String> row = parseFileLine(line);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                if (++rowIndex < startRowOfCurrentPage) {
                    continue;
                }
                if (rowIndex >= end) {
                    break;
                }
                for (int col = row.size(); col < columnsNumber; col++) {
                    row.add(defaultColValue());
                }
                rows.add(row);
            }
            return rows;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return null;
        }
    }

    protected List<String> parseFileLine(String line) {
        return TextTools.parseLine(line, delimiter);
    }

    protected List<String> readNames(BufferedReader reader) {
        List<String> names = null;
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                names = parseFileLine(line);
                if (names != null && !names.isEmpty()) {
                    break;
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
        }
        return names;
    }

    /*
        get/set
     */
}
