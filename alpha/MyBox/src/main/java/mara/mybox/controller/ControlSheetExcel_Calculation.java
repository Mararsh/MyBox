package mara.mybox.controller;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.data.DoubleStatistic;
import mara.mybox.db.data.DataDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.DoubleTools;
import mara.mybox.tools.MicrosoftDocumentTools;
import mara.mybox.tools.TmpFileTools;
import mara.mybox.value.AppValues;
import static mara.mybox.value.Languages.message;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * @Author Mara
 * @CreateDate 2021-8-25
 * @License Apache License Version 2.0
 */
public abstract class ControlSheetExcel_Calculation extends ControlSheetExcel_Operations {

    @Override
    protected String[][] allRows(List<Integer> cols) {
        if (sourceFile == null || cols == null || cols.isEmpty()) {
            return null;
        }
        List<List<String>> rows = new ArrayList<>();
        try ( Workbook sourceBook = WorkbookFactory.create(sourceFile)) {
            Sheet sourceSheet;
            if (currentSheetName != null) {
                sourceSheet = sourceBook.getSheet(currentSheetName);
            } else {
                sourceSheet = sourceBook.getSheetAt(0);
                currentSheetName = sourceSheet.getSheetName();
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                if (sourceWithNames) {
                    while (iterator.hasNext() && (iterator.next() == null)) {
                    }
                }
                int sourceRowIndex = -1;
                String d;
                while (iterator.hasNext()) {
                    Row sourceRow = iterator.next();
                    if (sourceRow == null) {
                        continue;
                    }
                    if (++sourceRowIndex < currentPageStart || sourceRowIndex >= currentPageEnd) {
                        List<String> row = new ArrayList<>();
                        for (int c : cols) {
                            int cellIndex = c + sourceRow.getFirstCellNum();
                            if (cellIndex >= sourceRow.getLastCellNum()) {
                                row.add(defaultColValue);
                            } else {
                                d = MicrosoftDocumentTools.cellString(sourceRow.getCell(cellIndex));
                                row.add(d == null ? defaultColValue : d);
                            }
                        }
                        rows.add(row);
                    } else if (sourceRowIndex == currentPageStart) {
                        copyPageData(rows, cols);
                    }
                }
            } else {
                copyPageData(rows, cols);
            }
            if (rows.isEmpty()) {
                return null;
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
                try ( Workbook sourceBook = WorkbookFactory.create(sourceFile)) {
                    Sheet sourceSheet;
                    if (currentSheetName != null) {
                        sourceSheet = sourceBook.getSheet(currentSheetName);
                    } else {
                        sourceSheet = sourceBook.getSheetAt(0);
                        currentSheetName = sourceSheet.getSheetName();
                    }
                    Iterator<Row> iterator = sourceSheet.iterator();
                    int sourceRowIndex = -1, calLen = calCols.size(), dataIndex = 0;
                    if (iterator != null && iterator.hasNext()) {
                        if (sourceWithNames) {
                            while (iterator.hasNext() && (iterator.next() == null)) {
                            }
                        }
                        String d;
                        while (iterator.hasNext()) {
                            Row sourceRow = iterator.next();
                            if (sourceRow == null) {
                                continue;
                            }
                            if (++sourceRowIndex < currentPageStart || sourceRowIndex >= currentPageEnd) {
                                dataIndex = sourceRowIndex;
                                List<String> row = new ArrayList<>();
                                if (percentage) {
                                    row.add(message("Percentage") + "_" + message("DataRow") + dataIndex + "_%");
                                } else {
                                    row.add(message("DataRow") + dataIndex);
                                }
                                for (int c = 0; c < calLen; c++) {
                                    if (percentage) {
                                        int cellIndex = calCols.get(c) + sourceRow.getFirstCellNum();
                                        if (cellIndex >= sourceRow.getLastCellNum()) {
                                            row.add("");
                                        } else {
                                            d = MicrosoftDocumentTools.cellString(sourceRow.getCell(cellIndex));
                                            row.add(DoubleTools.percentage(doubleValue(d), sData[c].sum));
                                        }

                                    } else {
                                        row.add("");
                                    }
                                }
                                for (int c = 0; c < disLen; c++) {
                                    int cellIndex = disCols.get(c) + sourceRow.getFirstCellNum();
                                    if (cellIndex >= sourceRow.getLastCellNum()) {
                                        row.add(defaultColValue);
                                    } else {
                                        d = MicrosoftDocumentTools.cellString(sourceRow.getCell(cellIndex));
                                        row.add(d == null ? defaultColValue : d);
                                    }
                                }
                                csvPrinter.printRecord(row);
                                row.clear();

                            } else if (sourceRowIndex == currentPageStart) {
                                dataIndex = writePageStatistic(csvPrinter, sData, calCols, disCols, percentage, dataIndex);
                            }
                        }
                    } else {
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
        try ( Workbook sourceBook = WorkbookFactory.create(sourceFile)) {
            Sheet sourceSheet;
            if (currentSheetName != null) {
                sourceSheet = sourceBook.getSheet(currentSheetName);
            } else {
                sourceSheet = sourceBook.getSheetAt(0);
                currentSheetName = sourceSheet.getSheetName();
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            int sourceRowIndex = -1;
            if (iterator != null && iterator.hasNext()) {
                if (sourceWithNames) {
                    while (iterator.hasNext() && (iterator.next() == null)) {
                    }
                }
                String d;
                while (iterator.hasNext()) {
                    Row sourceRow = iterator.next();
                    if (sourceRow == null) {
                        continue;
                    }
                    if (++sourceRowIndex < currentPageStart || sourceRowIndex >= currentPageEnd) {
                        for (int c = 0; c < calSize; c++) {
                            sData[c].count++;
                            int cellIndex = calCols.get(c) + sourceRow.getFirstCellNum();
                            if (cellIndex >= sourceRow.getLastCellNum()) {
                                continue;
                            }
                            double v = doubleValue(MicrosoftDocumentTools.cellString(sourceRow.getCell(cellIndex)));
                            sData[c].sum += v;
                            if (v > sData[c].maximum) {
                                sData[c].maximum = v;
                            }
                            if (v < sData[c].minimum) {
                                sData[c].minimum = v;
                            }
                        }
                    } else if (sourceRowIndex == currentPageStart) {
                        countPageData(sData, calCols);
                    }
                }
            } else {
                countPageData(sData, calCols);
            }
            if (sourceRowIndex < 0) {
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
        try ( Workbook sourceBook = WorkbookFactory.create(sourceFile)) {
            Sheet sourceSheet;
            if (currentSheetName != null) {
                sourceSheet = sourceBook.getSheet(currentSheetName);
            } else {
                sourceSheet = sourceBook.getSheetAt(0);
                currentSheetName = sourceSheet.getSheetName();
            }
            Iterator<Row> iterator = sourceSheet.iterator();
            int sourceRowIndex = -1;
            if (iterator != null && iterator.hasNext()) {
                if (sourceWithNames) {
                    while (iterator.hasNext() && (iterator.next() == null)) {
                    }
                }
                String d;
                while (iterator.hasNext()) {
                    Row sourceRow = iterator.next();
                    if (sourceRow == null) {
                        continue;
                    }
                    if (++sourceRowIndex < currentPageStart || sourceRowIndex >= currentPageEnd) {
                        for (int c = 0; c < calSize; c++) {
                            if (sData[c].count == 0) {
                                continue;
                            }
                            int cellIndex = calCols.get(c) + sourceRow.getFirstCellNum();
                            if (cellIndex >= sourceRow.getLastCellNum()) {
                                continue;
                            }
                            double v = doubleValue(MicrosoftDocumentTools.cellString(sourceRow.getCell(cellIndex)));
                            sData[c].variance += Math.pow(v - sData[c].mean, 2);
                            sData[c].skewness += Math.pow(v - sData[c].mean, 3);

                        }
                    } else if (sourceRowIndex == currentPageStart) {
                        variancePageData(sData, calCols);
                    }
                }
            } else {
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
