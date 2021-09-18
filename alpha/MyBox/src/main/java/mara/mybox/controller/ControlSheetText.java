package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import static mara.mybox.tools.TextTools.delimiterValue;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2021-9-6
 * @License Apache License Version 2.0
 */
public class ControlSheetText extends ControlSheetFile {

    protected Charset sourceCharset, targetCharset;
    protected boolean targetWithNames;
    protected String targetDelimiterName;

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

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
        if (currentPageStart < 1) {
            currentPageStart = 1;
        }
        long end = currentPageStart + pageSize;
        String[][] data = null;
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset))) {
            if (sourceWithNames) {
                readNames(reader);
            }
            String line;
            int rowIndex = 0, maxCol = 0;
            List<List<String>> rows = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                List<String> row = parseFileLine(line);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                if (++rowIndex < currentPageStart) {
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
                data = new String[rows.size()][maxCol];
                for (int row = 0; row < rows.size(); row++) {
                    List<String> rowData = rows.get(row);
                    for (int col = 0; col < rowData.size(); col++) {
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
            currentPageEnd = currentPageStart;
        } else {
            currentPageEnd = currentPageStart + data.length;  // 1-based, excluded
        }
        return data;

    }

    protected List<String> parseFileLine(String line) {
        return TextTools.parseLine(line, sourceDelimiterName);
    }

    @Override
    protected File fileCopyCols(List<Integer> cols, boolean withNames) {
        if (sourceFile == null || cols == null || cols.isEmpty()) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset));
                 CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, Charset.forName("utf-8")), CSVFormat.DEFAULT)) {
            if (sourceWithNames) {
                readNames(reader);
            }
            String line;
            int rowIndex = 0;
            while ((line = reader.readLine()) != null) {
                List<String> row = parseFileLine(line);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                if (++rowIndex < currentPageStart || rowIndex >= currentPageEnd) {
                    List<String> values = new ArrayList<>();
                    for (int c : cols) {
                        if (c >= row.size()) {
                            break;
                        }
                        String d = row.get(c);
                        d = d == null ? "" : d;
                        values.add(d);
                    }
                    csvPrinter.printRecord(values);
                } else if (rowIndex == currentPageStart) {
                    copyPageData(csvPrinter, cols);
                }
            }
            if (rowIndex == 0) {
                copyPageData(csvPrinter, cols);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
        }
        return tmpFile;
    }

    @Override
    protected File fileSetCols(List<Integer> cols, String value) {
        if (sourceFile == null || cols == null || cols.isEmpty()) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, sourceCharset, false))) {
            String delimiter = TextTools.delimiterValue(sourceDelimiterName);
            if (sourceWithNames) {
                TextFileTools.writeLine(writer, readNames(reader), delimiter);
            }
            String line;
            Random random = null;
            while ((line = reader.readLine()) != null) {
                List<String> row = parseFileLine(line);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                String rowString = "";
                int len = row.size();
                for (int c = 0; c < len; c++) {
                    if (cols.contains(c)) {
                        String v = value;
                        if (AppValues.MyBoxRandomFlag.equals(value)) {
                            if (random == null) {
                                random = new Random();
                            }
                            v = columns.get(c).random(random, maxRandom, scale);
                        }
                        rowString += v;
                    } else {
                        rowString += row.get(c);
                    }
                    if (c < len - 1) {
                        rowString += delimiter;
                    }
                }
                writer.write(rowString);
            }
            writer.flush();
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
        }
        return tmpFile;
    }

    @Override
    public File filePaste(ControlSheetCSV sourceController, int row, int col, boolean enlarge) {
        if (sourceController == null || sourceController.sourceFile == null || sourceFile == null || row < 0 || col < 0) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( CSVParser sourceParser = CSVParser.parse(sourceController.sourceFile, sourceController.sourceCharset, sourceController.sourceCsvFormat);
                 BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, sourceCharset, false))) {
            String delimiter = TextTools.delimiterValue(sourceDelimiterName);
            if (sourceWithNames) {
                TextFileTools.writeLine(writer, readNames(reader), delimiter);
            }
            Iterator<CSVRecord> sourceIterator = sourceParser.iterator();
            List<String> values = new ArrayList<>();
            int colsSize;
            int targetColsSize = columns.size();
            if (enlarge && col + sourceController.colsNumber > targetColsSize) {
                colsSize = col + sourceController.colsNumber;
            } else {
                colsSize = targetColsSize;
            }
            long rowsIndex = 0, sourceRowsSize = sourceController.rowsTotal(), targetRowsSize = rowsTotal(), rowsSize;
            if (enlarge && row + sourceRowsSize > targetRowsSize) {
                rowsSize = row + sourceRowsSize;
            } else {
                rowsSize = targetRowsSize;
            }
            String line;
            while (rowsIndex < row && rowsIndex < rowsSize && (line = reader.readLine()) != null) {
                List<String> rowData = parseFileLine(line);
                if (rowData == null || rowData.isEmpty()) {
                    continue;
                }
                makeRow(rowData, colsSize, values);
                TextFileTools.writeLine(writer, values, delimiter);
                rowsIndex++;
            }
            while (rowsIndex >= row && rowsIndex < rowsSize && rowsIndex < row + sourceRowsSize && sourceIterator.hasNext()) {
                List<String> rowData = TextTools.parseLine(reader.readLine(), sourceDelimiterName);
                makeRow(sourceIterator.next(), rowData, col, colsSize, values);
                TextFileTools.writeLine(writer, values, delimiter);
                rowsIndex++;
            }
            while (rowsIndex < rowsSize && (line = reader.readLine()) != null) {
                List<String> rowData = parseFileLine(line);
                if (rowData == null || rowData.isEmpty()) {
                    continue;
                }
                makeRow(rowData, colsSize, values);
                TextFileTools.writeLine(writer, values, delimiter);
                rowsIndex++;
            }
            writer.flush();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
    }

    protected void makeRow(List<String> rowData, int colsSize, List<String> values) {
        try {
            values.clear();
            int colIndex = 0;
            for (; colIndex < Math.min(colsSize, rowData.size()); colIndex++) {
                values.add(rowData.get(colIndex));
            }
            for (; colIndex < colsSize; colIndex++) {
                values.add(defaultColValue);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    protected void makeRow(CSVRecord sourceRecord, List<String> rowData,
            int col, int colsSize, List<String> values) {
        try {
            values.clear();
            int colsIndex = 0;
            while (colsIndex < col && colsIndex < colsSize) {
                if (rowData != null && colsIndex < rowData.size()) {
                    values.add(rowData.get(colsIndex));
                } else {
                    values.add(defaultColValue);
                }
                colsIndex++;
            }
            while (colsIndex >= col && colsIndex < colsSize && colsIndex < col + sourceRecord.size()) {
                values.add(sourceRecord.get(colsIndex - col));
                colsIndex++;
            }
            while (colsIndex < colsSize) {
                if (rowData != null && colsIndex < rowData.size()) {
                    values.add(rowData.get(colsIndex));
                } else {
                    values.add(defaultColValue);
                }
                colsIndex++;
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    protected File fileSortCol(int col, boolean asc) {
        if (sourceFile == null || col < 0) {
            return null;
        }
        List<List<String>> records = new ArrayList<>();
        List<String> names = null;
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset))) {
            if (sourceWithNames) {
                names = readNames(reader);
            }
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> row = TextTools.parseLine(line, sourceDelimiterName);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                records.add(row);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        if (records.isEmpty()) {
            return null;
        }
        Collections.sort(records, new Comparator<List<String>>() {
            @Override
            public int compare(List<String> row1, List<String> row2) {
                try {
                    ColumnDefinition column = columns.get(col);
                    int v = column.compare(row1.get(col), row2.get(col));
                    return asc ? v : -v;
                } catch (Exception e) {
                    return 0;
                }
            }
        });
        File tmpFile = TmpFileTools.getTempFile();
        try ( BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, sourceCharset, false))) {
            String delimiter = TextTools.delimiterValue(sourceDelimiterName);
            if (sourceWithNames) {
                TextFileTools.writeLine(writer, names, delimiter);
            }
            for (List<String> record : records) {
                TextFileTools.writeLine(writer, record, delimiter);
            }
            writer.flush();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
    }

    @Override
    protected File fileAddCols(int col, boolean left, int number) {
        if (sourceFile == null || number < 1) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, sourceCharset, false))) {
            String delimiter = TextTools.delimiterValue(sourceDelimiterName);
            if (sourceWithNames) {
                if (readNames(reader) != null) {
                    TextFileTools.writeLine(writer, columnNames(), delimiter);
                }
            }
            List<String> newValues = new ArrayList<>();
            for (int i = 0; i < number; i++) {
                newValues.add(defaultColValue);
            }
            List<String> values = new ArrayList<>();
            int index;
            if (col < 0) {
                index = 0;
            } else {
                index = col + (left ? 0 : 1);
            }
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> row = TextTools.parseLine(line, sourceDelimiterName);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                values.addAll(row);
                values.addAll(index, newValues);
                TextFileTools.writeLine(writer, values, delimiter);
                values.clear();
            }
            writer.flush();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
    }

    @Override
    protected File fileDeleteAll(boolean keepCols) {
        if (sourceFile == null) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, sourceCharset, false))) {
            if (sourceWithNames && keepCols) {
                String delimiter = TextTools.delimiterValue(sourceDelimiterName);
                TextFileTools.writeLine(writer, columnNames(), delimiter);
                writer.flush();
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
    }

    @Override
    protected File fileDeleteCols(List<Integer> cols) {
        if (sourceFile == null || cols == null || cols.isEmpty()) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile();
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset));
                 BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, sourceCharset, false))) {
            String delimiter = TextTools.delimiterValue(sourceDelimiterName);
            List<String> colsNames = columnNames();
            if (sourceWithNames) {
                TextFileTools.writeLine(writer, colsNames, delimiter);
            }
            List<String> values = new ArrayList<>();
            List<Integer> indexs = new ArrayList<>();
            for (int col = 0; col < colsNames.size(); ++col) {
                if (!cols.contains(col)) {
                    indexs.add(col);
                }
            }
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> row = TextTools.parseLine(line, sourceDelimiterName);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                for (int i = 0; i < indexs.size(); ++i) {
                    values.add(row.get(indexs.get(i)));
                }
                TextFileTools.writeLine(writer, values, delimiter);
                values.clear();
            }
            writer.flush();
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
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
                long rowIndex = 0;
                String line;
                while ((line = reader.readLine()) != null) {
                    List<String> row = TextTools.parseLine(line, sourceDelimiterName);
                    if (row == null || row.isEmpty()) {
                        continue;
                    }
                    if (++rowIndex < currentPageStart || rowIndex >= currentPageEnd) {    // 1-based, excluded
                        TextFileTools.writeLine(writer, row, delimiter);
                    } else if (rowIndex == currentPageStart) {
                        writePageData(writer, delimiter);
                    }
                }
                if (rowIndex == 0) {
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
            int fileIndex = 0, dataIndex = 0;
            while ((line = reader.readLine()) != null) {
                List<String> row = TextTools.parseLine(line, sourceDelimiterName);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                if (++fileIndex < currentPageStart || fileIndex >= currentPageEnd) {
                    rowText(s, dataIndex++, row, delimiter);
                } else if (fileIndex == currentPageStart) {
                    dataIndex = pageText(s, dataIndex, delimiter);
                }
            }
            if (fileIndex == 0) {
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
            int fileIndex = 0, dataIndex = 0;
            while ((line = reader.readLine()) != null) {
                List<String> row = TextTools.parseLine(line, sourceDelimiterName);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                if (++fileIndex < currentPageStart || fileIndex >= currentPageEnd) {
                    if (htmlRowCheck.isSelected()) {
                        row.add(0, message("Row") + (dataIndex + 1));
                    }
                    table.add(row);
                    dataIndex++;
                } else if (fileIndex == currentPageStart) {
                    dataIndex = pageHtml(table, dataIndex);
                }
            }
            if (fileIndex == 0) {
                pageHtml(table, dataIndex);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return table.html();
    }

}
