package mara.mybox.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.DataDefinition.DataType;
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

    protected String textDelimiterName;

    @Override
    public boolean readDataDefinition(SingletonTask<Void> task) {
        try ( Connection conn = DerbyBase.getConnection()) {
            definition = tableDataDefinition.queryFile(conn, file);
            if (userSavedDataDefinition) {
                if (definition != null) {
                    sourceWithNames = definition.isHasHeader();
                    sourceCharset = Charset.forName(definition.getCharset());
                    textDelimiterName = definition.getDelimiter();
                } else {
                    sourceWithNames = true;
                    sourceCharset = TextFileTools.charset(file);
                    textDelimiterName = guessDelimiter();
                }
            }
            if (sourceCharset == null) {
                sourceCharset = Charset.defaultCharset();
            }
            if (textDelimiterName == null) {
                textDelimiterName = ",";
            }
            if (definition == null) {
                definition = DataDefinition.create()
                        .setDataType(DataType.DataFile).setFile(file)
                        .setCharset(sourceCharset.name())
                        .setHasHeader(sourceWithNames)
                        .setDelimiter(textDelimiterName);
                tableDataDefinition.insertData(conn, definition);
                conn.commit();
            } else {
                if (!userSavedDataDefinition) {
                    definition.setCharset(sourceCharset.name())
                            .setHasHeader(sourceWithNames)
                            .setDelimiter(textDelimiterName);
                    tableDataDefinition.updateData(conn, definition);
                    conn.commit();
                }
                savedColumns = tableDataColumn.read(conn, definition.getDfid());
            }
            userSavedDataDefinition = true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return false;
        }
        return definition != null && definition.getDfid() >= 0;
    }

    @Override
    public boolean readColumns(SingletonTask<Void> task) {
        columns = new ArrayList<>();
        if (!sourceWithNames) {
            return true;
        }
        if (definition == null) {
            return false;
        }
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, sourceCharset))) {
            List<String> names = readNames(task, reader);
            if (names == null || names.isEmpty()) {
                sourceWithNames = false;
                return true;
            }
            for (String name : names) {
                boolean found = false;
                if (savedColumns != null) {
                    for (ColumnDefinition def : savedColumns) {
                        if (def.getName().equals(name)) {
                            columns.add(def);
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    ColumnDefinition column = new ColumnDefinition(name, ColumnDefinition.ColumnType.String);
                    columns.add(column);
                }
            }
            if (columns != null && !columns.isEmpty()) {
                StringTable validateTable = ColumnDefinition.validate(columns);
                if (validateTable == null || validateTable.isEmpty()) {
                    tableDataColumn.save(definition.getDfid(), columns);
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
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, sourceCharset))) {
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
        if (sourceWithNames && dataNumber > 0) {
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
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, sourceCharset))) {
            if (sourceWithNames) {
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
            loadPageData(fileRows, sourceWithNames ? columns.size() : maxCol);
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
        return TextTools.parseLine(line, definition.getDelimiter());
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
