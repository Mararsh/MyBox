package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.tools.TextTools.delimiterValue;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-9-6
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetText_File extends ControlSheetFile {

    protected Charset sourceCharset, targetCharset;
    protected boolean targetWithNames;
    protected String targetDelimiterName;

    @Override
    protected boolean readDataDefinition() {
        try ( Connection conn = DerbyBase.getConnection()) {
            dataName = sourceFile.getAbsolutePath();
            dataDefinition = tableDataDefinition.read(conn, dataType, dataName);
            if (userSavedDataDefinition && dataDefinition != null) {
                sourceWithNames = dataDefinition.isHasHeader();
                sourceCharset = Charset.forName(dataDefinition.getCharset());
                sourceDelimiterName = dataDefinition.getDelimiter();
                editDelimiterController.setDelimiter(sourceDelimiterName);
            }
            if (sourceDelimiterName == null) {
                sourceDelimiterName = editDelimiterName;
            }
            if (dataDefinition == null) {
                dataDefinition = DataDefinition.create().setDataName(dataName).setDataType(dataType)
                        .setDelimiter(sourceDelimiterName)
                        .setCharset(sourceCharset.name())
                        .setHasHeader(sourceWithNames);
                tableDataDefinition.insertData(conn, dataDefinition);
                conn.commit();
            } else {
                if (!userSavedDataDefinition) {
                    dataDefinition.setCharset(sourceCharset.name())
                            .setDelimiter(sourceDelimiterName)
                            .setHasHeader(sourceWithNames);
                    tableDataDefinition.updateData(conn, dataDefinition);
                    conn.commit();
                }
                savedColumns = tableDataColumn.read(conn, dataDefinition.getDfid());
            }
            userSavedDataDefinition = true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return false;
        }
        return dataDefinition != null && dataDefinition.getDfid() >= 0;
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

    @Override
    protected boolean readColumns() {
        columns = new ArrayList<>();
        if (!sourceWithNames) {
            return true;
        }
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset))) {
            List<String> names = readNames(reader);
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
                if (validateColumns(columns)) {
                    tableDataColumn.save(dataDefinition.getDfid(), columns);
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
    protected boolean readTotal() {
        totalSize = 0;
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (backgroundTask == null || backgroundTask.isCancelled()) {
                    totalSize = 0;
                }
                List<String> row = parseFileLine(line);
                if (row != null && !row.isEmpty()) {
                    totalSize++;
                }
            }
        } catch (Exception e) {
            if (backgroundTask != null) {
                backgroundTask.setError(e.toString());
            }
            MyBoxLog.console(e);
            return false;
        }
        if (sourceWithNames && totalSize > 0) {
            totalSize--;
        }
        return true;
    }

    @Override
    protected String[][] readPageData() {
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        long end = startRowOfCurrentPage + pageSize;
        String[][] data = null;
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset))) {
            if (sourceWithNames) {
                readNames(reader);
            }
            String line;
            int rowIndex = -1, maxCol = 0;
            List<List<String>> rows = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
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
                rows.add(row);
                if (maxCol < row.size()) {
                    maxCol = row.size();
                }
            }
            if (!rows.isEmpty() && maxCol > 0) {
                int colsSize = sourceWithNames ? columns.size() : maxCol;
                data = new String[rows.size()][colsSize];
                for (int row = 0; row < rows.size(); row++) {
                    List<String> rowData = rows.get(row);
                    for (int col = 0; col < Math.min(rowData.size(), colsSize); col++) {
                        data[row][col] = rowData.get(col);
                    }
                }
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
        }
        if (data == null) {
            endRowOfCurrentPage = startRowOfCurrentPage;
        } else {
            endRowOfCurrentPage = startRowOfCurrentPage + data.length;
        }
        return data;

    }

    protected List<String> parseFileLine(String line) {
        return TextTools.parseLine(line, sourceDelimiterName);
    }

    @Override
    public void saveFile() {
        if (sourceFile == null) {
            saveAs();
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                String[][] data;

                @Override
                protected boolean handle() {
                    backup();
                    return save(sourceFile, sourceCharset, sourceDelimiterName, sourceWithNames);
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("Saved"));
                    recordFileWritten(sourceFile);
                    dataChangedNotify.set(false);
                    loadFile();
                }

            };
            start(task);
        }
    }

    @Override
    public void saveAs() {
        File file = chooseSaveFile();
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {
                String[][] data;

                @Override
                protected boolean handle() {
                    return save(file, targetCharset, targetDelimiterName, targetWithNames);
                }

                @Override
                protected void whenSucceeded() {
                    popInformation(message("Saved"));
                    recordFileWritten(file);
                    if (sourceFile == null || saveAsType == SaveAsType.Load) {
                        if (parentController != null) {
                            dataChangedNotify.set(false);
                            parentController.sourceFileChanged(file);
                            return;
                        }
                    }
                    DataFileTextController controller = (DataFileTextController) WindowTools.openStage(Fxmls.DataFileTextFxml);
                    controller.sourceFileChanged(file);
                }

            };
            start(task);
        }
    }

    public boolean save(File tfile, Charset charset, String delimiterName, boolean withNames) {
        if (columns == null) {
            makeColumns(colsCheck.length);
        }
        File tmpFile = TmpFileTools.getTempFile();
        String delimiter = TextTools.delimiterValue(delimiterName);
        if (sourceFile != null) {
            try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset));
                     BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, charset, false))) {
                List<String> colsNames = columnNames();
                if (sourceWithNames) {
                    readNames(reader);
                }
                if (withNames) {
                    TextFileTools.writeLine(writer, colsNames, delimiter);
                }
                long rowIndex = -1;
                String line;
                while ((line = reader.readLine()) != null) {
                    List<String> row = TextTools.parseLine(line, sourceDelimiterName);
                    if (row == null || row.isEmpty()) {
                        continue;
                    }
                    if (++rowIndex < startRowOfCurrentPage || rowIndex >= endRowOfCurrentPage) {
                        TextFileTools.writeLine(writer, row, delimiter);
                    } else if (rowIndex == startRowOfCurrentPage) {
                        writePageData(writer, delimiter);
                    }
                }
                if (rowIndex < 0) {
                    writePageData(writer, delimiter);
                }
                writer.flush();
            } catch (Exception e) {
                MyBoxLog.console(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }
        } else {
            try ( BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, charset, false))) {
                List<String> colsNames = columnNames();
                if (withNames) {
                    TextFileTools.writeLine(writer, colsNames, delimiter);
                }
                writePageData(writer, delimiter);
            } catch (Exception e) {
                MyBoxLog.console(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }
        }
        if (FileTools.rename(tmpFile, tfile, false)) {
            return saveDefinition(tfile.getAbsolutePath(), dataType, charset, delimiterName, withNames, columns);
        } else {
            if (task != null) {
                task.setError(message("Failed"));
            }
            return false;
        }
    }

    protected void writePageData(BufferedWriter writer, String delimiter) {
        try {
            if (writer == null || delimiter == null || sheetInputs == null) {
                return;
            }
            for (int r = 0; r < sheetInputs.length; r++) {
                List<String> rowData = new ArrayList<>();
                for (int c = 0; c < sheetInputs[r].length; c++) {
                    rowData.add(cellString(r, c));
                }
                TextFileTools.writeLine(writer, rowData, delimiter);
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    @Override
    protected boolean saveDefinition() {
        return saveDefinition(sourceFile.getAbsolutePath(), dataType, sourceCharset, sourceDelimiterName, sourceWithNames, columns);
    }

    @Override
    protected String fileText() {
        if (sourceFile == null) {
            return null;
        }
        String delimiter = delimiterValue(displayDelimiterName);
        StringBuilder s = new StringBuilder();
        if (textTitleCheck.isSelected()) {
            s.append(titleName()).append("\n\n");
        }
        if (textColumnCheck.isSelected()) {
            rowText(s, -1, columnNames(), delimiter);
        }
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset))) {
            if (sourceWithNames) {
                readNames(reader);
            }
            String line;
            int fileIndex = -1, dataIndex = 0;
            while ((line = reader.readLine()) != null) {
                List<String> row = TextTools.parseLine(line, sourceDelimiterName);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                if (++fileIndex < startRowOfCurrentPage || fileIndex >= endRowOfCurrentPage) {
                    rowText(s, dataIndex++, row, delimiter);
                } else if (fileIndex == startRowOfCurrentPage) {
                    dataIndex = pageText(s, dataIndex, delimiter);
                }
            }
            if (fileIndex < 0) {
                pageText(s, dataIndex, delimiter);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return s.toString();
    }

    @Override
    protected String fileHtml() {
        if (sourceFile == null) {
            return null;
        }
        StringBuilder s = new StringBuilder();
        List<String> names = null;
        if (htmlColumnCheck.isSelected()) {
            names = new ArrayList<>();
            if (htmlRowCheck.isSelected()) {
                names.add("");
            }
            names.addAll(columnNames());
        }
        String title = null;
        if (htmlTitleCheck.isSelected()) {
            title = titleName();
        }
        StringTable table = new StringTable(names, title);
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset))) {
            if (sourceWithNames) {
                readNames(reader);
            }
            String line;
            int fileIndex = -1, dataIndex = 0;
            while ((line = reader.readLine()) != null) {
                List<String> row = TextTools.parseLine(line, sourceDelimiterName);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                if (++fileIndex < startRowOfCurrentPage || fileIndex >= endRowOfCurrentPage) {
                    if (htmlRowCheck.isSelected()) {
                        row.add(0, message("Row") + (dataIndex + 1));
                    }
                    table.add(row);
                    dataIndex++;
                } else if (fileIndex == startRowOfCurrentPage) {
                    dataIndex = pageHtml(table, dataIndex);
                }
            }
            if (fileIndex < 0) {
                pageHtml(table, dataIndex);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return table.html();
    }

}
