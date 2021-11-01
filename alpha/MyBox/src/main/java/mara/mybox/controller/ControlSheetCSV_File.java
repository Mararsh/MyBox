package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.data.StringTable;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.FileTools;
import static mara.mybox.tools.TextTools.delimiterValue;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2021-8-24
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetCSV_File extends ControlSheetFile {

    protected Charset sourceCharset, targetCharset;
    protected CSVFormat sourceCsvFormat;
    protected char sourceCsvDelimiter, targetCsvDelimiter;
    protected boolean autoDetermineSourceCharset, targetWithNames;

    @Override
    protected boolean readDataDefinition() {
        try ( Connection conn = DerbyBase.getConnection()) {
            dataName = sourceFile.getAbsolutePath();
//            dataDefinition = tableDataDefinition.read(conn, dataType, dataName);
            if (userSavedDataDefinition && dataDefinition != null) {
                sourceDelimiterName = dataDefinition.getDelimiter();
                sourceCsvDelimiter = sourceDelimiterName.charAt(0);
                sourceWithNames = dataDefinition.isHasHeader();
                sourceCharset = Charset.forName(dataDefinition.getCharset());
            }
            if (dataDefinition == null) {
                sourceDelimiterName = sourceCsvDelimiter + "";
                dataDefinition = DataDefinition.create().setDataName(dataName).setDataType(dataType)
                        .setCharset(sourceCharset.name()).setHasHeader(sourceWithNames)
                        .setDelimiter(sourceDelimiterName);
                tableDataDefinition.insertData(conn, dataDefinition);
                conn.commit();
            } else {
                if (!userSavedDataDefinition) {
                    sourceDelimiterName = sourceCsvDelimiter + "";
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

    @Override
    protected boolean readColumns() {
        columns = new ArrayList<>();
        sourceCsvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(sourceCsvDelimiter);
        if (!sourceWithNames) {
            return true;
        }
        sourceCsvFormat = sourceCsvFormat.withFirstRecordAsHeader();
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            List<String> names = parser.getHeaderNames();
            if (names == null) {
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
        sourceCsvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(sourceCsvDelimiter);
        if (sourceWithNames) {
            sourceCsvFormat = sourceCsvFormat.withFirstRecordAsHeader();
        }
        totalSize = 0;
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                while (backgroundTask != null && !backgroundTask.isCancelled() && iterator.hasNext()) {
                    iterator.next();
                    totalSize++;
                }
                if (backgroundTask == null || backgroundTask.isCancelled()) {
                    totalSize = 0;
                }
            }
        } catch (Exception e) {
            if (backgroundTask != null) {
                backgroundTask.setError(e.toString());
            }
            MyBoxLog.console(e);
            return false;
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
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            int rowIndex = -1, maxCol = 0;
            List<List<String>> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                if (++rowIndex < startRowOfCurrentPage) {
                    continue;
                }
                if (rowIndex >= end) {
                    break;
                }
                List<String> row = new ArrayList<>();
                for (int i = 0; i < record.size(); i++) {
                    row.add(record.get(i));
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

    protected String[][] read(int maxRows, int maxCols) {
        String[][] data = null;
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            int rowIndex = 0, maxCol = 0;
            List<List<String>> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                if (rowIndex++ >= maxRows) {
                    break;
                }
                List<String> row = new ArrayList<>();
                for (int i = 0; i < Math.min(maxCols, record.size()); i++) {
                    row.add(record.get(i));
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
            MyBoxLog.console(e);
        }
        return data;
    }

    protected String[][] readAll() {
        String[][] data = null;
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            int maxCol = 0;
            List<List<String>> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                List<String> row = new ArrayList<>();
                for (int i = 0; i < record.size(); i++) {
                    row.add(record.get(i));
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
            MyBoxLog.console(e);
        }
        return data;
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
            task = new SingletonTask<Void>(this) {
                String[][] data;

                @Override
                protected boolean handle() {
                    backup();
                    error = save(sourceFile, sourceCharset, sourceCsvFormat, sourceWithNames);
                    return error == null;
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
            task = new SingletonTask<Void>(this) {
                String[][] data;

                @Override
                protected boolean handle() {
                    CSVFormat targetCsvFormat = CSVFormat.DEFAULT
                            .withDelimiter(targetCsvDelimiter)
                            .withIgnoreEmptyLines().withTrim().withNullString("");
                    if (targetWithNames) {
                        targetCsvFormat = targetCsvFormat.withFirstRecordAsHeader();
                    }
                    error = save(file, targetCharset, targetCsvFormat, targetWithNames);
                    return error == null;
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
                    DataFileCSVController controller = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
                    controller.sourceFileChanged(file);
                }

            };
            start(task);
        }
    }

    public String save(File tfile, Charset charset, CSVFormat csvFormat, boolean withName) {
        File tmpFile = TmpFileTools.getTempFile();
        if (columns == null) {
            makeColumns(colsCheck.length);
        }
        if (charset == null) {
            charset = Charset.forName("UTF-8");
        }
        if (sourceFile != null) {
            try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat);
                     CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, charset), csvFormat)) {
                if (withName) {
                    csvPrinter.printRecord(columnNames());
                }
                long index = -1;
                for (CSVRecord record : parser) {
                    if (++index < startRowOfCurrentPage || index >= endRowOfCurrentPage) {
                        csvPrinter.printRecord(record);
                    } else if (index == startRowOfCurrentPage) {
                        writePageData(csvPrinter);
                    }
                }
                if (index < 0) {
                    writePageData(csvPrinter);
                }
            } catch (Exception e) {
                MyBoxLog.console(e);
                return e.toString();
            }
        } else {
            try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, charset), csvFormat)) {
                if (withName) {
                    csvPrinter.printRecord(columnNames());
                }
                writePageData(csvPrinter);
            } catch (Exception e) {
                MyBoxLog.console(e);
                return e.toString();
            }
        }
        if (FileTools.rename(tmpFile, tfile, false)) {
            saveDefinition(tfile.getAbsolutePath(), dataType, charset, csvFormat.getDelimiter() + "", withName, columns);
            return null;
        } else {
            return "Failed";
        }
    }

    protected void writePageData(CSVPrinter csvPrinter) {
        try {
            if (csvPrinter == null || sheetInputs == null) {
                return;
            }
            for (int r = 0; r < sheetInputs.length; r++) {
                csvPrinter.printRecord(row(r));
            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
    }

    @Override
    protected boolean saveDefinition() {
        return saveDefinition(sourceFile.getAbsolutePath(), dataType,
                sourceCharset, sourceDelimiterName, sourceWithNames, columns);
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
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            int fileIndex = -1, dataIndex = 0;
            for (CSVRecord record : parser) {
                if (++fileIndex < startRowOfCurrentPage || fileIndex >= endRowOfCurrentPage) {
                    List<String> values = new ArrayList<>();
                    for (String v : record) {
                        values.add(v);
                    }
                    rowText(s, dataIndex++, values, delimiter);
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
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            int fileIndex = -1, dataIndex = 0;
            for (CSVRecord record : parser) {
                if (++fileIndex < startRowOfCurrentPage || fileIndex >= endRowOfCurrentPage) {
                    List<String> values = new ArrayList<>();
                    if (htmlRowCheck.isSelected()) {
                        values.add(message("Row") + (dataIndex + 1));
                    }
                    for (String v : record) {
                        values.add(v);
                    }
                    table.add(values);
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
