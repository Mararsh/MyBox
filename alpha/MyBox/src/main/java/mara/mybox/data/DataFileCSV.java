package mara.mybox.data;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.collections.FXCollections;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2Column;
import mara.mybox.db.data.Data2DDefinition;
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
        type = Type.DataFileCSV;
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
            sourceCsvDelimiter = delimiter.charAt(0);
            if (definition == null) {
                definition = Data2DDefinition.create()
                        .setType(type).setFile(file)
                        .setDataName(file.getName())
                        .setCharset(charset).setHasHeader(hasHeader)
                        .setDelimiter(sourceCsvDelimiter + "");
                definition = tableData2DDefinition.insertData(conn, definition);
                conn.commit();
            } else {
                if (!userSavedDataDefinition) {
                    definition.setType(type).setFile(file)
                            .setDataName(file.getName())
                            .setCharset(charset)
                            .setDelimiter(delimiter)
                            .setHasHeader(hasHeader);
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
            MyBoxLog.debug(e);
            return false;
        }
        return d2did >= 0;
    }

    @Override
    public boolean readColumns(SingletonTask<Void> task) {
        columns = new ArrayList<>();
        sourceCsvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(sourceCsvDelimiter);
        if (d2did < 0) {
            return false;
        }
        if (!hasHeader) {
            return true;
        }
        sourceCsvFormat = sourceCsvFormat.withFirstRecordAsHeader();
        try ( CSVParser parser = CSVParser.parse(file, charset, sourceCsvFormat)) {
            List<String> names = parser.getHeaderNames();
            if (names == null) {
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
        sourceCsvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withTrim().withNullString("")
                .withDelimiter(sourceCsvDelimiter);
        if (hasHeader) {
            sourceCsvFormat = sourceCsvFormat.withFirstRecordAsHeader();
        }
        dataNumber = 0;
        try ( CSVParser parser = CSVParser.parse(file, charset, sourceCsvFormat)) {
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
        pageData = FXCollections.observableArrayList();
        try ( CSVParser parser = CSVParser.parse(file, charset, sourceCsvFormat)) {
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
