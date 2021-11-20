package mara.mybox.data;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.tools.TmpFileTools;
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

    public DataFileCSV() {
        type = Type.DataFileCSV;
    }

    @Override
    public long readDataDefinition() {
        d2did = -1;
        savedColumns = null;
        sourceCsvFormat = null;
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
            if (delimiter == null || delimiter.isEmpty()) {
                delimiter = ",";
            }
            if (dataName == null || dataName.isBlank()) {
                dataName = file.getName();
            }
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
            MyBoxLog.debug(e);
            return -1;
        }
        return d2did;
    }

    private void checkCVSFormat() {
        if (sourceCsvFormat == null) {
            if (delimiter == null || delimiter.isEmpty()) {
                delimiter = ",";
            }
            sourceCsvFormat = CSVFormat.DEFAULT.withIgnoreEmptyLines().withTrim().withNullString(defaultColValue())
                    .withDelimiter(delimiter.charAt(0));
            if (hasHeader) {
                sourceCsvFormat = sourceCsvFormat.withFirstRecordAsHeader();
            }
        }
    }

    @Override
    public List<String> readColumns() {
        List<String> names = null;
        checkCVSFormat();
        try ( CSVParser parser = CSVParser.parse(file, charset, sourceCsvFormat)) {
            if (hasHeader) {
                names = parser.getHeaderNames();
            } else {
                Iterator<CSVRecord> iterator = parser.iterator();
                if (iterator != null && iterator.hasNext()) {
                    CSVRecord record = iterator.next();
                    if (record != null) {
                        names = new ArrayList<>();
                        for (int i = 1; i <= record.size(); i++) {
                            names.add(colPrefix() + i);
                        }
                    }
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
        checkCVSFormat();
        dataSize = 0;
        try ( CSVParser parser = CSVParser.parse(file, charset, sourceCsvFormat)) {
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    if (backgroundTask == null || backgroundTask.isCancelled()) {
                        dataSize = 0;
                        break;
                    }
                    try {
                        if (iterator.next() != null) {
                            dataSize++;
                        }
                    } catch (Exception e) {  // skip  bad lines
                    }
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
        endRowOfCurrentPage = startRowOfCurrentPage;
        long end = startRowOfCurrentPage + pageSize;
        List<List<String>> rows;
        try ( CSVParser parser = CSVParser.parse(file, charset, sourceCsvFormat)) {
            long rowIndex = -1;
            int columnsNumber = columnsNumber();
            rows = new ArrayList<>();
            Iterator<CSVRecord> iterator = parser.iterator();
            if (iterator != null) {
                while (iterator.hasNext()) {
                    if (task == null || task.isCancelled()) {
                        if (task != null) {
                            task.setError("Canceled");
                        }
                        break;
                    }
                    try {
                        CSVRecord record = iterator.next();
                        if (record != null) {
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
                    } catch (Exception e) {  // skip  bad lines
                    }
                }
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

    public boolean savePageData(File tFile, Charset tCharset, CSVFormat tFormat, boolean withName) {
        File tmpFile = TmpFileTools.getTempFile();
        if (tCharset == null) {
            tCharset = Charset.forName("UTF-8");
        }
        if (file != null) {
            checkCVSFormat();
            try ( CSVParser parser = CSVParser.parse(file, charset, sourceCsvFormat);
                     CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, tCharset), tFormat)) {
                if (withName) {
                    csvPrinter.printRecord(columnNames());
                }
                long index = -1;
                long pageStart = getStartRowOfCurrentPage(), pageEnd = getEndRowOfCurrentPage();
                Iterator<CSVRecord> iterator = parser.iterator();
                if (iterator != null) {
                    while (iterator.hasNext()) {
                        if (task == null || task.isCancelled()) {
                            if (task != null) {
                                task.setError("Canceled");
                            }
                            break;
                        }
                        try {
                            CSVRecord record = iterator.next();
                            if (record != null) {
                                if (++index < pageStart || index >= pageEnd) {
                                    csvPrinter.printRecord(record);
                                } else if (index == pageStart) {
                                    if (!writePageData(csvPrinter)) {
                                        return false;
                                    }
                                }
                            }
                        } catch (Exception e) {  // skip  bad lines
                        }
                    }
                }
                if (index < 0) {
                    if (!writePageData(csvPrinter)) {
                        return false;
                    }
                }
            } catch (Exception e) {
                MyBoxLog.debug(e);
                if (task != null) {
                    task.setError(e.toString());
                }
                return false;
            }
        } else {
            try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, tCharset), tFormat)) {
                if (withName) {
                    csvPrinter.printRecord(columnNames());
                }
                if (!writePageData(csvPrinter)) {
                    return false;
                }
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

    public boolean writePageData(CSVPrinter csvPrinter) {
        try {
            if (csvPrinter == null) {
                return false;
            }
            for (int r = 0; r < tableRowsNumber(); r++) {
                if (task == null || task.isCancelled()) {
                    if (task != null) {
                        task.setError("Canceled");
                    }
                    return false;
                }
                csvPrinter.printRecord(pageRow(r));
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


    /*
        get/set
     */
    public CSVFormat getSourceCsvFormat() {
        return sourceCsvFormat;
    }

    public void setSourceCsvFormat(CSVFormat sourceCsvFormat) {
        this.sourceCsvFormat = sourceCsvFormat;
    }

}
