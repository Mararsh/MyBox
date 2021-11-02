package mara.mybox.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.DoubleStatistic;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.TextTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

/**
 * @Author Mara
 * @CreateDate 2021-9-6
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetText_Calculation extends ControlSheetText_Operations {

    @Override
    protected String[][] allRows(List<Integer> cols) {
        if (sourceFile == null || cols == null || cols.isEmpty()) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>();
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset))) {
            if (sourceWithNames) {
                readNames(reader);
            }
            String line;
            int rowIndex = -1;
            while ((line = reader.readLine()) != null) {
                List<String> row = parseFileLine(line);
                if (row == null || row.isEmpty()) {
                    continue;
                }
                if (++rowIndex < startRowOfCurrentPage || rowIndex >= endRowOfCurrentPage) {
                    List<String> values = new ArrayList<>();
                    for (int c : cols) {
                        if (c >= row.size()) {
                            break;
                        }
                        String d = row.get(c);
                        d = d == null ? "" : d;
                        values.add(d);
                    }
                    rows.add(values);
                } else if (rowIndex == startRowOfCurrentPage) {
                    copyPageData(rows, cols);
                }
            }
            if (rowIndex < 0) {
                copyPageData(rows, cols);
            }
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
        return TextTools.toArray(rows);
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
                try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset))) {
                    if (sourceWithNames) {
                        readNames(reader);
                    }
                    String line;
                    int fileRowIndex = -1, calLen = calCols.size(), dataIndex = 0;
                    while ((line = reader.readLine()) != null) {
                        List<String> lineData = parseFileLine(line);
                        if (lineData == null || lineData.isEmpty()) {
                            continue;
                        }
                        if (++fileRowIndex < startRowOfCurrentPage || fileRowIndex >= endRowOfCurrentPage) {
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
                                    if (col >= lineData.size()) {
                                        row.add("");
                                    } else {
                                        row.add(DoubleTools.percentage(doubleValue(lineData.get(col)), sData[c].sum));
                                    }
                                } else {
                                    row.add("");
                                }
                            }
                            for (int c = 0; c < disLen; c++) {
                                int col = disCols.get(c);
                                if (col >= lineData.size()) {
                                    row.add("");
                                } else {
                                    String v = lineData.get(col);
                                    row.add(v == null ? defaultColValue : v);
                                }
                            }
                            csvPrinter.printRecord(row);
                            row.clear();
                        } else if (fileRowIndex == startRowOfCurrentPage) {
                            dataIndex = writePageStatistic(csvPrinter, sData, calCols, disCols, percentage, dataIndex);
                        }

                    }
                    if (fileRowIndex < 0) {
                        dataIndex = writePageStatistic(csvPrinter, sData, calCols, disCols, percentage, dataIndex);
                    }
                } catch (Exception e) {
                    MyBoxLog.error(e);
                    return null;
                }
            }

//            saveDefinition(tmpFile.getAbsolutePath(),
//                    DataDefinition.DataType.DataFile, Charset.forName("UTF-8"), ",", true, statisticColumns(calCols, disCols));
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
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset))) {
            if (sourceWithNames) {
                readNames(reader);
            }
            String line;
            int index = -1;
            while ((line = reader.readLine()) != null) {
                List<String> lineData = parseFileLine(line);
                if (lineData == null || lineData.isEmpty()) {
                    continue;
                }
                if (++index < startRowOfCurrentPage || index >= endRowOfCurrentPage) {
                    for (int c = 0; c < calSize; c++) {
                        sData[c].count++;
                        int col = calCols.get(c);
                        if (col >= lineData.size()) {
                            continue;
                        }
                        double v = doubleValue(lineData.get(col));
                        sData[c].sum += v;
                        if (v > sData[c].maximum) {
                            sData[c].maximum = v;
                        }
                        if (v < sData[c].minimum) {
                            sData[c].minimum = v;
                        }
                    }
                } else if (index == startRowOfCurrentPage) {
                    countPageData(sData, calCols);
                }
            }
            if (index < 0) {
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
        try ( BufferedReader reader = new BufferedReader(new FileReader(sourceFile, sourceCharset))) {
            if (sourceWithNames) {
                readNames(reader);
            }
            String line;
            int index = -1;
            while ((line = reader.readLine()) != null) {
                List<String> lineData = parseFileLine(line);
                if (lineData == null || lineData.isEmpty()) {
                    continue;
                }
                if (++index < startRowOfCurrentPage || index >= endRowOfCurrentPage) {
                    for (int c = 0; c < calSize; c++) {
                        if (sData[c].count == 0) {
                            continue;
                        }
                        int col = calCols.get(c);
                        if (col >= lineData.size()) {
                            continue;
                        }
                        double v = doubleValue(lineData.get(col));
                        sData[c].variance += Math.pow(v - sData[c].mean, 2);
                        sData[c].skewness += Math.pow(v - sData[c].mean, 3);
                    }
                } else if (index == startRowOfCurrentPage) {
                    variancePageData(sData, calCols);
                }
            }
            if (index < 0) {
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
