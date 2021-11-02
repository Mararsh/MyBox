package mara.mybox.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2Column;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
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
    public boolean readDataDefinition(SingletonTask<Void> task) {
        d2did = -1;
        if (file == null) {
            return false;
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
            return false;
        }
        return d2did >= 0;
    }

    @Override
    public boolean readColumns(SingletonTask<Void> task) {
        columns = new ArrayList<>();
        if (d2did < 0) {
            return false;
        }
        if (!hasHeader) {
            return true;
        }
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            List<String> names = readNames(task, reader);
            if (names == null || names.isEmpty()) {
                hasHeader = false;
                return true;
            }
            for (String name : names) {
                boolean found = false;
                if (savedColumns != null) {
                    for (Data2Column def : savedColumns) {
                        if (def.getName().equals(name)) {
                            columns.add(def);
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    Data2Column column = new Data2Column(name, Data2Column.ColumnType.String);
                    columns.add(column);
                }
            }
            if (columns != null && !columns.isEmpty()) {
                StringTable validateTable = Data2Column.validate(columns);
                if (validateTable == null || validateTable.isEmpty()) {
                    tableData2DColumn.save(d2did, columns);
                    return true;
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean readTotal(SingletonTask<Void> task) {
        dataNumber = 0;
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (task == null || task.isCancelled()) {
                    dataNumber = 0;
                    break;
                }
                List<String> row = parseFileLine(line);
                if (row != null && !row.isEmpty()) {
                    dataNumber++;
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return false;
        }
        if (hasHeader && dataNumber > 0) {
            dataNumber--;
        }
        return true;
    }

    @Override
    public boolean readPageData(SingletonTask<Void> task) {
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        pageData = null;
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            if (hasHeader) {
                readNames(task, reader);
            }
            String line;
            int rowIndex = -1, maxCol = 0;
            long end = startRowOfCurrentPage + pageSize;
            List<List<String>> fileRows = new ArrayList<>();
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
                fileRows.add(row);
                if (maxCol < row.size()) {
                    maxCol = row.size();
                }
            }
            loadPageData(fileRows, hasHeader ? columns.size() : maxCol);
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return false;
        }
        return true;
    }

    protected List<String> parseFileLine(String line) {
        return TextTools.parseLine(line, delimiter);
    }

    protected List<String> readNames(SingletonTask<Void> task, BufferedReader reader) {
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
