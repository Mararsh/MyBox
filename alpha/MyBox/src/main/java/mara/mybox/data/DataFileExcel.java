package mara.mybox.data;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.MicrosoftDocumentTools;
import static mara.mybox.value.Languages.message;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * @Author Mara
 * @CreateDate 2021-10-17
 * @License Apache License Version 2.0
 */
public class DataFileExcel extends DataFile {

    protected String currentSheetName, targetSheetName;
    protected List<String> sheetNames;
    protected boolean currentSheetOnly;

    public DataFileExcel() {
        type = Type.DataFileExcel;
    }

    @Override
    public long readDataDefinition() {
        d2did = -1;
        if (file == null) {
            return -1;
        }
        try ( Connection conn = DerbyBase.getConnection();
                 Workbook wb = WorkbookFactory.create(file)) {
            int sheetsNumber = wb.getNumberOfSheets();
            sheetNames = new ArrayList<>();
            for (int i = 0; i < sheetsNumber; i++) {
                sheetNames.add(wb.getSheetName(i));
            }
            Sheet sheet;
            if (currentSheetName != null) {
                sheet = wb.getSheet(currentSheetName);
            } else {
                sheet = wb.getSheetAt(0);
                currentSheetName = sheet.getSheetName();
            }

            Data2DDefinition definition = tableData2DDefinition.queryFileName(conn, type, file, currentSheetName);

            if (userSavedDataDefinition && definition != null) {
                hasHeader = definition.isHasHeader();
            }
            boolean changed = !userSavedDataDefinition;
            if (hasHeader) {
                Iterator<Row> iterator = sheet.iterator();
                Row firstRow = null;
                if (iterator != null) {
                    while (iterator.hasNext()) {
                        firstRow = iterator.next();
                        if (firstRow != null) {
                            break;
                        }
                    }
                }
                if (firstRow == null) {
                    hasHeader = false;
                    changed = true;
                }
            }
            if (definition == null) {
                definition = Data2DDefinition.create().setType(type)
                        .setFile(file).setDataName(currentSheetName)
                        .setHasHeader(hasHeader);
                tableData2DDefinition.insertData(conn, definition);
                conn.commit();
            } else {
                if (changed) {
                    definition.setType(type)
                            .setFile(file).setDataName(currentSheetName)
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
            MyBoxLog.console(e);
        }
        return d2did;
    }

    @Override
    public List<Data2DColumn> readColumns() {
        List<Data2DColumn> fileColumns = null;
        try ( Workbook wb = WorkbookFactory.create(file)) {
            Sheet sheet;
            if (currentSheetName != null) {
                sheet = wb.getSheet(currentSheetName);
            } else {
                sheet = wb.getSheetAt(0);
                currentSheetName = sheet.getSheetName();
            }
            Iterator<Row> iterator = sheet.iterator();
            if (iterator == null) {
                hasHeader = false;
                return null;
            }
            Row firstRow = null;
            while (iterator.hasNext()) {
                firstRow = iterator.next();
                if (firstRow != null) {
                    break;
                }
            }
            if (firstRow == null) {
                hasHeader = false;
                return null;
            }
            fileColumns = new ArrayList<>();
            for (int col = firstRow.getFirstCellNum(); col < firstRow.getLastCellNum(); col++) {
                String name = (message(colPrefix()) + (col + 1));
                ColumnType ctype = ColumnType.String;
                if (hasHeader) {
                    Cell cell = firstRow.getCell(col);
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case NUMERIC:
                                ctype = ColumnType.Double;
                                break;
                            case BOOLEAN:
                                ctype = ColumnType.Boolean;
                                break;
                        }
                        String v = MicrosoftDocumentTools.cellString(cell);
                        if (!v.isBlank()) {
                            name = v;
                        }
                    }
                }
                Data2DColumn column = new Data2DColumn(name, ctype);
                fileColumns.add(column);
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
            return null;
        }
        return fileColumns;
    }

    @Override
    public long readTotal() {
        dataSize = 0;
        try ( Workbook wb = WorkbookFactory.create(file)) {
            Sheet sheet;
            if (currentSheetName != null) {
                sheet = wb.getSheet(currentSheetName);
            } else {
                sheet = wb.getSheetAt(0);
                currentSheetName = sheet.getSheetName();
            }
            Iterator<Row> iterator = sheet.iterator();
            if (iterator != null) {
                while (backgroundTask != null && !backgroundTask.isCancelled() && iterator.hasNext()) {
                    iterator.next();
                    dataSize++;
                }
                if (backgroundTask == null || backgroundTask.isCancelled()) {
                    dataSize = 0;
                } else if (hasHeader && dataSize > 0) {
                    dataSize--;
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
        if (startRowOfCurrentPage < 0) {
            startRowOfCurrentPage = 0;
        }
        tableData.clear();
        try ( Workbook wb = WorkbookFactory.create(file)) {
            Sheet sheet;
            if (currentSheetName != null) {
                sheet = wb.getSheet(currentSheetName);
            } else {
                sheet = wb.getSheetAt(0);
                currentSheetName = sheet.getSheetName();
            }
            Iterator<Row> iterator = sheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                if (hasHeader) {
                    while (iterator.hasNext() && (iterator.next() == null) && task != null && !task.isCancelled()) {
                    }
                }
                long rowIndex = -1;
                int columnsNumber = columnsNumber();
                long end = startRowOfCurrentPage + pageSize;
                List<List<String>> rows = new ArrayList<>();
                while (iterator.hasNext() && task != null && !task.isCancelled()) {
                    Row fileRow = iterator.next();
                    if (fileRow == null) {
                        continue;
                    }
                    if (++rowIndex < startRowOfCurrentPage) {
                        continue;
                    }
                    if (rowIndex >= end) {
                        break;
                    }
                    List<String> row = new ArrayList<>();
                    for (int cellIndex = fileRow.getFirstCellNum(); cellIndex < fileRow.getLastCellNum(); cellIndex++) {
                        String v = MicrosoftDocumentTools.cellString(fileRow.getCell(cellIndex));
                        row.add(v);
                    }
                    for (int col = row.size(); col < columnsNumber; col++) {
                        row.add(defaultColValue());
                    }
                    rows.add(row);
                }
                return rows;
            }
        } catch (Exception e) {
            if (task != null) {
                task.setError(e.toString());
            }
            MyBoxLog.console(e);
        }
        return null;
    }


    /*
        get/set
     */
    public String getCurrentSheetName() {
        return currentSheetName;
    }

    public void setCurrentSheetName(String currentSheetName) {
        this.currentSheetName = currentSheetName;
    }

    public String getTargetSheetName() {
        return targetSheetName;
    }

    public void setTargetSheetName(String targetSheetName) {
        this.targetSheetName = targetSheetName;
    }

    public List<String> getSheetNames() {
        return sheetNames;
    }

    public void setSheetNames(List<String> sheetNames) {
        this.sheetNames = sheetNames;
    }

    public boolean isCurrentSheetOnly() {
        return currentSheetOnly;
    }

    public void setCurrentSheetOnly(boolean currentSheetOnly) {
        this.currentSheetOnly = currentSheetOnly;
    }

}
