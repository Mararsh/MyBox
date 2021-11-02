package mara.mybox.data;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.ColumnDefinition.ColumnType;
import mara.mybox.db.data.Data2Column;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonTask;
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
    public boolean readDataDefinition(SingletonTask<Void> task) {
        d2did = -1;
        if (file == null) {
            return false;
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
            return false;
        }
        return d2did >= 0;
    }

    @Override
    public boolean readColumns(SingletonTask<Void> task) {
        columns = new ArrayList<>();
        if (d2did < 0) {
            return false;
        }
        if (!hasHeader) {
            return true;
        }
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
                return true;
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
                return true;
            }
            for (int col = firstRow.getFirstCellNum(); col < firstRow.getLastCellNum(); col++) {
                String name = (message(colPrefix()) + (col + 1));
                ColumnType ctype = ColumnType.String;
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
                    Data2Column column = new Data2Column(name, ctype);
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
        dataNumber = 0;
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
                while (task != null && !task.isCancelled() && iterator.hasNext()) {
                    iterator.next();
                    dataNumber++;
                }
                if (task == null || task.isCancelled()) {
                    dataNumber = 0;
                } else if (hasHeader && dataNumber > 0) {
                    dataNumber--;
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
        pageData = null;
        try ( Workbook wb = WorkbookFactory.create(file)) {
            Sheet sheet;
            if (currentSheetName != null) {
                sheet = wb.getSheet(currentSheetName);
            } else {
                sheet = wb.getSheetAt(0);
                currentSheetName = sheet.getSheetName();
            }
            List<List<String>> fileRows = new ArrayList<>();
            Iterator<Row> iterator = sheet.iterator();
            if (iterator != null && iterator.hasNext()) {
                if (hasHeader) {
                    while (iterator.hasNext() && (iterator.next() == null) && task != null && !task.isCancelled()) {
                    }
                }
                int rowIndex = -1, maxCol = 0;
                long end = startRowOfCurrentPage + pageSize;
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
                    fileRows.add(row);
                    if (maxCol < row.size()) {
                        maxCol = row.size();
                    }
                }
                loadPageData(fileRows, hasHeader ? columns.size() : maxCol);
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
