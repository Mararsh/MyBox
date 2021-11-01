package mara.mybox.data;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.db.data.DataDefinition.DataType;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
import mara.mybox.tools.TextFileTools;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class DataFileCSV extends DataFile {

    protected CSVFormat sourceCsvFormat;
    protected char sourceCsvDelimiter = ',', targetCsvDelimiter = ',';

    public DataFileCSV() {
        type = Type.DataFileCSV;;
    }

    @Override
    public boolean readDataDefinition(SingletonTask<Void> task) {
        if (file == null) {
            return false;
        }
        try ( Connection conn = DerbyBase.getConnection()) {
            definition = tableDataDefinition.queryFile(conn, file);
            if (userSavedDataDefinition) {
                if (definition != null) {
                    sourceCsvDelimiter = definition.getDelimiter().charAt(0);
                    sourceWithNames = definition.isHasHeader();
                    sourceCharset = Charset.forName(definition.getCharset());
                } else {
                    sourceWithNames = true;
                    sourceCharset = TextFileTools.charset(file);
                    String d = guessDelimiter();
                    if (d != null) {
                        sourceCsvDelimiter = d.charAt(0);
                    }
                }
            }
            if (sourceCharset == null) {
                sourceCharset = Charset.defaultCharset();
            }
            if (definition == null) {
                definition = DataDefinition.create()
                        .setDataType(DataType.DataFile).setFile(file)
                        .setDataName(file.getName())
                        .setCharset(sourceCharset.name()).setHasHeader(sourceWithNames)
                        .setDelimiter(sourceCsvDelimiter + "");
                definition = tableDataDefinition.insertData(conn, definition);
                conn.commit();
            } else {
                if (!userSavedDataDefinition) {
                    definition.setDataType(DataType.DataFile).setFile(file)
                            .setDataName(file.getName())
                            .setCharset(sourceCharset.name())
                            .setDelimiter(sourceCsvDelimiter + "")
                            .setHasHeader(sourceWithNames);
                    definition = tableDataDefinition.updateData(conn, definition);
                    conn.commit();
                }
                savedColumns = tableDataColumn.read(conn, definition.getDfid());
            }
            userSavedDataDefinition = true;
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.debug(e);
            return false;
        }
        return definition != null && definition.getDfid() >= 0;
    }

    @Override
    public boolean readColumns(SingletonTask<Void> task) {
        columns = new ArrayList<>();
        sourceCsvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(sourceCsvDelimiter);
        if (!sourceWithNames) {
            return true;
        }
        sourceCsvFormat = sourceCsvFormat.withFirstRecordAsHeader();
        try ( CSVParser parser = CSVParser.parse(file, sourceCharset, sourceCsvFormat)) {
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
        sourceCsvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(sourceCsvDelimiter);
        if (sourceWithNames) {
            sourceCsvFormat = sourceCsvFormat.withFirstRecordAsHeader();
        }
        dataNumber = 0;
        try ( CSVParser parser = CSVParser.parse(file, sourceCharset, sourceCsvFormat)) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                while (task != null && !task.isCancelled() && iterator.hasNext()) {
                    iterator.next();
                    dataNumber++;
                }
                if (task == null || task.isCancelled()) {
                    dataNumber = 0;
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
    public boolean readPageData(SingletonTask<Void> task) {
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        long end = startRowOfCurrentPage + pageSize;
        pageData = null;
        try ( CSVParser parser = CSVParser.parse(file, sourceCharset, sourceCsvFormat)) {
            int rowIndex = -1, maxCol = 0;
            List<List<String>> fileRows = new ArrayList<>();
            for (CSVRecord record : parser) {
                if (task == null || task.isCancelled()) {
                    break;
                }
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

    /*
        get/set
     */
    public CSVFormat getSourceCsvFormat() {
        return sourceCsvFormat;
    }

    public void setSourceCsvFormat(CSVFormat sourceCsvFormat) {
        this.sourceCsvFormat = sourceCsvFormat;
    }

    public char getSourceCsvDelimiter() {
        return sourceCsvDelimiter;
    }

    public void setSourceCsvDelimiter(char sourceCsvDelimiter) {
        this.sourceCsvDelimiter = sourceCsvDelimiter;
    }

    public char getTargetCsvDelimiter() {
        return targetCsvDelimiter;
    }

    public void setTargetCsvDelimiter(char targetCsvDelimiter) {
        this.targetCsvDelimiter = targetCsvDelimiter;
    }

}
