package mara.mybox.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
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
        savedColumns = null;
        if (file == null) {
            return -1;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            Data2DDefinition definition = tableData2DDefinition.queryFile(conn, type, file);
            if (userSavedDataDefinition) {
                if (definition != null) {
                    load(definition);
                } else {
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
            dataName = file.getName();
            if (definition == null) {
                definition = tableData2DDefinition.insertData(conn, this);
                conn.commit();
                d2did = definition.getD2did();
            } else {
                tableData2DDefinition.updateData(conn, this);
                conn.commit();
                d2did = definition.getD2did();
                savedColumns = tableData2DColumn.read(conn, d2did);
            }
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
    public List<String> readColumns() {
        List<String> names = null;
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            names = readNames(reader);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
        }
        if (names == null) {
            hasHeader = false;
        }
        return names;
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
        endRowOfCurrentPage = startRowOfCurrentPage;
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
            endRowOfCurrentPage = startRowOfCurrentPage + rows.size();
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
