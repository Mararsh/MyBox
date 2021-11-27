package mara.mybox.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TextTools;
import mara.mybox.tools.TmpFileTools;

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
            Data2DDefinition definition = null;
            if (userSavedDataDefinition) {
                definition = tableData2DDefinition.queryFile(conn, type, file);
                if (definition != null) {
                    load(definition);
                }
            }
            if (charset == null) {
                charset = TextFileTools.charset(file);
            }
            if (delimiter == null || delimiter.isEmpty()) {
                delimiter = guessDelimiter();
            }
            if (!isTmpFile()) {
                checkBeforeSaving();
                if (definition == null) {
                    definition = tableData2DDefinition.insertData(conn, this);
                    conn.commit();
                    d2did = definition.getD2did();
                } else {
                    tableData2DDefinition.updateData(conn, this);
                    conn.commit();
                    d2did = definition.getD2did();
                    if (userSavedDataDefinition) {
                        savedColumns = tableData2DColumn.read(conn, d2did);
                    }
                }
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
            names = readValidLine(reader);
            if (!hasHeader && names != null) {
                int len = names.size();
                names = new ArrayList<>();
                for (int i = 1; i <= len; i++) {
                    names.add(colPrefix() + i);
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

    protected List<String> parseFileLine(String line) {
        return TextTools.parseLine(line, delimiter);
    }

    protected List<String> readValidLine(BufferedReader reader) {
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
        List<List<String>> rows = new ArrayList<>();
        try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            if (hasHeader) {
                readValidLine(reader);
            }
            String line;
            long rowIndex = -1;
            long end = startRowOfCurrentPage + pageSize;
            int columnsNumber = columnsNumber();
            while ((line = reader.readLine()) != null && task != null && !task.isCancelled()) {
                List<String> record = parseFileLine(line);
                if (record == null || record.isEmpty()) {
                    continue;
                }
                if (++rowIndex < startRowOfCurrentPage) {
                    continue;
                }
                if (rowIndex >= end) {
                    break;
                }
                List<String> row = new ArrayList<>();
                row.add("" + (rowIndex + 1));
                for (int i = 0; i < record.size(); i++) {
                    row.add(record.get(i));
                }
                for (int col = row.size(); col < columnsNumber; col++) {
                    row.add(defaultColValue());
                }
                rows.add(row);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return null;
        }
        endRowOfCurrentPage = startRowOfCurrentPage + rows.size();
        return rows;
    }

    @Override
    public boolean savePageData(Data2D targetData) {
        if (targetData == null || !(targetData instanceof DataFileText)) {
            return false;
        }
        DataFileText targetTextFile = (DataFileText) targetData;
        File tmpFile = TmpFileTools.getTempFile();
        File tFile = targetTextFile.getFile();
        if (tFile == null) {
            return false;
        }
        Charset tCharset = targetTextFile.getCharset();
        if (tCharset == null) {
            tCharset = Charset.forName("UTF-8");
        }
        String tDelimiter = targetTextFile.getDelimiter();
        if (tDelimiter == null || tDelimiter.isEmpty()) {
            tDelimiter = ",";
        }
        boolean tHasHeader = targetTextFile.isHasHeader();
        if (file != null) {
            try ( BufferedReader reader = new BufferedReader(new FileReader(file, charset));
                     BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, tCharset, false))) {
                List<String> colsNames = columnNames();
                if (hasHeader) {
                    readValidLine(reader);
                }
                if (tHasHeader) {
                    TextFileTools.writeLine(writer, colsNames, tDelimiter);
                }
                long rowIndex = -1;
                String line;
                while ((line = reader.readLine()) != null && task != null && !task.isCancelled()) {
                    List<String> row = parseFileLine(line);
                    if (row == null || row.isEmpty()) {
                        continue;
                    }
                    if (++rowIndex < startRowOfCurrentPage || rowIndex >= endRowOfCurrentPage) {
                        TextFileTools.writeLine(writer, row, tDelimiter);
                    } else if (rowIndex == startRowOfCurrentPage) {
                        writePageData(writer, tDelimiter);
                    }
                }
                if (rowIndex < 0) {
                    writePageData(writer, tDelimiter);
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
                if (tHasHeader) {
                    TextFileTools.writeLine(writer, colsNames, tDelimiter);
                }
                writePageData(writer, tDelimiter);
            } catch (Exception e) {
                MyBoxLog.console(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }
        }
        return FileTools.rename(tmpFile, tFile, false);
    }

    public boolean writePageData(BufferedWriter writer, String delimiter) {
        try {
            if (writer == null || delimiter == null) {
                return false;
            }
            for (int r = 0; r < tableRowsNumber(); r++) {
                if (task == null || task.isCancelled()) {
                    return false;
                }
                TextFileTools.writeLine(writer, pageRow(r), delimiter);
            }
            return true;
        } catch (Exception e) {
            MyBoxLog.console(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return false;
        }
    }

    @Override
    public File tmpFile(List<String> cols, List<List<String>> data) {
        try {
            if (cols == null || cols.isEmpty()) {
                if (data == null || data.isEmpty()) {
                    return null;
                }
            }
            File tmpFile = TmpFileTools.getTempFile(type == Type.DataFileCSV ? ".csv" : ".txt");
            String fDelimiter = ",";
            try ( BufferedWriter writer = new BufferedWriter(new FileWriter(tmpFile, Charset.forName("UTF-8"), false))) {
                if (cols != null && !cols.isEmpty()) {
                    TextFileTools.writeLine(writer, cols, fDelimiter);
                }
                if (data != null) {
                    for (int r = 0; r < data.size(); r++) {
                        if (task != null && task.isCancelled()) {
                            break;
                        }
                        TextFileTools.writeLine(writer, data.get(r), fDelimiter);
                    }
                }
            }
            return tmpFile;
        } catch (Exception e) {
            MyBoxLog.console(e);
            if (task != null) {
                task.setError(e.toString());
            }
            return null;
        }
    }

}
