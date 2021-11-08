package mara.mybox.data;

import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TextFileTools;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
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
    public long readDataDefinition() {
        d2did = -1;
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
            return -1;
        }
        return d2did;
    }

    private void checkCVSFormat() {
        if (sourceCsvFormat == null) {
            sourceCsvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withTrim().withNullString("")
                    .withDelimiter(sourceCsvDelimiter);
            if (hasHeader) {
                sourceCsvFormat = sourceCsvFormat.withFirstRecordAsHeader();
            }
        }
    }

    @Override
    public List<Data2DColumn> readColumns() {
        List<String> names = null;
        checkCVSFormat();
        try ( CSVParser parser = CSVParser.parse(file, charset, sourceCsvFormat)) {
            if (hasHeader) {
                names = parser.getHeaderNames();
                if (names == null) {
                    hasHeader = false;
                }
            }
            if (!hasHeader) {
                for (CSVRecord record : parser) {
                    names = new ArrayList<>();
                    for (int i = 1; i <= record.size(); i++) {
                        names.add(colPrefix() + i);
                    }
                    break;
                }
            }
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
        checkCVSFormat();
        dataSize = 0;
        try ( CSVParser parser = CSVParser.parse(file, charset, sourceCsvFormat)) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                while (backgroundTask != null && !backgroundTask.isCancelled() && iterator.hasNext()) {
                    iterator.next();
                    dataSize++;
                }
                if (backgroundTask == null || backgroundTask.isCancelled()) {
                    dataSize = 0;
                }
            }
        } catch (Exception e) {
            if (backgroundTask != null) {
                backgroundTask.setError(e.toString());
            }
            MyBoxLog.console(e);
            return -1;
        }
        return dataSize;
    }

    @Override
    public List<List<String>> readPageData() {
        checkCVSFormat();
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        long end = startRowOfCurrentPage + pageSize;
        try ( CSVParser parser = CSVParser.parse(file, charset, sourceCsvFormat)) {
            long rowIndex = -1;
            int columnsNumber = columnsNumber();
            List<List<String>> rows = new ArrayList<>();
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

    protected void writePageData(CSVPrinter csvPrinter) {
        try {
//            if (csvPrinter == null || sheetInputs == null) {
//                return;
//            }
//            for (int r = 0; r < sheetInputs.length; r++) {
//                csvPrinter.printRecord(row(r));
//            }
        } catch (Exception e) {
            MyBoxLog.console(e);
        }
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
