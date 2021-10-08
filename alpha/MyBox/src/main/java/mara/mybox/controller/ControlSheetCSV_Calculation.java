package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.DoubleStatistic;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

/**
 * @Author Mara
 * @CreateDate 2021-10-6
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetCSV_Calculation extends ControlSheetCSV_Operations {

    @Override
    protected String[][] allRows(List<Integer> cols) {
        if (sourceFile == null || cols == null || cols.isEmpty()) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>();
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            int index = 0;
            String d;
            for (CSVRecord record : parser) {
                if (++index < currentPageStart || index >= currentPageEnd) {
                    List<String> row = new ArrayList<>();
                    for (int c : cols) {
                        if (c >= record.size()) {
                            row.add(defaultColValue);
                        } else {
                            d = record.get(c);
                            row.add(d == null ? defaultColValue : d);
                        }
                    }
                    rows.add(row);
                } else if (index == currentPageStart) {
                    copyPageData(rows, cols);
                }
            }
            if (index == 0) {
                copyPageData(rows, cols);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        if (rows.isEmpty()) {
            return null;
        }
        String[][] data = new String[rows.size()][cols.size()];
        for (int r = 0; r < rows.size(); ++r) {
            List<String> row = rows.get(r);
            for (int c = 0; c < cols.size(); ++c) {
                int col = cols.get(c);
                if (col < row.size()) {
                    data[r][c] = row.get(col);
                }
            }
        }
        return data;
    }

    @Override
    protected File fileStatistic(List<Integer> calCols, List<Integer> disCols, boolean percentage) {
        if (sourceFile == null || calCols == null || calCols.isEmpty()) {
            return null;
        }

        DoubleStatistic[] sData = countFileStatistic(calCols);
        if (sData == null) {
            return null;
        }
        File tmpFile = TmpFileTools.getTempFile(".csv");
        CSVFormat csvFormat = CSVFormat.DEFAULT.withDelimiter(',')
                .withIgnoreEmptyLines().withTrim().withNullString("").withFirstRecordAsHeader();
        try ( CSVPrinter csvPrinter = new CSVPrinter(new FileWriter(tmpFile, Charset.forName("UTF-8")), csvFormat)) {
            writeStatisticData(csvPrinter, sData, calCols, disCols, percentage);

            int disLen = disCols == null ? 0 : disCols.size();
            if (percentage || disLen > 0) {
                try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
                    int fileRowIndex = 0, calLen = calCols.size(), dataIndex = 0;
                    for (CSVRecord record : parser) {
                        if (++fileRowIndex < currentPageStart || fileRowIndex >= currentPageEnd) {
                            dataIndex = fileRowIndex;
                            List<String> row = new ArrayList<>();
                            if (percentage) {
                                row.add(message("Percentage") + "_" + message("DataRow") + dataIndex + "_%");
                            } else {
                                row.add(message("DataRow") + dataIndex);
                            }
                            for (int c = 0; c < calLen; c++) {
                                if (percentage) {
                                    int col = calCols.get(c);
                                    if (col >= record.size()) {
                                        row.add("");
                                    } else {
                                        row.add(DoubleTools.percentage(doubleValue(record.get(col)), sData[c].sum));
                                    }
                                } else {
                                    row.add("");
                                }
                            }
                            for (int c = 0; c < disLen; c++) {
                                int col = disCols.get(c);
                                if (col >= record.size()) {
                                    row.add("");
                                } else {
                                    String v = record.get(col);
                                    row.add(v == null ? defaultColValue : v);
                                }
                            }
                            csvPrinter.printRecord(row);
                            row.clear();
                        } else if (fileRowIndex == currentPageStart) {
                            dataIndex = writePageStatistic(csvPrinter, sData, calCols, disCols, percentage, dataIndex);
                        }

                    }
                    if (fileRowIndex == 0) {
                        dataIndex = writePageStatistic(csvPrinter, sData, calCols, disCols, percentage, dataIndex);
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return null;
                }
            }

            saveDefinition(tmpFile.getAbsolutePath(),
                    DataDefinition.DataType.DataFile, Charset.forName("UTF-8"), ",", true, statisticColumns(calCols, disCols));

        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return tmpFile;
    }

    protected DoubleStatistic[] countFileStatistic(List<Integer> calCols) {
        if (sourceFile == null || calCols == null || calCols.isEmpty()) {
            return null;
        }
        int calSize = calCols.size();
        DoubleStatistic[] sData = new DoubleStatistic[calSize];
        for (int c = 0; c < calSize; c++) {
            sData[c] = new DoubleStatistic();
        }
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            int index = 0;
            for (CSVRecord record : parser) {
                if (++index < currentPageStart || index >= currentPageEnd) {
                    for (int c = 0; c < calSize; c++) {
                        sData[c].count++;
                        int col = calCols.get(c);
                        if (col >= record.size()) {
                            continue;
                        }
                        double v = doubleValue(record.get(col));
                        sData[c].sum += v;
                        if (v > sData[c].maximum) {
                            sData[c].maximum = v;
                        }
                        if (v < sData[c].minimum) {
                            sData[c].minimum = v;
                        }
                    }
                } else if (index == currentPageStart) {
                    countPageData(sData, calCols);
                }
            }
            if (index == 0) {
                countPageData(sData, calCols);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        boolean allInvalid = true;
        for (int c = 0; c < calSize; c++) {
            if (sData[c].count != 0) {
                sData[c].mean = sData[c].sum / sData[c].count;
                allInvalid = false;
            } else {
                sData[c].mean = AppValues.InvalidDouble;
                sData[c].variance = AppValues.InvalidDouble;
                sData[c].skewness = AppValues.InvalidDouble;
            }
        }
        if (allInvalid) {
            return sData;
        }
        try ( CSVParser parser = CSVParser.parse(sourceFile, sourceCharset, sourceCsvFormat)) {
            int index = 0;
            for (CSVRecord record : parser) {
                if (++index < currentPageStart || index >= currentPageEnd) {
                    for (int c = 0; c < calSize; c++) {
                        if (sData[c].count == 0) {
                            continue;
                        }
                        int col = calCols.get(c);
                        if (col >= record.size()) {
                            continue;
                        }
                        double v = doubleValue(record.get(col));
                        sData[c].variance += Math.pow(v - sData[c].mean, 2);
                        sData[c].skewness += Math.pow(v - sData[c].mean, 3);
                    }
                } else if (index == currentPageStart) {
                    variancePageData(sData, calCols);
                }
            }
            if (index == 0) {
                variancePageData(sData, calCols);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        for (int c = 0; c < calSize; c++) {
            if (sData[c].count == 0) {
                continue;
            }
            sData[c].variance = Math.sqrt(sData[c].variance / sData[c].count);
            sData[c].skewness = Math.cbrt(sData[c].skewness / sData[c].count);
        }

        return sData;
    }

}
